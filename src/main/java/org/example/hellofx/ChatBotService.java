package org.example.hellofx;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Minimal ChatBotService that integrates with the Gemini (Google GenAI) client.
 *
 * What this class does:
 * - If `GEMINI_API_KEY` is set it will call the GenAI client asynchronously
 *   to request a control command based on a telemetry snapshot and a goal.
 * - If no API key is present (or if any error occurs) it falls back to a
 *   deterministic local `mockDecision` implementation so the simulation can
 *   continue without network access.
 * - Responses are converted into the simple `ChatCommand` DTO. A lightweight
 *   `parseSimpleJson` attempts to extract fields from the model text; on
 *   failure the mock fallback is used.
 *
 * Design notes:
 * - All network/model calls are made off the JavaFX thread using an
 *   ExecutorService and returned as `CompletableFuture` to avoid blocking.
 * - Temperature is set low for deterministic responses when using the model.
 */
public class ChatBotService {

    // If present, `apiKey` enables real Gemini calls; otherwise `client` will be null
    private final String apiKey;
    // GenAI client instance (null when running in mock/offline mode)
    private final Client client;
    // Executor used to perform model calls off the JavaFX/UI thread so we
    // don't block the UI when awaiting network/model responses.
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public ChatBotService() {
        this.apiKey = System.getenv("GEMINI_API_KEY");
        if (this.apiKey != null && !this.apiKey.isBlank()) {
            this.client = Client.builder().apiKey(this.apiKey).build();
        } else {
            this.client = null;
        }
    }

    public static class ChatCommand {
        public String action = "noop"; // move|rotate|set_speed|stop|noop
        public String direction = null; // forward/backward
        public double distance = 0.0; // meters / grid units
        public double confidence = 1.0;
        public String explanation = "";
        public String timestamp = Instant.now().toString();
    }

    public CompletableFuture<ChatCommand> requestCommand(String telemetryJson, String goal) {
        if (client == null) {
            // Mock behavior: simple decision engine using telemetry + goal
            return CompletableFuture.supplyAsync(() -> mockDecision(telemetryJson, goal), executor);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Compose simple prompt: system + user
                String systemInstruction = "You are a robot controller. Reply ONLY with JSON: {\"action\":..., \"params\":{...}, \"confidence\":<0-1>, \"explanation\":\"...\"}.";

                Content systemContent = Content.builder()
                        .parts(Part.builder().text(systemInstruction).build())
                        .build();

                GenerateContentConfig config = GenerateContentConfig.builder()
                        .systemInstruction(systemContent)
                        .temperature(0.0F)
                        .build();

                String userPrompt = "Telemetry: " + telemetryJson + "\nGoal: " + goal + "\nRespond with JSON only.";

                GenerateContentResponse response = client.models.generateContent(
                        "gemini-2.5-flash",
                        userPrompt,
                        config
                );

                String text = response.text();
                // attempt simple parsing
                ChatCommand cmd = parseSimpleJson(text);
                if (cmd == null) cmd = mockDecision(telemetryJson, goal);
                return cmd;
            } catch (Exception e) {
                // On error fallback to mock
                return mockDecision(telemetryJson, goal);
            }
        }, executor);
    }

    private ChatCommand mockDecision(String telemetryJson, String goal) {
        // Very small heuristic: if goal contains "exit" try to move forward 1 unit
        ChatCommand c = new ChatCommand();
        if (goal != null && goal.toLowerCase().contains("exit")) {
            c.action = "move";
            c.direction = "forward";
            c.distance = 1.0;
            c.confidence = 0.9;
            c.explanation = "mock: advance toward exit";
        } else {
            c.action = "noop";
            c.confidence = 0.5;
            c.explanation = "mock: no action";
        }
        c.timestamp = Instant.now().toString();
        return c;
    }

    private ChatCommand parseSimpleJson(String text) {
        // naive parser: find "action":"..." and "distance":number
        // NOTE: This is an intentionally lightweight parser used for the
        // prototype. For production use, replace with a strict JSON parser
        // and schema validation to avoid parsing errors and security issues.
        if (text == null) return null;
        ChatCommand cmd = new ChatCommand();
        try {
            String lower = text.replace('\n',' ').trim();
            // action
            java.util.regex.Matcher mAction = java.util.regex.Pattern.compile("\"action\"\s*:\s*\"(\\w+)\"")
                    .matcher(lower);
            if (mAction.find()) cmd.action = mAction.group(1);

            java.util.regex.Matcher mDir = java.util.regex.Pattern.compile("\"direction\"\s*:\s*\"(\\w+)\"")
                    .matcher(lower);
            if (mDir.find()) cmd.direction = mDir.group(1);

            java.util.regex.Matcher mDist = java.util.regex.Pattern.compile("\"distance\"\s*:\s*([0-9]+(?:\\.[0-9]+)?)")
                    .matcher(lower);
            if (mDist.find()) cmd.distance = Double.parseDouble(mDist.group(1));

            java.util.regex.Matcher mConf = java.util.regex.Pattern.compile("\"confidence\"\s*:\s*([0-9]+(?:\\.[0-9]+)?)")
                    .matcher(lower);
            if (mConf.find()) cmd.confidence = Double.parseDouble(mConf.group(1));

            java.util.regex.Matcher mExpl = java.util.regex.Pattern.compile("\"explanation\"\s*:\s*\"([^\"]*)\"")
                    .matcher(lower);
            if (mExpl.find()) cmd.explanation = mExpl.group(1);

            cmd.timestamp = Instant.now().toString();
            return cmd;
        } catch (Exception e) {
            return null;
        }
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
