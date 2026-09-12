package com.amazonchecker.scheduler;

import com.amazonchecker.entity.ProductEntity;
import com.amazonchecker.web.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class SchedulerRunnerTest {

    static class StubProductService extends ProductService {
        private final Supplier<List<ProductEntity>> supplier;

        public StubProductService(Supplier<List<ProductEntity>> supplier) {
            super(null, null);
            this.supplier = supplier;
        }

        @Override
        public List<ProductEntity> getActiveProducts() {
            return supplier != null ? supplier.get() : Collections.emptyList();
        }
    }

    @Test
    @DisplayName("Should enforce concurrency locking: second check is blocked when one is running")
    void testConcurrencyLocking() throws Exception {
        CountDownLatch checkStartedLatch = new CountDownLatch(1);
        CountDownLatch checkBlockLatch = new CountDownLatch(1);

        ProductService stubService = new StubProductService(() -> {
            checkStartedLatch.countDown();
            try {
                checkBlockLatch.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return Collections.emptyList();
        });

        SchedulerRunner runner = new SchedulerRunner(stubService);
        runner.setEnabled(true);
        runner.setIntervalMinutes(30);

        Thread checkThread = new Thread(() -> runner.executeCheck("TestCheck"));
        checkThread.start();

        // Wait until first check is definitely inside executeCheck
        assertTrue(checkStartedLatch.await(2, TimeUnit.SECONDS));
        assertTrue(runner.isChecking(), "Runner should report checking=true");

        // Attempt a second check while the first is running
        boolean startedSecond = runner.triggerManualCheck();
        assertFalse(startedSecond, "Second manual check should be rejected because check is in progress");

        boolean directSecond = runner.executeCheck("ConcurrentAttempt");
        assertFalse(directSecond, "Direct executeCheck should also return false due to lock");

        // Release the first check
        checkBlockLatch.countDown();
        checkThread.join(2000);

        assertFalse(runner.isChecking(), "Runner should report checking=false after check finishes");
        assertNotNull(runner.getLastCheckTime(), "Last check time should be recorded");
        assertNotNull(runner.getNextCheckTime(), "Next check time should be calculated");
    }

    @Test
    @DisplayName("Should correctly calculate interval and countdown values")
    void testIntervalCalculations() {
        SchedulerRunner runner = new SchedulerRunner(new StubProductService(() -> Collections.emptyList()));
        runner.setIntervalMinutes(15);
        runner.setEnabled(true);

        assertEquals(15, runner.getIntervalMinutes());
        assertTrue(runner.isEnabled());

        // When disabled, countdown should return null
        runner.setEnabled(false);
        assertNull(runner.getSecondsUntilNextCheck());
        assertNull(runner.getMinutesUntilNextCheck());
    }
}
