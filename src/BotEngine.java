
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

                if (jsonResponse != null && !jsonResponse.isEmpty()){
                    long updateId = SimpleJsonParser.extractLong(jsonResponse, "update_id");

                    if (updateId != -1){
                        currentOffset = updateId + 1;

                        // Extract chat_id and msg text manually
                        long chatId = SimpleJsonParser.extractLong(jsonResponse, "Id");
                        String messageText = SimpleJsonParser.extractString(jsonResponse, "text");

                        if(chatId != -1 && messageText != null){
                            System.out.println("[Received] Chat ID: " + chatId + " | Message: "+ messageText);

                            String replyText = "You said: " + messageText;
                            apiClient.sendMessage(chatId, replyText);
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
