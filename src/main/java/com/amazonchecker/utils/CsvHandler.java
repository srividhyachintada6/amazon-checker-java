package com.amazonchecker.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Equivalent of utils/csv_handler.py
 * Reads/writes the simple two-column products.csv (product_name, product_url).
 * No external CSV library is used, so this keeps to a naive split on comma,
 * same simplifying assumption the original script made.
 */
public class CsvHandler {

    public static List<Product> readProducts(String csvFile) throws IOException {
        List<Product> products = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(csvFile), StandardCharsets.UTF_8)) {
            String header = reader.readLine(); // skip header row: product_name,product_url
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                String trimmed = line.trim();
                // Product URLs always begin with http:// or https://
                int httpIndex = trimmed.indexOf("https://");
                if (httpIndex == -1) {
                    httpIndex = trimmed.indexOf("http://");
                }

                if (httpIndex > 0) {
                    String url = trimmed.substring(httpIndex).trim();
                    String namePart = trimmed.substring(0, httpIndex).trim();
                    if (namePart.endsWith(",")) {
                        namePart = namePart.substring(0, namePart.length() - 1).trim();
                    }
                    if (namePart.startsWith("\"") && namePart.endsWith("\"") && namePart.length() >= 2) {
                        namePart = namePart.substring(1, namePart.length() - 1).replace("\"\"", "\"").trim();
                    }
                    products.add(new Product(namePart, url));
                } else {
                    int firstComma = trimmed.indexOf(',');
                    if (firstComma != -1) {
                        String name = trimmed.substring(0, firstComma).trim();
                        String url = trimmed.substring(firstComma + 1).trim();
                        products.add(new Product(name, url));
                    }
                }
            }
        }
        return products;
    }

    public static void writeProducts(List<Product> products, String csvFile) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(csvFile), StandardCharsets.UTF_8)) {
            writer.write("product_name,product_url");
            writer.newLine();
            for (Product p : products) {
                String name = p.getName();
                if (name.contains(",") || name.contains("\"")) {
                    name = "\"" + name.replace("\"", "\"\"") + "\"";
                }
                writer.write(name + "," + p.getUrl());
                writer.newLine();
            }
        }
    }
}
