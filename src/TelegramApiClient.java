import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLOutput;
import java.util.List;

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
            String urlString = "https://api.telegram.org/bot" + botToken + "/getFile?file_id=" + fileId;
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

    //Download pdf file
    public java.io.File downloadPdfFile(String filePath, String originalFileName){
        try {
            //Construct the permanent file download URL
            String fileUrlString = "https://api.telegram.org/file/bot" + botToken + "/" + filePath;
            java.net.URL url = new java.net.URL(fileUrlString);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Create a local temp file to store the downloaded file

            java.io.File tempFile = new  java.io.File("downloaded_" + originalFileName);
            // Read the incoming Bytes coming from telegram and stores them directly into temp file
            try(java.io.InputStream in = conn.getInputStream();
                 java.io.OutputStream out = new java.io.FileOutputStream(tempFile))  {

                byte[] buffer = new byte[4096];
                int byteRead;
                while((byteRead = in.read(buffer)) != -1){
                    out.write(buffer, 0, byteRead);
                }
            }

            System.out.println("[SYstem] File successfully downloaded locally: " + tempFile.getAbsolutePath());
            return tempFile;
        }catch (Exception e){
            System.err.println("[ERROR] Failed to download file bytes: " + e.getMessage());
            return null;
        }
    }

    //Sending Photo in album to minimize the http POST request.
    public void sendMediaGroupAsAlbum(String chatId, List<File> imageFile){
        for(int i = 0; i < imageFile.size(); i += 10){
            List<File> batch = imageFile.subList(i, Math.min(i + 10, imageFile.size()));
            //sending album
            sendSingleAlbum(chatId,batch);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void sendSingleAlbum(String chatId, List<File> batch){
        String boundary = "===" + System.currentTimeMillis() + "===";
        String urlString = "https://api.telegram.org/bot" + botToken + "/sendMediaGroup";

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream outputStream = conn.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true))
            {
                //1. Write chat id
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"chat_id\"\r\n\r\n");
                writer.append(chatId).append("\r\n");

                //2. Build JSON array defining the media
                StringBuilder jsonMedia = new StringBuilder("[");
                for (int j = 0; j < batch.size(); j++){
                    if(j > 0) jsonMedia.append(",");
                    jsonMedia.append("{\"type\":\"photo\",\"media\":\"attach://photo").append(j).append("\"}");
                }
                jsonMedia.append("]");

                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"media\"\r\n\r\n");
                writer.append(jsonMedia.toString()).append("\r\n");

                //3. Attach each physical image file bytes with corresponding field name
                for(int j = 0; j < batch.size(); j++){
                    File file = batch.get(j);
                    writer.append("--").append(boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"photo")
                            .append(String.valueOf(j)).append("\"; filename=\"").append(file.getName()).append("\"\r\n");
                    writer.append("Content-Type: image/png\r\n\r\n");
                    writer.flush();

                    try {
                        FileInputStream inputStream = new FileInputStream(file);
                        byte[] buffer= new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1){
                            if (bytesRead > 0){
                                outputStream.write(buffer, 0, bytesRead);
                            }
                        }
                        outputStream.flush();
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    writer.append("\r\n");
                    writer.flush();
                }

                //End multipart boundary
                writer.append("--").append(boundary).append("--\r\n");
                writer.flush();
            }

            int responseCode = conn.getResponseCode();
            if(responseCode == 200){
                System.out.println("[TelegramApiClient] successfully sent album batch of " + batch.size() + "image.");
            } else {
                System.err.println("[Error] Failed to send media group. Response code " + responseCode);
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"))){
                    StringBuilder errorBody = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null){
                        errorBody.append(line.trim());
                    }
                    System.err.println("[Error Body]: " + errorBody.toString());
                } catch (Exception ignored) {}
            }
        }catch (Exception e){
            System.err.println("[Error] Exception while sending media group: " + e.getMessage());
        }
    }




    //This sends photo one by one causing spam of POST request , so won't work when PDF has lots of pages
    public void sendPhoto(long chatId, java.io.File imageFile){
        String boundary = "===" + System.currentTimeMillis() + "===";
        String lineEnd = "\r\n";
        String twoHyphenes = "--";

        try {
            java.net.URL url = new java.net.URL("https://api.telegram.org/bot" + botToken +  "/sendPhoto");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connetion", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (java.io.OutputStream outputStream = conn.getOutputStream();
                 java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(outputStream, "UTF-8"), true)){

                //Send chat_id parameter
                writer.append(twoHyphenes).append(boundary).append(lineEnd);
                writer.append("Content-Disposition: form-data; name=\"chat_id\"").append(lineEnd);
                writer.append("Content-Type: text/plain; charset=UTF-8").append(lineEnd);
                writer.append(lineEnd);
                writer.append(String.valueOf(chatId)).append(lineEnd);

                //Send photo file stream
                writer.append(twoHyphenes).append(boundary).append(lineEnd);
                writer.append("Content-Disposition: form-date; name=\"photo\"; filename=\"").append(imageFile.getName()).append("\"").append(lineEnd);
                writer.append("Content-Type: application/octet-stream").append(lineEnd);
                writer.append(lineEnd);
                writer.flush();

                //Write raw image file byte
                try (java.io.FileInputStream inputStream = new java.io.FileInputStream(imageFile)){
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1){
                        if(bytesRead > 0) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                    outputStream.flush();
                }
                writer.append(lineEnd);
                writer.append(twoHyphenes).append(boundary).append(twoHyphenes).append(lineEnd);
            }

            //Read response to ensure success
            int responseCode = conn.getResponseCode();
            if(responseCode == 200){
                System.out.println("[TelegramApiClient] Successfully sent Photo: " + imageFile.getName());
            }else {
                System.err.println("[Error] Failed to sent photo. Response code: " + responseCode);
            }

        } catch (Exception e) {
            System.err.println("[Error] Exception while sending photo: " + e.getMessage());
        }
    }

}
