package org.example.hellofx;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * ChatBotService that loads system instructions from a file.
 */
public class ChatBotService {

    private final String apiKey;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private boolean mockMode = true;

    public ChatBotService() {
        // 1. Replace with your actual Google AI API Key from https://aistudio.google.com/app/apikey
        this.apiKey = "AIzaSyDjAV5jIQAMS4R4mWKtlMuw_d6nlW29t8o";
       
        if (this.apiKey != null && !this.apiKey.isBlank() && !this.apiKey.equals("PASTE_YOUR_API_KEY_HERE")) {
            this.mockMode = false;
            System.out.println("API Key found. Running in Live Mode.");
        } else {
            System.out.println("No API Key found. Running in Mock Mode.");
        }
    }

    public static class ChatCommand {
        public String action = "noop";
        public String direction = null;
        public double distance = 0.0;
        public double confidence = 1.0;
        public String explanation = "";
        public String timestamp = Instant.now().toString();
    }

    /**
     * Helper method to load the text file from resources.
     */
    private String loadSystemInstructions() {
        try (InputStream is = getClass().getResourceAsStream("/system_instructions.txt")) {
            if (is == null) {
                System.err.println("WARNING: system_instructions.txt not found! Using fallback.");
                return "You are a robot controller. Reply ONLY with JSON.";
            }
            return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            e.printStackTrace();
            return "You are a robot controller. Reply ONLY with JSON.";
        }
    }

    public CompletableFuture<ChatCommand> requestCommand(String telemetryJson, String goal) {
        if (mockMode) {
            System.out.println("ChatBot: Running in MOCK mode");
            return CompletableFuture.supplyAsync(() -> mockDecision(telemetryJson, goal), executor);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("ChatBot: Calling Google Generative AI API...");
                
                // Call Google Generative AI API - using v1 endpoint with gemini-1.5-flash
                String apiUrl = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent?key=" + apiKey;
                
                String systemInstructionText = loadSystemInstructions();
                String prompt = "You are a helpful robot controller assistant. Provide a response to help the user.\n" +
                                "User: " + goal;
                
                String jsonRequest = "{\n" +
                    "  \"contents\": [{\n" +
                    "    \"parts\": [{\n" +
                    "      \"text\": \"" + escapeJson(prompt) + "\"\n" +
                    "    }]\n" +
                    "  }]\n" +
                    "}";
                
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                
                int responseCode = connection.getResponseCode();
                System.out.println("ChatBot: API Response Code: " + responseCode);
                
                if (responseCode == 200) {
                    InputStream inputStream = connection.getInputStream();
                    String responseBody = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"));
                    
                    System.out.println("ChatBot: API Response: " + responseBody.substring(0, Math.min(200, responseBody.length())));
                    
                    ChatCommand cmd = parseGoogleResponse(responseBody);
                    if (cmd != null && !cmd.explanation.isEmpty()) {
                        System.out.println("ChatBot: Successfully parsed response");
                        return cmd;
                    }
                } else {
                    InputStream errorStream = connection.getErrorStream();
                    if (errorStream != null) {
                        String errorBody = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8))
                            .lines()
                            .collect(Collectors.joining("\n"));
                        System.out.println("ChatBot: API Error: " + errorBody);
                    }
                }
                
                System.out.println("ChatBot: Falling back to mock response");
                return mockDecision(telemetryJson, goal);
            } catch (Exception e) {
                System.out.println("ChatBot: Exception - " + e.getMessage());
                e.printStackTrace();
                return mockDecision(telemetryJson, goal);
            }
        }, executor);
    }
    
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    private ChatCommand parseGoogleResponse(String responseJson) {
        try {
            ChatCommand cmd = new ChatCommand();
            
            // Extract text content from Google's response
            int textStart = responseJson.indexOf("\"text\":");
            if (textStart == -1) {
                System.out.println("ChatBot: Could not find 'text' in response");
                return null;
            }
            
            int contentStart = responseJson.indexOf("\"", textStart + 7);
            int contentEnd = responseJson.indexOf("\"", contentStart + 1);
            
            // Handle escaped quotes
            while (contentEnd > 0 && responseJson.charAt(contentEnd - 1) == '\\') {
                contentEnd = responseJson.indexOf("\"", contentEnd + 1);
            }
            
            if (contentEnd <= contentStart) {
                System.out.println("ChatBot: Could not parse text content");
                return null;
            }
            
            String content = responseJson.substring(contentStart + 1, contentEnd);
            content = content.replace("\\\"", "\"").replace("\\n", "\n").replace("\\\\", "\\");
            
            System.out.println("ChatBot: Extracted content: " + content.substring(0, Math.min(100, content.length())));
            
            // Use the full response as explanation if no JSON is found
            cmd.explanation = content.trim();
            
            // Try to parse JSON fields if they exist
            if (content.contains("\"action\"")) {
                int actionStart = content.indexOf("\"action\"") + 9;
                int actionEnd = content.indexOf("\"", actionStart);
                if (actionEnd > actionStart) {
                    cmd.action = content.substring(actionStart, actionEnd).trim();
                }
            }
            
            if (content.contains("\"direction\"")) {
                int dirStart = content.indexOf("\"direction\"") + 12;
                int dirEnd = content.indexOf("\"", dirStart);
                if (dirEnd > dirStart) {
                    cmd.direction = content.substring(dirStart, dirEnd).trim();
                }
            }
            
            if (content.contains("\"distance\"")) {
                int distStart = content.indexOf("\"distance\"") + 11;
                int distEnd = content.indexOf(",", distStart);
                if (distEnd == -1) distEnd = content.indexOf("}", distStart);
                if (distEnd > distStart) {
                    String distStr = content.substring(distStart, distEnd).trim();
                    try {
                        cmd.distance = Double.parseDouble(distStr);
                    } catch (NumberFormatException e) {
                        cmd.distance = 0.0;
                    }
                }
            }
            
            cmd.timestamp = Instant.now().toString();
            return cmd;
        } catch (Exception e) {
            System.out.println("ChatBot: Error parsing response - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ... (MockDecision and ParseSimpleJson methods remain the same) ...
   
    private ChatCommand mockDecision(String telemetryJson, String goal) {
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

    public void shutdown() {
        executor.shutdownNow();
    }
}