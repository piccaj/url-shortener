package com.example.shortener.controller;

import com.example.shortener.service.ShortUrlService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedirectController {

    private final ShortUrlService service;

    public RedirectController(ShortUrlService service) {
        this.service = service;
    }

    // Constrained to the exact slug format so this catch-all doesn't shadow
    // static resources (e.g. /index.html, /favicon.ico), which would otherwise
    // be routed here and 404 as unknown slugs.
    @GetMapping("/{slug:[0-9a-zA-Z]{7}}")
    public ResponseEntity<?> redirect(@PathVariable("slug") String slug) {
        return service.resolve(slug)
                .<ResponseEntity<?>>map(url -> ResponseEntity.status(HttpStatus.FOUND)
                        .location(java.net.URI.create(url.getOriginalUrl()))
                        .build())
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "slug not found")));
    }
}
