import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * The ExtractionEngine class extracts articles from the News API based on a list of keywords, and then processes the
 * extracted articles using the DataProcessingEngine class.
 */
public class ExtractionEngine {

    // The API key for the News API
    private static final String API_KEY = "key";

    // The API endpoint for the News API
    private static final String API_ENDPOINT = "https://newsapi.org/v2/everything?";

    // The character set to use when encoding URLs
    private static final String API_CHARSET = "UTF-8";

    /**
     * An inner class representing an article.
     */
    public static class Article {

        // The title of the article
        private final String title;

        // The description of the article
        private final String description;

        /**
         * Constructor for the Article class.
         *
         * @param title       the title of the article
         * @param description the description of the article
         */
        public Article(String title, String description) {
            this.title = title;
            this.description = description;
        }

        /**
         * Returns the title of the article.
         *
         * @return the title of the article
         */
        public String getTitle() {
            return title;
        }

        /**
         * Returns the description of the article.
         *
         * @return the description of the article
         */
        public String getDescription() {
            return description;
        }

    }

    /**
     * The main method of the ExtractionEngine class.
     *
     * @param args command line arguments (not used)
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the thread is interrupted
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        // The list of keywords to extract articles for
        String[] keywords = {"Canada", "University", "Dalhousie", "Halifax", "Canada Education", "Moncton", "hockey", "Fredericton", "celebration"};

        // The count of existing files
        int existingFileCount = 1;

        // Extract articles for each keyword and process them using the DataProcessingEngine class
        for (String keyword : keywords) {

            List<Article> articles = extractArticles(keyword);
            DataProcessingEngine.processArticles(articles, existingFileCount);
            File[] files = new File(System.getProperty("user.dir") + "/output").listFiles();
            int numFiles = 0;
            for (File file : files) {
                if (file.getName().endsWith(".txt") && file.getName().startsWith("file")) {
                    numFiles++;
                }
            }
            existingFileCount = numFiles;
        }

    }

    /**
     * Extracts articles from the News API based on a given keyword.
     *
     * @param keyword the keyword to search for
     * @return a list of articles matching the keyword
     * @throws IOException if an I/O error occurs
     */
    private static List<Article> extractArticles(String keyword) throws IOException {

        // The list of articles to return
        List<Article> articles = new ArrayList<>();

        // Convert the keyword to lowercase
        keyword = keyword.toLowerCase();
        // Encode the keyword for use in the API URL
        String encodedKeyword = URLEncoder.encode(keyword, API_CHARSET);

        // Construct the full URL for the API request
        String fullUrl = API_ENDPOINT + "q=" + encodedKeyword + "&apiKey=" + API_KEY;
        // Create a new URL object from the full URL string
        URL url = new URL(fullUrl);

        // Open a connection to the API endpoint
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        // Read the response from the API endpoint
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();

        // Convert the response to lowercase and extract the articles from the JSON string
        String jsonStr = content.toString().toLowerCase();
        int startIndex = 0;
        while (true) {
            // Find the next occurrence of the "title" field in the JSON string
            startIndex = jsonStr.indexOf("\"title\":", startIndex);
            if (startIndex == -1) {
                break;
            }

            // Extract the title of the article from the JSON string
            startIndex += 9;
            int endIndex = jsonStr.indexOf("\",", startIndex);
            if (endIndex == -1) {
                break;
            }
            String title = jsonStr.substring(startIndex, endIndex);
//             Skip articles with certain characters in their titles
            if (title.contains("\\t")) {
                continue;
            }

            // Find the next occurrence of the "description" field in the JSON string
            startIndex = jsonStr.indexOf("\"description\":", startIndex);
            if (startIndex == -1) {
                break;
            }
            // Extract the description of the article from the JSON string
            startIndex += 15;
            endIndex = jsonStr.indexOf("\",", startIndex);
            if (endIndex == -1) {
                break;
            }
            String description = jsonStr.substring(startIndex, endIndex);

            // Create a new Article object and add it to the list of articles
            Article article = new Article(title, description);
            articles.add(article);

            // Set the start index to the end of the current article in
            startIndex = endIndex;
        }

        return articles;
    }
}


