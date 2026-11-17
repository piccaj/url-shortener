package com.example.shortener.controller;

import com.example.shortener.model.ShortUrl;
import com.example.shortener.service.ShortUrlService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api")
public class UrlShortenerController {

    private final ShortUrlService service;

    public UrlShortenerController(ShortUrlService service) {
        this.service = service;
    }

    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest request) {
        ShortUrl shortUrl = service.shorten(request.url());
        return ResponseEntity.ok(ShortenResponse.of(shortUrl, currentBaseUrl()));
    }

    @GetMapping("/urls/{slug}")
    public ResponseEntity<?> getStats(@PathVariable String slug) {
        return service.findBySlug(slug)
                .<ResponseEntity<?>>map(url -> ResponseEntity.ok(ShortenResponse.of(url, currentBaseUrl())))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "slug not found")));
    }

    @GetMapping("/urls/top")
    public ResponseEntity<?> top() {
        return service.findMostVisited()
                .<ResponseEntity<?>>map(url -> ResponseEntity.ok(ShortenResponse.of(url, currentBaseUrl())))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "no urls shortened yet")));
    }

    private String currentBaseUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    }
}
