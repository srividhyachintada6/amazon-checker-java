package com.amazonchecker.scheduler;

import com.amazonchecker.config.Settings;

/**
 * Equivalent of scheduler/scheduler.py
 * Runs a task repeatedly, sleeping CHECK_INTERVAL_HOURS between runs.
 */
public class SchedulerRunner {

    public static void runScheduler(Runnable task) {
        while (true) {
            task.run();
            try {
                long sleepMillis = Settings.CHECK_INTERVAL_HOURS * 3600L * 1000L;
                Thread.sleep(sleepMillis);
                System.out.println("Next check in " + Settings.CHECK_INTERVAL_HOURS + " hours...");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
