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
                                    replyText = "Welcome! 📄✨\n\nI am your PDF-to-Image bot. Send me any PDF document, and I will convert each page into high-quality images and send them right back to you!";
                                } else {
                                    replyText = "Please upload a PDF file to begin conversion!";
                                }
                                apiClient.sendMessage(chatId, replyText);
                            } else if (fileId != null && fileName != null) {
                                System.out.println("[Received] Chat ID: " + chatId + " | File Name: " + fileName + " | File ID: " + fileId);

                                if(fileName.toLowerCase().endsWith(".pdf")){
                                    apiClient.sendMessage(chatId, " PDF received: " + fileName + " Preparing to convert...");

                                    String filePath = apiClient.getFilePath(fileId);
                                    if(filePath != null){
                                        java.io.File downloadedPdf = apiClient.downloadPdfFile(filePath, fileName);


                                        if(downloadedPdf != null && downloadedPdf.exists()){
                                            System.out.println("[Debug] Downloaded file size: " + downloadedPdf.length() + " bytes");

//                                            try {
//                                                String fileContent = new String(java.nio.file.Files.readAllBytes(downloadedPdf.toPath()), java.nio.charset.StandardCharsets.UTF_8);
//                                                System.out.println("[Debug File Content]: " + fileContent);
//                                            } catch (Exception e) {
//                                                // Ignore if binary
//                                            }

                                            java.util.List<java.io.File> renderedImage = PdfConverter.convertPdfToImages(downloadedPdf);

                                            if(!renderedImage.isEmpty()){
                                                apiClient.sendMessage(chatId, "Converted " + renderedImage.size() + " page(s). Sending images...");

                                                apiClient.sendMediaGroupAsAlbum(String.valueOf(chatId), renderedImage);
                                                //Loop through each images to send it to user
//                                                for(java.io.File img : renderedImage){
//                                                    if(img.exists()) {
//                                                        //apiClient.sendMessage(chatId, "Sending Images please wait...");
//                                                        apiClient.sendPhoto(chatId, img);
//
//                                                        try {
//                                                            Thread.sleep(300);
//                                                        }catch (InterruptedException e){
//                                                            Thread.currentThread().interrupt();
//                                                        }
//
//                                                    }else {
//                                                        System.err.println("[Error] Image file missing on disk: " + img.getAbsolutePath());
//                                                    }
//                                                }
                                                apiClient.sendMessage(chatId, "All Pages sent successfullt!");
                                            }else {
                                                apiClient.sendMessage(chatId, "Failed to render pages from this PDF.");
                                            }

                                        }else {
                                            apiClient.sendMessage(chatId, "Failed to saved file");
                                        }
                                    }else{
                                        apiClient.sendMessage(chatId, "Could not Retrive file path from telegram");
                                    }
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
