package com.amazonchecker.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Equivalent of utils/screenshot.py
 * Uses headless Chrome (via Selenium + WebDriverManager) to save a screenshot of the product page.
 * WebDriverManager automatically downloads a matching chromedriver, so no manual driver setup
 * is required as long as Google Chrome is installed on the machine.
 */
public class Screenshot {

    public static String takeScreenshot(String url, String productName) throws IOException {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        ChromeDriver driver = new ChromeDriver(options);
        try {
            driver.get(url);
            Thread.sleep(5000);

            File screenshotDir = new File("screenshots");
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String safeName = productName.replaceAll("[^a-zA-Z0-9_-]", "_");
            String filename = "screenshots/" + safeName + "_" + timestamp + ".png";

            File srcFile = driver.getScreenshotAs(OutputType.FILE);
            Files.copy(srcFile.toPath(), Path.of(filename));

            return filename;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Screenshot interrupted", e);
        } finally {
            driver.quit();
        }
    }
}
