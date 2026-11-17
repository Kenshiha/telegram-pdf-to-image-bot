public class BotEngine {

    private final TelegramApiClient apiClient;
    private long currentOffset = 0;
    private boolean isRunning = false;

    public BotEngine(String botToken) {
        this.apiClient = new TelegramApiClient(botToken);
    }

    public void start(){
        isRunning = true;
        System.out.println("[BotEngine] Bot Started and Listening for Messages...");

        while (isRunning){
            try {
                // 1 Fetch raw JSON updates from telegram using our network client
                String jsonResponse = apiClient.getUpdates(currentOffset);
                //System.out.println("[Debug JSON] " + jsonResponse);

                if (jsonResponse != null && jsonResponse.contains("update_id")){
                    long updateId = SimpleJsonParser.extractLong(jsonResponse, "update_id");

                    if (updateId != -1){
                        currentOffset = updateId + 1;

                        // Extract chat_id and msg text manually
                        long chatId = SimpleJsonParser.extractLong(jsonResponse, "id");
                        String messageText = SimpleJsonParser.extractString(jsonResponse, "text");
                        String fileName = SimpleJsonParser.extractDocumentField(jsonResponse, "file_name");
                        String fileId = SimpleJsonParser.extractDocumentField(jsonResponse, "file_id");

                        if(chatId != -1){
                            if(messageText != null) {
                                System.out.println("[Received] Chat ID: " + chatId + " | Message: " + messageText);

                                String replyText;
                                if (messageText.equalsIgnoreCase("/start") || messageText.equalsIgnoreCase("hello")) {
                                    replyText = "Welcome! 📄✨\n\nI am your PDF-to-Image bot. Send me any **PDF document**, and I will convert each page into high-quality images and send them right back to you!";
                                } else {
                                    replyText = "Please upload a **PDF file** to begin conversion!";
                                }
                                apiClient.sendMessage(chatId, replyText);
                            } else if (fileId != null && fileName != null) {
                                System.out.println("[Received] Chat ID: " + chatId + " | File Name: " + fileName + " | File ID: " + fileId);

                                if(fileName.toLowerCase().endsWith(".pdf")){
                                    apiClient.sendMessage(chatId, " PDF received: " + fileName + " Preparing to convert...");
                                }else {
                                    apiClient.sendMessage(chatId, "That Does not Look like PDF. Please upload a valid .pdf file.");
                                }
                            }
                        }
                    }
                }
                Thread.sleep(1000);
            } catch (InterruptedException e){
                System.out.println("[BotEngine] Polling Intereupted.");
                break;
            } catch (Exception e){
                System.out.println("[BotEngine] Error in Polling loop: " + e.getMessage());
            }
        }
    }
    public void stop(){
        isRunning = false;
    }
}
