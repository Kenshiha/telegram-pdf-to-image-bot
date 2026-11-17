import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TelegramApiClient {
    private final String botToken;
    private final HttpClient httpClient;

    public TelegramApiClient(String botToken) {
        this.botToken = botToken;
        this.httpClient = HttpClient.newHttpClient();
    }

    // GET MESSAGE : ask telegram for new msg
    public String getUpdates(long offset){

        String url = "https://api.telegram.org/bot" + botToken + "/getUpdates?offset=" + offset + "&timeout=5";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e){
            System.err.println("Error Fetching updates: " + e.getMessage());
            return null;

        }
    }

    //SEND MESSAGE: sends a raw json post request to reply user

    public String sendMessage(long chatId, String text){
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        String jsonPayload = "{\"chat_id\": " + chatId + ", \"text\": \"" + text + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        }catch (Exception e){
            System.err.println("Error sending message: " + e.getMessage());
            return null;
        }
    }

    // GetFilePath
    public String getFilePath(String fileId){
        try {
            String urlString = "https://api.telegram.org/bot" + botToken + "/getFile?file_id" + fileId;
            java.net.URL url = new java.net.URL(urlString);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            try {
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null){
                    response.append(line);
                }
                return SimpleJsonParser.extractString(response.toString(), "file_path");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e){
            System.err.println("[ERROR] failed to get file path " + e.getMessage());
            return null;
        }
    }
}
