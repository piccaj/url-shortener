package com.example.shortener.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shortener.model.ShortUrl;
import com.example.shortener.repository.ShortUrlRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ShortUrlServiceTest {

    @Autowired
    private ShortUrlService service;

    @Autowired
    private ShortUrlRepository repository;

    @Test
    void shortenCreatesSlugAndPersistsUrl() {
        ShortUrl saved = service.shorten("https://example.com/some/long/path");

        assertThat(saved.getSlug()).hasSize(7).matches("[0-9a-zA-Z]{7}");
        assertThat(saved.getOriginalUrl()).isEqualTo("https://example.com/some/long/path");
        assertThat(repository.findBySlug(saved.getSlug())).isPresent();
    }

    @Test
    void resolveIncrementsVisitCount() {
        ShortUrl saved = service.shorten("https://example.com/visits");

        Optional<ShortUrl> first = service.resolve(saved.getSlug());
        Optional<ShortUrl> second = service.resolve(saved.getSlug());

        assertThat(first).isPresent();
        assertThat(second).isPresent();
        assertThat(repository.findBySlug(saved.getSlug()).orElseThrow().getVisitCount()).isEqualTo(2);
    }

    @Test
    void resolveUnknownSlugReturnsEmpty() {
        assertThat(service.resolve("zzzzzzz")).isEmpty();
    }

    @Test
    void findMostVisitedReturnsMostPopular() {
        ShortUrl a = service.shorten("https://example.com/a");
        ShortUrl b = service.shorten("https://example.com/b");

        for (int i = 0; i < 3; i++) {
            service.resolve(a.getSlug());
        }
        service.resolve(b.getSlug());

        assertThat(service.findMostVisited()).map(ShortUrl::getOriginalUrl).contains("https://example.com/a");
    }
}
