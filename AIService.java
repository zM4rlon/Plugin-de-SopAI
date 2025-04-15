package com.example.shopai.ai;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.example.shopai.ShopAIPlugin;

public class AIService {
    
    private final ShopAIPlugin plugin;
    
    public AIService(ShopAIPlugin plugin) {
        this.plugin = plugin;
    }
    
    public double suggestPrice(String itemId, double basePrice, Map<String, Integer> interestCounts) {
        try {
            // Check if API key is configured
            String apiKey = plugin.getConfig().getString("openai-api-key", "");
            if (apiKey.isEmpty()) {
                plugin.getLogger().warning("OpenAI API key not configured. Using fallback pricing algorithm.");
                return calculateFallbackPrice(basePrice, interestCounts);
            }
            
            // Prepare request to OpenAI API
            URL url = new URL("https://api.openai.com/v1/chat/completions");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setDoOutput(true);
            
            // Create prompt
            String prompt = createPrompt(itemId, basePrice, interestCounts);
            
            // Create request body
            String requestBody = "{"
                + "\"model\": \"gpt-3.5-turbo\","
                + "\"messages\": ["
                + "  {\"role\": \"system\", \"content\": \"You are an AI assistant that helps with dynamic pricing for a Minecraft shop. You only respond with a number representing the suggested price.\"},"
                + "  {\"role\": \"user\", \"content\": \"" + prompt + "\"}"
                + "],"
                + "\"temperature\": 0.7,"
                + "\"max_tokens\": 50"
                + "}";
            
            // Send request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            // Read response
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                
                // Parse JSON response
                JSONParser parser = new JSONParser();
                JSONObject jsonResponse = (JSONObject) parser.parse(response.toString());
                
                // Extract price from response
                String content = extractContentFromResponse(jsonResponse);
                
                // Parse price from content
                double suggestedPrice = extractPriceFromContent(content, basePrice);
                
                // Ensure price is within reasonable bounds
                return validatePrice(suggestedPrice, basePrice);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error calling OpenAI API: " + e.getMessage());
            return calculateFallbackPrice(basePrice, interestCounts);
        }
    }
    
    private String createPrompt(String itemId, double basePrice, Map<String, Integer> interestCounts) {
        int purchases = interestCounts.getOrDefault("purchase", 0);
        int interests = interestCounts.getOrDefault("interest", 0);
        int disinterests = interestCounts.getOrDefault("disinterest", 0);
        
        return "I need to price a Minecraft item called '" + itemId + "'. "
             + "The base price is " + basePrice + ". "
             + "Player behavior data: "
             + purchases + " purchases, "
             + interests + " expressions of interest, "
             + disinterests + " expressions of disinterest. "
             + "What should the new price be? Respond only with a number.";
    }
    
    private String extractContentFromResponse(JSONObject jsonResponse) {
        try {
            JSONObject choices = (JSONObject) ((org.json.simple.JSONArray) jsonResponse.get("choices")).get(0);
            JSONObject message = (JSONObject) choices.get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            plugin.getLogger().warning("Error parsing OpenAI response: " + e.getMessage());
            return "";
        }
    }
    
    private double extractPriceFromContent(String content, double basePrice) {
        try {
            // Try to extract a number from the content
            content = content.trim();
            
            // Remove any non-numeric characters except decimal point
            content = content.replaceAll("[^0-9.]", "");
            
            if (content.isEmpty()) {
                return basePrice;
            }
            
            return Double.parseDouble(content);
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Could not parse price from AI response: " + content);
            return basePrice;
        }
    }
    
    private double validatePrice(double suggestedPrice, double basePrice) {
        // Ensure price doesn't go below 50% or above 200% of base price
        double minPrice = basePrice * 0.5;
        double maxPrice = basePrice * 2.0;
        
        return Math.max(minPrice, Math.min(maxPrice, suggestedPrice));
    }
    
    private double calculateFallbackPrice(double basePrice, Map<String, Integer> interestCounts) {
        int purchases = interestCounts.getOrDefault("purchase", 0);
        int interests = interestCounts.getOrDefault("interest", 0);
        int disinterests = interestCounts.getOrDefault("disinterest", 0);
        
        // Simple demand calculation
        double demand = purchases * 2 + interests - disinterests;
        
        if (demand > 0) {
            return basePrice * (1.0 + (Math.min(demand, 20) / 100.0)); // Max 20% increase
        } else if (demand < 0) {
            return basePrice * (1.0 - (Math.min(Math.abs(demand), 20) / 100.0)); // Max 20% decrease
        } else {
            return basePrice;
        }
    }
}
