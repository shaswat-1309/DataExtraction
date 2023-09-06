import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.LinkedList;
import java.util.Queue;
import com.mongodb.*;
import com.mongodb.client.*;
import org.bson.Document;

/**

 A class for processing and transforming text files and uploading the results to a MongoDB database.
  */
public class TransformationEngine {
    /**
     * The connection string for the MongoDB database.
     */
    static ConnectionString connectionString = new ConnectionString("mongodb+srv://shaswatdoshi000:HJNWwHf2sfXoyZIl@cluster0.xocy03t.mongodb.net/?retryWrites=true&w=majority");

    /**
     * The settings for the MongoDB client.
     */
    static MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build();

    /**
     * The MongoDB client.
     */
    static MongoClient mongoClient = MongoClients.create(settings);

    /**
     * The MongoDB database to use.
     */
    static MongoDatabase database = mongoClient.getDatabase("myMongoNews");

    /**
     * The MongoDB collection to use.
     */
    static MongoCollection<Document> collection = database.getCollection("test");

    /**
     * The queue of files to be processed.
     */
    private static Queue<String> fileQueue = new LinkedList<>();

    /**
     * Whether a file is currently being processed.
     */
    private static boolean isProcessing = false;

    /**
     * The set of files that have already been processed.
     */
    private static Set<String> processedFiles = new HashSet<>();

    /**
     * Adds the specified file to the queue of files to be processed and starts processing files.
     * If the file has already been processed or is currently being processed, this method does nothing.
     *
     * @param fileName the name of the file to process
     * @throws IOException if an I/O error occurs
     */
    public void processFiles(String fileName) throws IOException {
        // If the file has already been processed or is currently being processed, return
        if (processedFiles.contains(fileName) || isProcessing) {
            return;
        }

        // Otherwise, add the file to the queue and start processing files
        fileQueue.add(fileName);
        processQueue();
    }

    /**
     * Processes the next file in the queue of files to be processed.
     * If there are no files left to process, this method does nothing.
     *
     * @throws IOException if an I/O error occurs
     */
    private void processQueue() throws IOException {
        // If there are no files left to process, return
        if (fileQueue.isEmpty()) {
            return;
        }

        // Otherwise, get the next file from the queue and set isProcessing to true
        String fileName = fileQueue.poll();
        isProcessing = true;

        String folderPath = System.getProperty("user.dir") + "/output";
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().equals(fileName)) {
                        System.out.println("Processing file: " + fileName);
                        processFile(file);
                        processedFiles.add(fileName);
                        break;
                    }
                }
            }
        }

        // Set isProcessing to false and process the next file in the queue
        isProcessing = false;
        processQueue();
    }

/**
 * Processes the specified file and transforms its contents, uploading the result to the MongoDB database.
 *
 * @param file the file to process
 * @throws IOException if an I/O error occurs
 */

    /**

     This method extracts articles from the given file and transforms them before uploading them to MongoDB.
     @param file the file from which the articles are to be extracted
     @throws IOException if there is an error while reading the file
     */
    private void processFile(File file) throws IOException {
        List<Article> articles = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String title = null;
            StringBuilder description = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Title: ")) {
                    if (title != null && description.length() > 0) {
                        articles.add(new Article(title, description.toString()));
                        title = null;
                        description = new StringBuilder();
                    }
                    title = line.substring(7);
                } else if (line.startsWith("Description: ")) {
                    description.append(line.substring(9));
                }
            }
            if (title != null && description.length() > 0) {
                articles.add(new Article(title, description.toString()));
            }
        }
        transformArticles(articles);
    }
    /**

     This method transforms the given list of articles and uploads them to MongoDB.
     @param articles the list of articles to be transformed and uploaded
     */
    public void transformArticles(List<Article> articles) {
        for (Article article : articles) {
            String cleanedTitle = cleanText(article.getTitle());
            String cleanedDescription = cleanText(article.getDescription());
            Article transformedArticle = new Article(cleanedTitle, cleanedDescription);
            uploadToMongoDB(transformedArticle);
        }
    }
    /**

     This method cleans the given text by removing URLs, emoticons, and special characters, and converting

     the text to lowercase.

     @param text the text to be cleaned

     @return the cleaned text
     */
    private static String cleanText(String text) {
        String regex = "[^A-Za-z0-9\\s]+|https?://\\S+|:\\)|;\\)|:-\\)|:-D|:\\(|:-\\(|:'\\(|:'-\\(|:P|:O";
        text = text.replaceAll(regex, "");

        text = text.toLowerCase();

        return text;
    }

    /**

     This method uploads the given article to MongoDB.
     @param article the article to be uploaded
     */
    private static void uploadToMongoDB(Article article) {
        Document doc = new Document("title", article.getTitle())
                .append("description", article.getDescription());
        collection.insertOne(doc);
    }
    /**

     This class represents an article with a title and description.
     */
    public class Article {
        private final String title;
        private final String description;

        public Article(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }
    }
}