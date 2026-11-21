package com.kharid.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** Talks to the Python AI/ML service. */
@Service
@SuppressWarnings("unchecked")
public class MlService {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public MlService(RestTemplate restTemplate, @Value("${ml.service.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public Map<String, Object> analyze(String query, List<Map<String, Object>> listings, Map<String, Object> profile) {
        Map<String, Object> body = new HashMap<>();
        body.put("query", query);
        body.put("listings", listings);
        body.put("profile", profile);
        return restTemplate.postForObject(baseUrl + "/ml/analyze", body, Map.class);
    }

    public Map<String, Object> assistant(String message, List<Map<String, Object>> listings, Map<String, Object> profile) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", message);
        body.put("listings", listings);
        body.put("profile", profile);
        return restTemplate.postForObject(baseUrl + "/ml/assistant", body, Map.class);
    }

    public List<Map<String, Object>> recommend(List<Map<String, Object>> listings, List<String> brands, List<String> categories) {
        Map<String, Object> body = new HashMap<>();
        body.put("listings", listings);
        body.put("brands", brands);
        body.put("categories", categories);
        return restTemplate.postForObject(baseUrl + "/ml/recommend", body, List.class);
    }

    public List<Map<String, Object>> catalogue(List<Map<String, Object>> listings) {
        Map<String, Object> body = new HashMap<>();
        body.put("listings", listings);
        return restTemplate.postForObject(baseUrl + "/ml/catalogue", body, List.class);
    }

    public Map<String, Object> imageSearch(byte[] bytes, String filename, String category, Integer budget, String listingsJson) {
        final String name = (filename == null || filename.isBlank()) ? "upload.png" : filename;
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("file", new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return name;
            }
        });
        form.add("listings", listingsJson);
        form.add("category", category == null ? "" : category);
        form.add("budget", String.valueOf(budget == null ? 0 : budget));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return restTemplate.postForObject(baseUrl + "/ml/image-search", new HttpEntity<>(form, headers), Map.class);
    }

    public List<Map<String, Object>> sampleListings() {
        return restTemplate.getForObject(baseUrl + "/ml/sample-listings", List.class);
    }

    public boolean isUp() {
        try {
            restTemplate.getForObject(baseUrl + "/ml/health", Map.class);
            return true;
        } catch (RestClientException e) {
            return false;
        }
    }
}
