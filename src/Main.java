import java.io.FileInputStream;
import java.util.Properties;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        System.out.println("[System] Initializing Raw Java Telegram Bot...");

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")){
                props.load(fis);
        } catch (IOException e) {
            System.err.println("[Error] Could not find 'config.properties' in the project root!");
            System.err.println("[Hint] Make sure you created config.properties.");
            System.exit(1);
        }

        String botToken = props.getProperty("bot.token");

        if(botToken == null){
            System.err.println("[Error] Bot token Missing.");
            System.exit(1);
        }

        BotEngine engine = new BotEngine(botToken);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[System] Shutting down bot grcefully..");
            engine.stop();
        }));
    }
}