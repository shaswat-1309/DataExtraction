import java.io.*;
import java.util.Arrays;
import java.util.List;
/**

 The DataProcessingEngine class processes a list of articles and writes them to files in a specified folder.
 The maximum number of articles per file, maximum number of files per keyword and maximum number of articles per keyword are
 defined as constants in the class.
 */
 public class DataProcessingEngine {
    /**
     * The base path for the output folder is obtained from the user directory.
     */
    private static final String BASE_PATH = System.getProperty("user.dir");

    /**
     * The maximum number of articles that can be written to a single file.
     */
    private static final int MAX_ARTICLES_PER_FILE = 5;

    /**
     * The maximum number of files that can be created per keyword.
     */
    private static final int MAX_FILES_PER_KEYWORD = 5;

    /**
     * The maximum number of articles that can be processed per keyword.
     */
    private static final int MAX_ARTICLES_PER_KEYWORD = 8;
    private static TransformationEngine te = new TransformationEngine();

    /**
     * Processes the given list of articles and writes them to files in the output folder.
     *
     * @param articles          the list of articles to be processed
     * @param existingFileCount the number of existing files for the current keyword
     * @return
     * @throws IOException if an I/O error occurs
     */
    public static void processArticles(List<ExtractionEngine.Article> articles, int existingFileCount) throws IOException {
        String folderPath = BASE_PATH + "/output";
        int fileCount = existingFileCount;
        File folder = new File(folderPath);

        // create the output folder if it does not exist
        if (!folder.exists()) {
            folder.mkdir();
        }

        int articleCount = 0;
        String filePath = null;
        FileWriter writer = null;
        boolean fileProcessed = false;

        // get a list of all files in the output folder and sort them alphabetically
        File[] files = folder.listFiles();
        Arrays.sort(files);

        // loop through each file in the output folder
        for (File file : files) {
            String fileName = file.getName();

            // check if the file is a text file and its name starts with "file"
            if (fileName.endsWith(".txt") && fileName.startsWith("file")) {
                int numArticles = countArticlesInFile(file);

                // check if the file has fewer than the maximum number of articles allowed
                if (numArticles < MAX_ARTICLES_PER_FILE) {
                    // continue writing to this file
                    filePath = folderPath + "/" + fileName;
                    writer = new FileWriter(filePath, true);
                    articleCount = numArticles;
                    break;
                }
            }
        }

        // if a writer has not been initialized, create a new file and writer
        if (writer == null) {
            filePath = folderPath + "/file" + fileCount + ".txt";
            File obj = new File(filePath);
            obj.createNewFile();
            writer = new FileWriter(filePath);
        }

        // loop through each article and write it to the file
        for (ExtractionEngine.Article article : articles) {
            // check if the maximum number of articles per file has been reached
            if (articleCount == MAX_ARTICLES_PER_FILE) {
                writer.close();

                // check if the file has already been processed
                if (!fileProcessed) {
                    fileProcessed = true;
                }

                // transform the file and increment the file count
                te.processFiles("file" + fileCount + ".txt");
                if (fileCount % MAX_FILES_PER_KEYWORD == 0) {
                    fileCount = existingFileCount + 1;
                } else {
                    fileCount++;
                }

                // create a new file and writer
                filePath = folderPath + "/file" + fileCount + ".txt";
                File obj = new File(filePath);
/**

 Creates a new File instance by converting the given pathname string into an abstract pathname.
 @param filePath A pathname string.
 @return A new File instance representing the same abstract pathname.
 */
                obj.createNewFile();
                /**
                 Creates a new, empty file named by this abstract pathname if and only if a file with this name does not yet exist.
                 @throws IOException If an I/O error occurs
                 */
                writer = new FileWriter(filePath);
                /**
                 Constructs a FileWriter object given a file name.
                 @param filePath A string containing the file name.
                 */
                articleCount = 0;
                /**
                 Initializes articleCount to 0.
                 */
            }
            writer.append("Title: ").append(article.getTitle().replaceAll("[^\\p{Alnum}\s]", "")).append("\n")
                    .append("Description: ").append(article.getDescription()).append("\n\n");
            /**
             Appends the title and content of an article to the FileWriter.
             */
            articleCount++;
        }
                //Increments the articleCount variable.
                writer.close();

                //Closes the FileWriter object.

                if (!fileProcessed) {
                    te.processFiles("file" + fileCount + ".txt");
                }
            }

            //If the file was not processed, calls the processFiles method of the TransformationEngine class to process the file.
            /**

             Constructs a new BufferedReader to read characters from a file.
             @param file A File object representing a file to be read.
             */

            private static int countArticlesInFile (File file) throws IOException {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                int count = 0;
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Title: ")) {
                        count++;
                    }
                }
                reader.close();
                return count;
            }
 }