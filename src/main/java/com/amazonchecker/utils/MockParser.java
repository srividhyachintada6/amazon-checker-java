package com.amazonchecker.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;

/**
 * Equivalent of utils/mock_parser.py
 * Parses a local HTML file instead of hitting the network - used for offline demos/tests.
 */
public class MockParser {

    public static Document parseMockHtml(String filePath) throws IOException {
        File input = new File(filePath);
        return Jsoup.parse(input, "UTF-8");
    }
}
