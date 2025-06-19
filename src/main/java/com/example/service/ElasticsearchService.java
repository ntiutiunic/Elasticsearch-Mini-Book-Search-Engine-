package com.example.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.*;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.example.config.ElasticsearchClientConfig;
import com.example.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.SourceConfig;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import java.util.LinkedHashMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.DeleteResponse;

public class ElasticsearchService {
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchService.class);
    private static final String INDEX_NAME = "books";
    private final ElasticsearchClient client;

    public ElasticsearchService() {
        this.client = ElasticsearchClientConfig.getClient();
    }

    public void createIndexWithMapping() throws IOException {
        // Проверяем, существует ли индекс
        boolean exists = client.indices().exists(e -> e.index(INDEX_NAME)).value();
        if (exists) {
            logger.info("Index '{}' already exists", INDEX_NAME);
            return;
        }

        // Описываем маппинг
        TypeMapping mapping = new TypeMapping.Builder()
            .properties("title", new Property.Builder()
                .text(t -> t.fields("keyword", new Property.Builder().keyword(k -> k.ignoreAbove(256)).build()))
                .build())
            .properties("author", new Property.Builder()
                .text(t -> t.fields("keyword", new Property.Builder().keyword(k -> k.ignoreAbove(256)).build()))
                .build())
            .properties("summary", new Property.Builder().text(t -> t).build())
            .properties("genres", new Property.Builder().keyword(k -> k).build())
            .properties("year", new Property.Builder().integer(i -> i).build())
            .properties("publisher", new Property.Builder().keyword(k -> k).build())
            .build();

        client.indices().create(new CreateIndexRequest.Builder()
            .index(INDEX_NAME)
            .mappings(mapping)
            .settings(s -> s.numberOfShards("1").numberOfReplicas("0"))
            .build());
        logger.info("Created index '{}' with mapping", INDEX_NAME);
    }

    public void bulkIndexBooks(List<Book> books) throws IOException {
        List<BulkOperation> operations = new ArrayList<>();
        for (Book book : books) {
            Map<String, Object> document = new HashMap<>();
            document.put("title", book.getTitle());
            document.put("author", book.getAuthor());
            document.put("summary", book.getSummary());
            document.put("genres", book.getGenres());
            if (book.getYear() != null) document.put("year", book.getYear());
            if (book.getPublisher() != null) document.put("publisher", book.getPublisher());

            operations.add(BulkOperation.of(b -> b
                .index(idx -> idx
                    .index(INDEX_NAME)
                    .id(book.getId())
                    .document(document)
                )
            ));
        }
        BulkResponse response = client.bulk(new BulkRequest.Builder().operations(operations).build());
        if (response.errors()) {
            logger.error("Bulk indexing has failures");
            response.items().stream().filter(i -> i.error() != null).forEach(i ->
                logger.error(i.error().reason())
            );
        } else {
            logger.info("Successfully indexed {} books", books.size());
        }
    }

    public long getDocumentCount() throws IOException {
        CountResponse response = client.count(new CountRequest.Builder().index(INDEX_NAME).build());
        return response.count();
    }

    public List<Book> searchBooks(String q, String author, String genre) throws IOException {
        List<Query> must = new ArrayList<>();
        List<Query> filter = new ArrayList<>();

        if (q != null && !q.isEmpty()) {
            must.add(Query.of(m -> m.multiMatch(MultiMatchQuery.of(mm -> mm
                .fields("title", "summary")
                .query(q)
            ))));
        }
        if (author != null && !author.isEmpty()) {
            filter.add(Query.of(f -> f.term(TermQuery.of(t -> t
                .field("author.keyword")
                .value(author)
            ))));
        }
        if (genre != null && !genre.isEmpty()) {
            filter.add(Query.of(f -> f.term(TermQuery.of(t -> t
                .field("genres")
                .value(genre)
            ))));
        }

        Query finalQuery = Query.of(qb -> qb.bool(BoolQuery.of(b -> b
            .must(must)
            .filter(filter)
        )));

        SearchRequest request = new SearchRequest.Builder()
            .index(INDEX_NAME)
            .query(finalQuery)
            .size(10)
            .source(SourceConfig.of(s -> s.filter(f -> f.includes("title", "author", "summary"))))
            .build();

        SearchResponse<Book> response = client.search(request, Book.class);
        List<Book> result = new ArrayList<>();
        for (Hit<Book> hit : response.hits().hits()) {
            Book book = hit.source();
            if (book != null) {
                book.setId(hit.id());
                result.add(book);
            }
        }
        return result;
    }

    public Map<String, Long> topGenresAggregation(String q, String author, String genre) throws IOException {
        List<Query> must = new ArrayList<>();
        List<Query> filter = new ArrayList<>();

        if (q != null && !q.isEmpty()) {
            must.add(Query.of(m -> m.multiMatch(MultiMatchQuery.of(mm -> mm
                .fields("title", "summary")
                .query(q)
            ))));
        }
        if (author != null && !author.isEmpty()) {
            filter.add(Query.of(f -> f.term(TermQuery.of(t -> t
                .field("author.keyword")
                .value(author)
            ))));
        }
        if (genre != null && !genre.isEmpty()) {
            filter.add(Query.of(f -> f.term(TermQuery.of(t -> t
                .field("genres")
                .value(genre)
            ))));
        }

        Query finalQuery = Query.of(qb -> qb.bool(BoolQuery.of(b -> b
            .must(must)
            .filter(filter)
        )));

        Aggregation genresAgg = Aggregation.of(a -> a.terms(t -> t.field("genres").size(5)));

        SearchRequest request = new SearchRequest.Builder()
            .index(INDEX_NAME)
            .query(finalQuery)
            .size(0)
            .aggregations("top_genres", genresAgg)
            .build();

        SearchResponse<Void> response = client.search(request, Void.class);
        Aggregate agg = response.aggregations().get("top_genres");
        Map<String, Long> result = new LinkedHashMap<>();
        if (agg != null && agg.isSterms()) {
            for (StringTermsBucket b : agg.sterms().buckets().array()) {
                result.put(b.key().stringValue(), b.docCount());
            }
        }
        return result;
    }

    public void addGenreToBook(String bookId, String newGenre) throws IOException {
        // Получаем текущий документ
        Book book = client.get(g -> g.index(INDEX_NAME).id(bookId), Book.class).source();
        if (book == null) {
            logger.warn("Book with id {} not found", bookId);
            return;
        }
        List<String> genres = new ArrayList<>(book.getGenres() != null ? book.getGenres() : List.of());
        if (!genres.contains(newGenre)) {
            genres.add(newGenre);
        }
        Map<String, Object> update = Map.of("genres", genres);
        UpdateRequest<Book, Map<String, Object>> request = UpdateRequest.of(u -> u
            .index(INDEX_NAME)
            .id(bookId)
            .doc(update)
        );
        UpdateResponse<Book> response = client.update(request, Book.class);
        logger.info("Updated book {}: genres now {}", bookId, genres);
    }

    public void deleteBookById(String bookId) throws IOException {
        DeleteRequest request = DeleteRequest.of(d -> d.index(INDEX_NAME).id(bookId));
        DeleteResponse response = client.delete(request);
        logger.info("Deleted book with id {}. Result: {}", bookId, response.result());
    }
} 