# Elasticsearch Mini Book Search Engine

A practical Java project demonstrating full-text search, filtering, and faceted aggregation using Elasticsearch 8.x and the official Java API client.

## Features
- Bulk indexing of book data (title, author, summary, genres, etc.)
- Full-text search with filters (author, genre)
- Faceted aggregation (top genres)
- Partial update and delete operations
- Clean, modern Java code (Maven project)

## Requirements
- Java 11+
- Maven 3.6+
- Docker (for running Elasticsearch)

## Quick Start

### 1. Run Elasticsearch in Docker
```
docker network create elastic-net

docker run -d --name es-dev --net elastic-net -p 9200:9200 -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  -e "ES_JAVA_OPTS=-Xms1g -Xmx1g" \
  docker.elastic.co/elasticsearch/elasticsearch:8.9.0
```

Check that ES is running:
```
curl http://localhost:9200/
```

### 2. Build and Run the Java Project
```
mvn clean compile exec:java -Dexec.mainClass="com.example.BookSearchApplication"
```

### 3. What Happens
- Generates a sample dataset of books (with diverse authors and genres)
- Creates the `books` index with custom mapping
- Bulk indexes all books
- Runs a sample search and prints results
- Runs a sample aggregation (top 5 genres) and prints results

## Project Structure
- `src/main/java/com/example/model/Book.java` — Book data model
- `src/main/java/com/example/service/ElasticsearchService.java` — All ES operations (index, search, agg, update, delete)
- `src/main/java/com/example/BookSearchApplication.java` — Main entry point
- `docs/mapping.md` — Mapping explanation
- `docs/lucene_comparison.md` — Lucene vs Elasticsearch comparison

## Key Endpoints (in code)
- `searchBooks(String q, String author, String genre)` — Full-text search with filters
- `topGenresAggregation(...)` — Faceted aggregation (top genres)
- `addGenreToBook(...)` — Partial update
- `deleteBookById(...)` — Delete by ID

## Notes
- Uses the official [elasticsearch-java](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/index.html) client (8.x)
- All code and comments are in English
- No cloud deployment included (local only)

## License
MIT 