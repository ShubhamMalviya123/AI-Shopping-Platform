package com.kharid.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kharid.entity.Listing;
import com.kharid.entity.PricePoint;
import com.kharid.entity.Review;
import com.kharid.repository.ListingRepository;
import com.kharid.repository.PricePointRepository;
import com.kharid.repository.ReviewRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/** Reads and writes marketplace listings, reviews and price history in MySQL. */
@Service
public class ListingService {

    private final ListingRepository listings;
    private final ReviewRepository reviews;
    private final PricePointRepository pricePoints;
    private final MlService ml;
    private final ObjectMapper mapper;

    public ListingService(ListingRepository listings, ReviewRepository reviews, PricePointRepository pricePoints,
                          MlService ml, ObjectMapper mapper) {
        this.listings = listings;
        this.reviews = reviews;
        this.pricePoints = pricePoints;
        this.ml = ml;
        this.mapper = mapper;
    }

    /** First run: store the sample catalogue in MySQL. Replace this with real marketplace/affiliate API imports later. */
    public synchronized void ensureSeeded() {
        if (listings.count() > 0) {
            return;
        }
        for (Map<String, Object> m : ml.sampleListings()) {
            saveListing(m);
        }
    }

    @SuppressWarnings("unchecked")
    private void saveListing(Map<String, Object> m) {
        Listing l = new Listing();
        l.setExternalId((String) m.get("id"));
        l.setMarketplace((String) m.get("marketplace"));
        l.setTitle((String) m.get("title"));
        l.setBrand((String) m.get("brand"));
        l.setCategory((String) m.get("cat"));
        l.setPrice(((Number) m.get("price")).intValue());
        l.setRating(((Number) m.get("rating")).doubleValue());
        l.setRatingCount(((Number) m.get("rating_count")).intValue());
        Object ram = m.get("ram");
        l.setRam(ram instanceof Number n ? Integer.valueOf(n.intValue()) : null);
        l.setUrl((String) m.get("url"));
        try {
            l.setSpecJson(mapper.writeValueAsString(m.get("spec")));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not store specs", e);
        }
        Listing saved = listings.save(l);

        List<Review> reviewRows = new ArrayList<>();
        for (Object text : (List<Object>) m.get("reviews")) {
            Review r = new Review();
            r.setListingId(saved.getId());
            r.setText(text.toString());
            reviewRows.add(r);
        }
        reviews.saveAll(reviewRows);

        List<PricePoint> points = new ArrayList<>();
        List<Object> history = (List<Object>) m.get("history");
        for (int i = 0; i < history.size(); i++) {
            PricePoint p = new PricePoint();
            p.setListingId(saved.getId());
            p.setMonthIndex(i);
            p.setPrice(((Number) history.get(i)).intValue());
            points.add(p);
        }
        pricePoints.saveAll(points);
    }

    /** Builds the payload the Python AI service expects, from MySQL rows. */
    public List<Map<String, Object>> payload() {
        Map<Long, List<String>> reviewsByListing = reviews.findAll().stream()
                .collect(Collectors.groupingBy(Review::getListingId,
                        Collectors.mapping(Review::getText, Collectors.toList())));
        Map<Long, List<Integer>> historyByListing = pricePoints.findAll(Sort.by("monthIndex")).stream()
                .collect(Collectors.groupingBy(PricePoint::getListingId,
                        Collectors.mapping(PricePoint::getPrice, Collectors.toList())));

        List<Map<String, Object>> out = new ArrayList<>();
        for (Listing l : listings.findAll()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", l.getExternalId());
            m.put("marketplace", l.getMarketplace());
            m.put("title", l.getTitle());
            m.put("brand", l.getBrand());
            m.put("cat", l.getCategory());
            m.put("price", l.getPrice());
            m.put("rating", l.getRating());
            m.put("rating_count", l.getRatingCount());
            m.put("ram", l.getRam());
            m.put("url", l.getUrl());
            m.put("spec", readSpec(l.getSpecJson()));
            m.put("reviews", reviewsByListing.getOrDefault(l.getId(), List.of()));
            m.put("history", historyByListing.getOrDefault(l.getId(), List.of()));
            out.add(m);
        }
        return out;
    }

    private Map<String, Object> readSpec(String json) {
        try {
            return mapper.readValue(json, new TypeReference<Map<String, Object>>() { });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not read specs", e);
        }
    }

    public String payloadJson() {
        try {
            return mapper.writeValueAsString(payload());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialise listings", e);
        }
    }

    /** Demo helper for admins: drops a few prices so that price alerts can fire. */
    public List<String> simulateDrop() {
        List<Listing> all = new ArrayList<>(listings.findAll());
        Collections.shuffle(all);
        List<PricePoint> allPoints = pricePoints.findAll();
        List<String> changes = new ArrayList<>();
        for (Listing l : all.subList(0, Math.min(4, all.size()))) {
            int oldPrice = l.getPrice();
            int newPrice = (int) Math.round(oldPrice * (0.88 + Math.random() * 0.06));
            int next = allPoints.stream().filter(p -> p.getListingId().equals(l.getId()))
                    .mapToInt(PricePoint::getMonthIndex).max().orElse(-1) + 1;
            l.setPrice(newPrice);
            listings.save(l);
            PricePoint point = new PricePoint();
            point.setListingId(l.getId());
            point.setMonthIndex(next);
            point.setPrice(newPrice);
            pricePoints.save(point);
            changes.add(l.getTitle() + " (" + l.getMarketplace() + "): ₹" + oldPrice + " to ₹" + newPrice);
        }
        return changes;
    }
}
