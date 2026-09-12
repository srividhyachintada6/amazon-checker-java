package com.amazonchecker.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads/writes products.csv (product_name, product_url, store).
 * Backward-compatible with 2-column format.
 */
public class CsvHandler {

    public static List<Product> readProducts(String csvFile) throws IOException {
        List<Product> products = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(csvFile), StandardCharsets.UTF_8)) {
            String header = reader.readLine(); // skip header row
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                String trimmed = line.trim();
                int httpIndex = trimmed.indexOf("https://");
                if (httpIndex == -1) {
                    httpIndex = trimmed.indexOf("http://");
                }

                if (httpIndex > 0) {
                    String afterHttp = trimmed.substring(httpIndex).trim();
                    String namePart = trimmed.substring(0, httpIndex).trim();
                    if (namePart.endsWith(",")) {
                        namePart = namePart.substring(0, namePart.length() - 1).trim();
                    }
                    if (namePart.startsWith("\"") && namePart.endsWith("\"") && namePart.length() >= 2) {
                        namePart = namePart.substring(1, namePart.length() - 1).replace("\"\"", "\"").trim();
                    }

                    // Check if after URL there is a comma with store
                    String url = afterHttp;
                    String store = "AMAZON";
                    int nextComma = afterHttp.indexOf(',');
                    if (nextComma != -1) {
                        url = afterHttp.substring(0, nextComma).trim();
                        store = afterHttp.substring(nextComma + 1).trim();
                    }
                    if (store.isBlank()) store = "AMAZON";

                    products.add(new Product(namePart, url, store));
                } else {
                    String[] parts = trimmed.split(",");
                    if (parts.length >= 2) {
                        String name = parts[0].trim();
                        String url = parts[1].trim();
                        String store = parts.length >= 3 ? parts[2].trim() : "AMAZON";
                        products.add(new Product(name, url, store));
                    }
                }
            }
        }
        return products;
    }

    public static void writeProducts(List<Product> products, String csvFile) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(csvFile), StandardCharsets.UTF_8)) {
            writer.write("product_name,product_url,store");
            writer.newLine();
            for (Product p : products) {
                String name = p.getName();
                if (name.contains(",") || name.contains("\"")) {
                    name = "\"" + name.replace("\"", "\"\"") + "\"";
                }
                String store = p.getStore() != null ? p.getStore() : "AMAZON";
                writer.write(name + "," + p.getUrl() + "," + store);
                writer.newLine();
            }
        }
    }
}
