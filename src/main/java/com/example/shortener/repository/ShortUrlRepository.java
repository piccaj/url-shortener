package com.example.shortener.repository;

import com.example.shortener.model.ShortUrl;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Optional<ShortUrl> findTopBySlugNotNullOrderByVisitCountDesc();

    List<ShortUrl> findTop50ByOrderByCreatedAtDesc();
}
