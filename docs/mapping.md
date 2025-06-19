# Elasticsearch Mapping Explanation

## Field Types

### `title` (text + keyword)
- **Type**: `text` with `keyword` subfield
- **Reason**: 
  - `text` allows full-text search within titles (e.g., searching for "Harry" should match "Harry Potter")
  - `keyword` subfield enables exact matching and sorting (e.g., sorting books alphabetically by title)

### `author` (text + keyword)
- **Type**: `text` with `keyword` subfield
- **Reason**:
  - `text` enables searching for partial author names (e.g., "Rowling" should match "J.K. Rowling")
  - `keyword` subfield allows exact filtering (e.g., show all books by exactly "J.K. Rowling")

### `summary` (text)
- **Type**: `text`
- **Reason**:
  - Only needs full-text search capability
  - No need for exact matching or aggregations on summaries
  - Allows for relevance scoring based on content

### `genres` (keyword)
- **Type**: `keyword`
- **Reason**:
  - Genres are predefined categories that should match exactly
  - Enables faceted search (counting books per genre)
  - No need for full-text search within genre names

### `year` (integer)
- **Type**: `integer`
- **Reason**:
  - Enables range queries (e.g., books published between 2000-2010)
  - Allows sorting by publication year
  - Supports aggregations (e.g., books per decade)

### `publisher` (keyword)
- **Type**: `keyword`
- **Reason**:
  - Publisher names should match exactly
  - Enables faceted search by publisher
  - No need for partial matching within publisher names

## Index Settings
- Single shard for development environment
- No replicas in single-node setup
- Can be adjusted for production with multiple nodes 