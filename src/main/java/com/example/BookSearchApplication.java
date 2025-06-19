package com.example;

import com.example.model.Book;
import com.example.service.ElasticsearchService;
import com.example.util.DatasetDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class BookSearchApplication {
    private static final Logger logger = LoggerFactory.getLogger(BookSearchApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Book Search Application");
        
        try {
            // Step 1: Data Preparation
            logger.info("Step 1: Data Preparation");
            DatasetDownloader.downloadDataset();
            List<Book> books = DatasetDownloader.processDataset();
            logger.info("Data preparation completed. Processed {} books", books.size());

            // Step 4: Create Index with Mapping
            logger.info("Step 4: Creating Elasticsearch Index with Mapping");
            ElasticsearchService elasticsearchService = new ElasticsearchService();
            elasticsearchService.createIndexWithMapping();
            
            // Step 5: Bulk Index Data
            logger.info("Step 5: Bulk Indexing Books");
            elasticsearchService.bulkIndexBooks(books);
            long count = elasticsearchService.getDocumentCount();
            logger.info("Indexing completed. Total documents in index: {}", count);
            
            // Sample search
            logger.info("Sample search: books with 'Harry' in title or summary, genre = 'Fantasy'");
            List<Book> found = elasticsearchService.searchBooks("Harry", null, "Fantasy");
            for (Book b : found) {
                System.out.printf("- %s by %s [%s]\n", b.getTitle(), b.getAuthor(),
                    b.getGenres() != null ? String.join(", ", b.getGenres()) : "no genres");
                System.out.printf("  Summary: %s\n", b.getSummary());
            }

            // Demo: update genre for the first found book
            if (!found.isEmpty()) {
                Book bookToUpdate = found.get(0);
                elasticsearchService.addGenreToBook(bookToUpdate.getId(), "DemoGenre");
                System.out.printf("Updated book %s: added genre 'DemoGenre'\n", bookToUpdate.getTitle());
            }

            // Demo: delete the first found book
            if (!found.isEmpty()) {
                Book bookToDelete = found.get(0);
                elasticsearchService.deleteBookById(bookToDelete.getId());
                System.out.printf("Deleted book %s\n", bookToDelete.getTitle());
            }

            // Sample aggregation
            logger.info("Sample aggregation: top 5 genres");
            Map<String, Long> topGenres = elasticsearchService.topGenresAggregation(null, null, null);
            System.out.println("Top 5 genres:");
            topGenres.forEach((genre, cnt) -> System.out.printf("- %s: %d\n", genre, cnt));
        } catch (IOException e) {
            logger.error("Application error", e);
            System.exit(1);
        }
    }
} 