package com.example.shortener;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UrlShortenerApiTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Test
    void shortenEndpointReturnsSlugAndShortUrl() {
        ResponseEntity<Map> response = restTemplate.postForEntity("/api/shorten", body("https://example.com"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat((String) response.getBody().get("slug")).matches("[0-9a-zA-Z]{7}");
        assertThat((String) response.getBody().get("shortUrl")).endsWith("/" + response.getBody().get("slug"));
        assertThat((String) response.getBody().get("originalUrl")).isEqualTo("https://example.com");
    }

    @Test
    void shortenRejectsNonHttpUrl() {
        ResponseEntity<Map> response = restTemplate.postForEntity("/api/shorten", body("ftp://example.com"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shortenRejectsBlankUrl() {
        ResponseEntity<Map> response = restTemplate.postForEntity("/api/shorten", body(""), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void unknownSlugReturns404() {
        ResponseEntity<String> response = restTemplate.getForEntity("/doesnotexist", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void statsEndpointReturnsVisitCount() {
        ResponseEntity<Map> shortened = restTemplate.postForEntity("/api/shorten", body("https://example.com/stats"), Map.class);
        String slug = (String) shortened.getBody().get("slug");

        restTemplate.getForEntity("/" + slug, String.class);
        restTemplate.getForEntity("/" + slug, String.class);

        ResponseEntity<Map> stats = restTemplate.getForEntity("/api/urls/" + slug, Map.class);
        assertThat(stats.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Integer) stats.getBody().get("visitCount")).isEqualTo(2);
    }

    @Test
    void redirectEndpointReturns302WithLocation() throws Exception {
        ResponseEntity<Map> shortened = restTemplate.postForEntity("/api/shorten", body("https://example.com/redirect"), Map.class);
        String slug = (String) shortened.getBody().get("slug");

        // TestRestTemplate follows redirects; use the JDK client with redirects disabled
        // so we can assert the raw 302 instead of example.com's response.
        HttpClient noFollow = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/" + slug)).build();
        HttpResponse<Void> redirect = noFollow.send(request, HttpResponse.BodyHandlers.discarding());

        assertThat(redirect.statusCode()).isEqualTo(302);
        assertThat(redirect.headers().firstValue("Location").orElseThrow()).isEqualTo("https://example.com/redirect");
    }

    private record Body(String url) {
    }

    private Body body(String url) {
        return new Body(url);
    }
}
