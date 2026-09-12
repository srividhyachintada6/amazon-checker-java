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

                // product_url may itself contain commas (query params), so split only on the first comma
                int firstComma = line.indexOf(',');
                if (firstComma == -1) continue;

                String name = line.substring(0, firstComma).trim();
                String url = line.substring(firstComma + 1).trim();

                products.add(new Product(name, url));
            }
        }
        return products;
    }

    public static void writeProducts(List<Product> products, String csvFile) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(csvFile), StandardCharsets.UTF_8)) {
            writer.write("product_name,product_url");
            writer.newLine();
            for (Product p : products) {
                writer.write(p.getName() + "," + p.getUrl());
                writer.newLine();
            }
        }
    }
}
