package com.college.library.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class IsbnLookupService {

    private static final Logger log = LoggerFactory.getLogger(IsbnLookupService.class);
    private final HttpClient httpClient;

    public IsbnLookupService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(4))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public Map<String, String> lookupByIsbn(String rawIsbn) {
        Map<String, String> result = new HashMap<>();
        if (rawIsbn == null || rawIsbn.isBlank()) {
            return result;
        }

        String isbn = rawIsbn.replaceAll("[^0-9X-x]", "");
        if (isbn.isBlank()) {
            return result;
        }

        // Try OpenLibrary first
        try {
            String url = "https://openlibrary.org/isbn/" + isbn + ".json";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .header("User-Agent", "DigitalLibrarySystem/1.0 (Enterprise College Library)")
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body() != null && !response.body().isBlank()) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                if (json.has("title")) {
                    result.put("title", json.get("title").getAsString());
                }
                if (json.has("description")) {
                    JsonElement descElem = json.get("description");
                    if (descElem.isJsonObject() && descElem.getAsJsonObject().has("value")) {
                        result.put("description", descElem.getAsJsonObject().get("value").getAsString());
                    } else if (descElem.isJsonPrimitive()) {
                        result.put("description", descElem.getAsString());
                    }
                }
                if (json.has("publishers") && json.get("publishers").isJsonArray()) {
                    JsonArray pubArr = json.getAsJsonArray("publishers");
                    if (pubArr.size() > 0) {
                        result.put("publisher", pubArr.get(0).getAsString());
                    }
                }
                if (json.has("by_statement")) {
                    result.put("authors", json.get("by_statement").getAsString());
                }
                if (json.has("number_of_pages")) {
                    result.put("edition", json.get("number_of_pages").getAsString() + " pages");
                }
                result.put("isbn", isbn);
                return result;
            }
        } catch (Exception e) {
            log.warn("OpenLibrary lookup failed for ISBN {}: {}", isbn, e.getMessage());
        }

        // Fallback to Google Books API
        try {
            String url = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .header("User-Agent", "DigitalLibrarySystem/1.0 (Enterprise College Library)")
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body() != null) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                if (json.has("items") && json.getAsJsonArray("items").size() > 0) {
                    JsonObject volumeInfo = json.getAsJsonArray("items").get(0).getAsJsonObject().getAsJsonObject("volumeInfo");
                    if (volumeInfo != null) {
                        if (volumeInfo.has("title")) {
                            result.put("title", volumeInfo.get("title").getAsString());
                        }
                        if (volumeInfo.has("description")) {
                            result.put("description", volumeInfo.get("description").getAsString());
                        }
                        if (volumeInfo.has("publisher")) {
                            result.put("publisher", volumeInfo.get("publisher").getAsString());
                        }
                        if (volumeInfo.has("authors") && volumeInfo.get("authors").isJsonArray()) {
                            StringBuilder authors = new StringBuilder();
                            for (JsonElement a : volumeInfo.getAsJsonArray("authors")) {
                                if (authors.length() > 0) authors.append(", ");
                                authors.append(a.getAsString());
                            }
                            result.put("authors", authors.toString());
                        }
                        result.put("isbn", isbn);
                        return result;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Google Books lookup fallback failed for ISBN {}: {}", isbn, e.getMessage());
        }

        return result;
    }
}
