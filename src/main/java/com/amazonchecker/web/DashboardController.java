package com.amazonchecker.web;

import com.amazonchecker.Main;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final AtomicBoolean checking = new AtomicBoolean(false);

    @GetMapping("/status")
    public String status() {

        if (checking.get()) {
            return """
                    {
                      "status": "checking",
                      "message": "Checking Amazon products..."
                    }
                    """;
        }

        return """
                {
                  "status": "ready",
                  "message": "Amazon checker is ready"
                }
                """;
    }

    @PostMapping("/check")
    public String checkProducts() {

        if (checking.get()) {
            return """
                    {
                      "status": "checking",
                      "message": "A check is already running"
                    }
                    """;
        }

        checking.set(true);

        CompletableFuture.runAsync(() -> {
            try {
                Main.main(new String[0]);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                checking.set(false);
            }
        });

        return """
                {
                  "status": "started",
                  "message": "Amazon checking started"
                }
                """;
    }

    @GetMapping("/log")
    public String getLog() {

        try {

            Path logFile = Path.of("data", "availability_log.txt");

            if (!Files.exists(logFile)) {
                return "";
            }

            return Files.readString(logFile);

        } catch (IOException e) {
            return "Unable to read log: " + e.getMessage();
        }
    }
}