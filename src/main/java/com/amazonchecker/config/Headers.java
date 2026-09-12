package com.amazonchecker.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Equivalent of config/headers.py
 * Holds the HTTP headers used for every outgoing request.
 */
public class Headers {

    public static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/122.0.0.0 Safari/537.36";

    public static Map<String, String> get() {

        Map<String, String> headers = new LinkedHashMap<>();

        headers.put("User-Agent", USER_AGENT);
        headers.put("Accept-Language", "en-IN,en;q=0.9");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Connection", "keep-alive");

        return headers;
    }
}