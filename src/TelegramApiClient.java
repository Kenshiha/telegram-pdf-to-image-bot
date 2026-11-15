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
}
