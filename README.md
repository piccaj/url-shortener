# URL Shortener

[![CI](https://github.com/piccaj/url-shortener/actions/workflows/ci.yml/badge.svg)](https://github.com/piccaj/url-shortener/actions/workflows/ci.yml)

A URL shortener web app built with Spring Boot, JPA, and an in-memory H2 database — REST API **and** a bundled web UI.

## Features

- **Web UI** at `/` — paste a URL, get a short link with copy/open buttons, plus a live table of recent links and click counts
- `POST /api/shorten` — create a short URL from any `http`/`https` URL
- `GET /{slug}` — 302 redirect to the original URL
- `GET /api/urls/{slug}` — view stats (original URL, visit count)
- `GET /api/urls/recent?limit=N` — recently created links
- `GET /api/urls/top` — the most-visited short URL
- Input validation on the `url` field (blank / non-http values rejected with 400 + readable error)

## Quick start

Requires Java 21 and Maven.

```bash
mvn spring-boot:run
```

Then open **http://localhost:8080** — the web UI is served by the same Spring Boot app, no separate frontend build required.

## Usage

Create a short URL:

```bash
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com/some/very/long/address"}'
```

Response:

```json
{
  "slug": "aB3xK9p",
  "shortUrl": "http://localhost:8080/aB3xK9p",
  "originalUrl": "https://example.com/some/very/long/address",
  "visitCount": 0
}
```

Follow it (note the 302 and `Location` header):

```bash
curl -i http://localhost:8080/aB3xK9p
```

Check its stats:

```bash
curl http://localhost:8080/api/urls/aB3xK9p
```

See the most-visited URL:

```bash
curl http://localhost:8080/api/urls/top
```

## Running tests

```bash
mvn verify
```

Runs the unit tests plus a full-stack API integration test suite (service tests, HTTP endpoint tests, redirect behavior, validation).

## Project structure

```
src/main/java/com/example/shortener/
├── UrlShortenerApplication.java   # entry point
├── controller/                    # REST endpoints + DTOs
├── exception/                     # validation error → readable JSON
├── model/                         # JPA entity
├── repository/                    # Spring Data JPA repository
└── service/                       # slug generation + business logic
src/main/resources/
├── application.properties         # config (H2 datasource, port)
└── static/index.html              # web UI — vanilla HTML/CSS/JS, no build step
```

## Deploying (Docker)

The repo ships a multi-stage `Dockerfile` (Maven build → slim JRE runtime, runs as non-root), so it deploys to any container host. The app reads the port from `$PORT` (falls back to `8080`), which is what hosts like Render pass in.

```bash
docker build -t url-shortener .
docker run -p 8080:8080 url-shortener
```

To deploy on [Render](https://render.com): **New → Web Service →** connect this repo → it detects the Dockerfile and builds it. No start command needed (the Dockerfile's ENTRYPOINT handles it). Free-tier instances sleep when idle — the first request after a pause may take ~1 min while the JVM boots.

## Notes

- The H2 database is in-memory, so data resets on restart. To use a real database, swap the datasource in `src/main/resources/application.properties` for MySQL/Postgres and add the appropriate driver dependency.
- Slugs are 7 random alphanumeric characters (case-sensitive), generated with `SecureRandom` and checked for collisions before saving.
