package com.example.util;

import com.example.model.Book;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class DatasetDownloader {
    private static final Logger logger = LoggerFactory.getLogger(DatasetDownloader.class);
    private static final String DATASET_PATH = "data/books.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void downloadDataset() {
        try {
            // Create data directory if it doesn't exist
            Path dataDir = Paths.get("data");
            if (!Files.exists(dataDir)) {
                Files.createDirectory(dataDir);
            }

            // Create sample dataset
            List<Book> books = createSampleDataset();
            
            // Write to file
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(DATASET_PATH), books);
            
            logger.info("Sample dataset created successfully at {}", DATASET_PATH);
        } catch (IOException e) {
            logger.error("Error creating dataset", e);
            throw new RuntimeException("Failed to create dataset", e);
        }
    }

    private static List<Book> createSampleDataset() {
        List<Book> books = new ArrayList<>();
        
        // Sample authors
        List<String> authors = Arrays.asList(
            "J.K. Rowling", "George R.R. Martin", "Stephen King",
            "Jane Austen", "Neil Gaiman", "Brandon Sanderson"
        );
        
        // Sample genres
        List<String> genres = Arrays.asList(
            "Fiction", "Mystery", "Science Fiction", "Fantasy", "Romance",
            "Thriller", "Horror", "Historical Fiction", "Biography", "Non-fiction",
            "Adventure", "Drama", "Poetry", "Classic", "Contemporary"
        );

        // Sample book titles and summaries for each author
        Map<String, List<String[]>> authorBooks = new HashMap<>();
        authorBooks.put("J.K. Rowling", Arrays.asList(
            new String[]{"Harry Potter and the Philosopher's Stone", "The story of a young wizard's first year at Hogwarts School of Witchcraft and Wizardry."},
            new String[]{"Harry Potter and the Chamber of Secrets", "Harry's second year at Hogwarts brings fresh challenges and mysterious dangers."},
            new String[]{"The Casual Vacancy", "A dark comedy about a small English town dealing with local politics and social issues."}
        ));
        
        authorBooks.put("George R.R. Martin", Arrays.asList(
            new String[]{"A Game of Thrones", "The first book in the epic fantasy series A Song of Ice and Fire."},
            new String[]{"A Clash of Kings", "The second novel in the series, continuing the saga of the Seven Kingdoms."}
        ));
        
        authorBooks.put("Stephen King", Arrays.asList(
            new String[]{"The Shining", "A psychological horror novel about a family serving as winter caretakers of the Overlook Hotel."},
            new String[]{"It", "A horror novel about seven children who face an ancient, shape-shifting evil."},
            new String[]{"The Stand", "A post-apocalyptic horror/fantasy novel about the survivors of a pandemic."}
        ));

        Random random = new Random(42); // Fixed seed for reproducibility

        // Create books for each author
        for (String author : authors) {
            List<String[]> authorBookList = authorBooks.getOrDefault(author, Collections.singletonList(
                new String[]{"Unknown Book", "A compelling book that explores various themes and ideas."}
            ));
            
            for (String[] bookInfo : authorBookList) {
                Book book = new Book();
                book.setId(UUID.randomUUID().toString());
                book.setTitle(bookInfo[0]);
                book.setAuthor(author);
                book.setSummary(bookInfo[1]);
                
                // Assign 1-3 random genres
                int numGenres = random.nextInt(3) + 1;
                Set<String> bookGenres = new HashSet<>();
                while (bookGenres.size() < numGenres) {
                    bookGenres.add(genres.get(random.nextInt(genres.size())));
                }
                book.setGenres(new ArrayList<>(bookGenres));
                
                books.add(book);
            }
        }

        return books;
    }

    public static List<Book> processDataset() {
        try {
            String content = Files.readString(Paths.get(DATASET_PATH));
            return objectMapper.readValue(content, objectMapper.getTypeFactory().constructCollectionType(List.class, Book.class));
        } catch (IOException e) {
            logger.error("Error processing dataset", e);
            throw new RuntimeException("Failed to process dataset", e);
        }
    }
} 