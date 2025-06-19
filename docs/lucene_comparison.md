# Lucene vs Elasticsearch: Implementation Comparison

## How to implement steps 4–7 with pure Lucene

- **Indexing (1000 books):**
  - Use `IndexWriter` to write documents.
  - For each book, create a `Document` and add fields (`TextField` for title/summary, `StringField` for genres/author).
  - Specify the required `Analyzer` (e.g., StandardAnalyzer) explicitly for text analysis.

- **Search and filtering:**
  - Build a `BooleanQuery` with multiple conditions (`TermQuery` for filters, `MultiFieldQueryParser` for searching multiple fields).
  - For filtering by genre/author — use separate `TermQuery` for the required fields.

- **Aggregations (facets):**
  - Lucene does not have built-in aggregations. You need to manually iterate over search results and count by genre (or use third-party libraries, e.g., BoboBrowse).

## Key differences and challenges: Lucene vs Elasticsearch

1. **API and convenience:**
   - Lucene is low-level: you must manage the index, analyzers, schema, and aggregations manually.
   - Elasticsearch provides REST/Java APIs, ready-to-use aggregations, filters, and mapping settings.

2. **Scalability and fault tolerance:**
   - Lucene is a local index only, no clustering.
   - Elasticsearch is distributed, supports clusters, replicas, and sharding.

3. **Aggregations and facets:**
   - Lucene has no built-in aggregations, you must implement them yourself.
   - Elasticsearch: just one line in the query.

4. **Updates and deletes:**
   - In Lucene, update/delete is actually deleting the old document and adding a new one.
   - In Elasticsearch, there are convenient APIs for partial update, delete by query, etc.

5. **DevOps and operations:**
   - Lucene is a library, everything is on your side.
   - Elasticsearch is a service, with monitoring, REST API, Kibana, etc. 