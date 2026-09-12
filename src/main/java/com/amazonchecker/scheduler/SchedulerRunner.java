package com.amazonchecker.scheduler;

import com.amazonchecker.Main;
import com.amazonchecker.config.Settings;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Scheduled runner for periodic product availability and price checking.
 * Coordinates both automatic schedule executions and manual check triggers,
 * guaranteeing that two checks never execute concurrently.
 */
@Component
public class SchedulerRunner {

    private final AtomicBoolean checking = new AtomicBoolean(false);
    private Thread schedulerThread;
    private volatile boolean running = false;
    private volatile LocalDateTime lastCheckTime = null;
    private volatile LocalDateTime nextCheckTime = null;

    @Value("${checker.schedule.minutes:30}")
    private int intervalMinutes = 30;

    @Value("${checker.schedule.enabled:true}")
    private boolean enabled = true;

    @PostConstruct
    public void start() {
        initializeTimesFromLog();

        if (!enabled) {
            System.out.println("ℹ️ Automatic scheduler is disabled via configuration.");
            return;
        }

        running = true;
        schedulerThread = new Thread(this::runSchedulerLoop, "amazon-scheduler-thread");
        schedulerThread.setDaemon(true);
        schedulerThread.start();
        System.out.println("🚀 Automatic scheduler started. Configured interval: " + intervalMinutes + " minutes. Next check: " + getFormattedNextCheckTime());
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (schedulerThread != null) {
            schedulerThread.interrupt();
        }
    }

    /**
     * Periodic loop running in background thread.
     * Evaluates next check time once per second so shutdown and manual rescheduling respond instantly.
     */
    private void runSchedulerLoop() {
        while (running) {
            try {
                Thread.sleep(1000);

                if (!running || !enabled) {
                    continue;
                }

                if (nextCheckTime != null && LocalDateTime.now().isAfter(nextCheckTime)) {
                    if (!checking.get()) {
                        System.out.println("⏰ Automatic check triggered by scheduler...");
                        executeCheck("Automatic");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Executes check with strict mutual exclusion.
     * Prevents simultaneous runs between manual and scheduled tasks.
     */
    public boolean executeCheck(String triggerType) {
        if (!checking.compareAndSet(false, true)) {
            System.out.println("⚠️ A check is already in progress. Skipping " + triggerType + " check request.");
            return false;
        }

        try {
            System.out.println("🔍 Starting " + triggerType + " Amazon product check...");
            Main.run();
            lastCheckTime = LocalDateTime.now();
            nextCheckTime = lastCheckTime.plusMinutes(intervalMinutes);
            System.out.println("✅ " + triggerType + " check finished. Next scheduled check at: " + getFormattedNextCheckTime());
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error during " + triggerType + " check: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            checking.set(false);
        }
    }

    /**
     * Trigger manual check asynchronously. Returns false immediately if check is already running.
     */
    public boolean triggerManualCheck() {
        if (checking.get()) {
            return false;
        }
        CompletableFuture.runAsync(() -> executeCheck("Manual"));
        return true;
    }

    public boolean isChecking() {
        return checking.get();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getIntervalMinutes() {
        return intervalMinutes;
    }

    public LocalDateTime getLastCheckTime() {
        return lastCheckTime;
    }

    public LocalDateTime getNextCheckTime() {
        return nextCheckTime;
    }

    public Long getSecondsUntilNextCheck() {
        if (!enabled || nextCheckTime == null) {
            return null;
        }
        long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), nextCheckTime);
        return Math.max(0L, seconds);
    }

    public Long getMinutesUntilNextCheck() {
        Long seconds = getSecondsUntilNextCheck();
        if (seconds == null) {
            return null;
        }
        return Math.max(0L, seconds / 60);
    }

    public String getFormattedLastCheckTime() {
        if (lastCheckTime == null) return null;
        return lastCheckTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    public String getFormattedNextCheckTime() {
        if (nextCheckTime == null) return null;
        return nextCheckTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    private void initializeTimesFromLog() {
        Path logPath = Path.of("data", "availability_log.txt");
        if (Files.exists(logPath)) {
            try {
                List<String> lines = Files.readAllLines(logPath, StandardCharsets.UTF_8);
                for (int i = lines.size() - 1; i >= 0; i--) {
                    String line = lines.get(i);
                    if (line != null && line.contains("|")) {
                        String ts = line.split("\\|")[0].trim();
                        try {
                            lastCheckTime = LocalDateTime.parse(ts.substring(0, Math.min(19, ts.length())));
                            break;
                        } catch (Exception ignored) {
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (lastCheckTime != null) {
            nextCheckTime = lastCheckTime.plusMinutes(intervalMinutes);
            if (nextCheckTime.isBefore(LocalDateTime.now())) {
                nextCheckTime = LocalDateTime.now().plusMinutes(intervalMinutes);
            }
        } else {
            nextCheckTime = LocalDateTime.now().plusMinutes(intervalMinutes);
        }
    }

    /**
     * Legacy static runner for CLI invocation compatibility.
     */
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
