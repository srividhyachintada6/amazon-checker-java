package com.amazonchecker.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Equivalent of utils/logger.py
 * Named LogWriter (not Logger) to avoid clashing with java.util.logging.Logger.
 */
public class LogWriter {

    private static final String LOG_FILE = "data/availability_log.txt";

    public static void logResult(String product, String status, String price) {
        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            writer.write(String.format("%s | %s | %s | %s%n",
                    LocalDateTime.now(), product, status, price));
        } catch (IOException e) {
            System.err.println("⚠️  Could not write to log file: " + e.getMessage());
        }
    }
}
