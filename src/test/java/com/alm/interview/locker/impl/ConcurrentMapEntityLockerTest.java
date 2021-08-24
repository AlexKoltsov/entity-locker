package com.alm.interview.locker.impl;

import com.alm.interview.model.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class ConcurrentMapEntityLockerTest {

    private static final int NUMBER_OF_THREADS = 10;

    private final ConcurrentMapEntityLocker<Long> longEntityLocker = new ConcurrentMapEntityLocker<>();
    private final Consumer<User> userScoreIncrementer = _user -> {
        try {
            Thread.sleep(10L); // emulate long running user update;
            Long prevScore = _user.getUserScore();
            Long newScore = prevScore + 1;
            _user.setUserScore(newScore);
            log.info("User ID's [{}] score updated [{}] -> [{}]", _user.getId(), prevScore, newScore);
        } catch (InterruptedException e) {
            log.error("InterruptedException occured", e);
        }
    };
    private final Consumer<List<User>> allUsersScoreIncrementer = users -> {
        log.info("Global job started");
        users.forEach(userScoreIncrementer);
        log.info("Global job completed");
    };

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5, 10, 100, 1_000})
    void runWithLock1EntityTest(int jobsToSubmit) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        User user = new User(1L, "Alex", "Koltsov", 0L);

        IntStream.iterate(0, i -> i < jobsToSubmit, i -> i + 1)
                .forEach(i -> executorService.submit(() -> {
                    try {
                        longEntityLocker.runWithLock(user.getId(), () -> userScoreIncrementer.accept(user));
                    } catch (InterruptedException e) {
                        log.info("Thread was interrupted");
                    }
                }));

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
        assertEquals(jobsToSubmit, user.getUserScore());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5, 10, 100, 1_000})
    void runWithLock2EntitiesTest(int jobsToSubmit) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        User user1 = new User(1L, "Alex", "Koltsov", 0L);
        User user2 = new User(2L, "Other", "Person", 0L);

        IntStream.iterate(0, i -> i < jobsToSubmit, i -> i + 1)
                .forEach(i -> executorService.submit(() -> {
                    try {
                        longEntityLocker.runWithLock(user1.getId(), () -> userScoreIncrementer.accept(user1));
                    } catch (InterruptedException e) {
                        log.info("Thread was interrupted");
                    }
                }));
        IntStream.iterate(0, i -> i < jobsToSubmit, i -> i + 1)
                .forEach(i -> executorService.submit(() -> {
                    try {
                        longEntityLocker.runWithLock(user2.getId(), () -> userScoreIncrementer.accept(user2));
                    } catch (InterruptedException e) {
                        log.info("Thread was interrupted");
                    }
                }));

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
        assertEquals(jobsToSubmit, user1.getUserScore());
        assertEquals(jobsToSubmit, user2.getUserScore());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5, 10, 100, 1_000})
    void runWithGlobalLock(int jobsToSubmit) throws InterruptedException {
        int globalJobsToSubmit = jobsToSubmit / 10;
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        ExecutorService executorServiceForGlobalTasks = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        User user1 = new User(1L, "Alex", "Koltsov", 0L);
        User user2 = new User(2L, "Other", "Person", 0L);
        List<User> allUsers = List.of(user1, user2);

        IntStream.iterate(0, i -> i < jobsToSubmit, i -> i + 1)
                .forEach(i -> executorService.submit(() -> {
                    try {
                        longEntityLocker.runWithLock(user1.getId(), () -> userScoreIncrementer.accept(user1));
                    } catch (InterruptedException e) {
                        log.info("Thread was interrupted");
                    }
                }));
        IntStream.iterate(0, i -> i < globalJobsToSubmit, i -> i + 1)
                .forEach(i -> executorServiceForGlobalTasks.submit(() -> {
                    try {
                        longEntityLocker.runWithGlobalLock(() -> allUsersScoreIncrementer.accept(allUsers));
                    } catch (InterruptedException e) {
                        log.info("Thread was interrupted");
                    }
                }));

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
        assertEquals(jobsToSubmit + globalJobsToSubmit, user1.getUserScore());
        assertEquals(globalJobsToSubmit, user2.getUserScore());
    }
}