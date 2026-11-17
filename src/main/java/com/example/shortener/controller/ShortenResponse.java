package com.example.shortener.controller;

import com.example.shortener.model.ShortUrl;

public record ShortenResponse(String slug, String shortUrl, String originalUrl, long visitCount) {

    public static ShortenResponse of(ShortUrl url, String baseUrl) {
        String shortUrl = baseUrl + "/" + url.getSlug();
        return new ShortenResponse(url.getSlug(), shortUrl, url.getOriginalUrl(), url.getVisitCount());
    }
}
