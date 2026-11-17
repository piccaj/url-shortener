package com.example.shortener.service;

import com.example.shortener.model.ShortUrl;
import com.example.shortener.repository.ShortUrlRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShortUrlService {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int SLUG_LENGTH = 7;

    private final ShortUrlRepository repository;
    private final SecureRandom random = new SecureRandom();

    public ShortUrlService(ShortUrlRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ShortUrl shorten(String originalUrl) {
        String slug = generateUniqueSlug();
        ShortUrl shortUrl = new ShortUrl(slug, originalUrl, Instant.now().toEpochMilli());
        return repository.save(shortUrl);
    }

    @Transactional
    public Optional<ShortUrl> resolve(String slug) {
        return repository.findBySlug(slug).map(url -> {
            url.setVisitCount(url.getVisitCount() + 1);
            return url;
        });
    }

    public Optional<ShortUrl> findBySlug(String slug) {
        return repository.findBySlug(slug);
    }

    public Optional<ShortUrl> findMostVisited() {
        return repository.findTopBySlugNotNullOrderByVisitCountDesc();
    }

    private String generateUniqueSlug() {
        String slug;
        do {
            slug = randomSlug();
        } while (repository.existsBySlug(slug));
        return slug;
    }

    private String randomSlug() {
        StringBuilder sb = new StringBuilder(SLUG_LENGTH);
        for (int i = 0; i < SLUG_LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
