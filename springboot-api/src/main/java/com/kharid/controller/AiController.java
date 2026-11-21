package com.kharid.controller;

import com.kharid.dto.AssistantRequest;
import com.kharid.entity.AppUser;
import com.kharid.entity.SearchLog;
import com.kharid.repository.SearchLogRepository;
import com.kharid.service.ListingService;
import com.kharid.service.MlService;
import com.kharid.service.UserService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** AI shopping assistant, image search and personalised recommendations. */
@RestController
@RequestMapping("/api")
public class AiController {

    private final MlService ml;
    private final ListingService listingService;
    private final UserService users;
    private final SearchLogRepository logs;

    public AiController(MlService ml, ListingService listingService, UserService users, SearchLogRepository logs) {
        this.ml = ml;
        this.listingService = listingService;
        this.users = users;
        this.logs = logs;
    }

    @PostMapping("/assistant")
    public Map<String, Object> assistant(@Valid @RequestBody AssistantRequest request) {
        listingService.ensureSeeded();
        return ml.assistant(request.message().trim(), listingService.payload(), users.mlProfile(users.current()));
    }

    @PostMapping(value = "/image-search", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> imageSearch(@RequestParam("file") MultipartFile file,
                                           @RequestParam(value = "category", required = false) String category,
                                           @RequestParam(value = "budget", required = false) Integer budget) throws IOException {
        listingService.ensureSeeded();
        return ml.imageSearch(file.getBytes(), file.getOriginalFilename(), category, budget, listingService.payloadJson());
    }

    @GetMapping("/recommendations")
    public List<Map<String, Object>> recommendations() {
        AppUser u = users.current();
        List<String> categories = u == null ? List.of()
                : logs.findTop10ByUserIdOrderByIdDesc(u.getId()).stream()
                        .map(SearchLog::getCategory).filter(Objects::nonNull).toList();
        listingService.ensureSeeded();
        return ml.recommend(listingService.payload(), users.brandsOf(u), categories);
    }
}
