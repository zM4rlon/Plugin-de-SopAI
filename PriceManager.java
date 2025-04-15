package com.example.shopai.managers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.example.shopai.ShopAIPlugin;
import com.example.shopai.ai.AIService;

public class PriceManager {
    
    private final ShopAIPlugin plugin;
    private final Map<String, Double> currentPrices;
    private final Map<String, Double> basePrices;
    private File priceFile;
    private FileConfiguration priceConfig;
    private AIService aiService;
    
    public PriceManager(ShopAIPlugin plugin) {
        this.plugin = plugin;
        this.currentPrices = new HashMap<>();
        this.basePrices = new HashMap<>();
        this.aiService = new AIService(plugin);
        
        setupPriceFile();
        loadData();
    }
    
    private void setupPriceFile() {
        priceFile = new File(plugin.getDataFolder(), "prices.yml");
        
        if (!priceFile.exists()) {
            try {
                priceFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create prices.yml!");
                e.printStackTrace();
            }
        }
        
        priceConfig = YamlConfiguration.loadConfiguration(priceFile);
    }
    
    public void initializePrice(String itemId, double basePrice) {
        basePrices.put(itemId, basePrice);
        
        if (!currentPrices.containsKey(itemId)) {
            // Load from config or use base price
            if (priceConfig.contains("prices." + itemId)) {
                currentPrices.put(itemId, priceConfig.getDouble("prices." + itemId));
            } else {
                currentPrices.put(itemId, basePrice);
            }
        }
    }
    
    public double getCurrentPrice(String itemId) {
        return currentPrices.getOrDefault(itemId, 0.0);
    }
    
    public void updatePrices() {
        BehaviorManager behaviorManager = plugin.getBehaviorManager();
        
        for (String itemId : currentPrices.keySet()) {
            Map<String, Integer> interestCounts = behaviorManager.getItemInterestCounts(itemId);
            double basePrice = basePrices.getOrDefault(itemId, 100.0);
            
            // Use AI to adjust price based on behavior data
            if (plugin.getConfig().getBoolean("use-ai-pricing", true)) {
                double newPrice = aiService.suggestPrice(itemId, basePrice, interestCounts);
                currentPrices.put(itemId, newPrice);
            } else {
                // Simple algorithm if AI is disabled
                int purchases = interestCounts.getOrDefault("purchase", 0);
                int interests = interestCounts.getOrDefault("interest", 0);
                int disinterests = interestCounts.getOrDefault("disinterest", 0);
                
                double demandFactor = calculateDemandFactor(purchases, interests, disinterests);
                double newPrice = basePrice * demandFactor;
                
                // Ensure price doesn't change too drastically
                double currentPrice = currentPrices.get(itemId);
                double maxChange = currentPrice * 0.25; // Max 25% change
                
                if (Math.abs(newPrice - currentPrice) > maxChange) {
                    if (newPrice > currentPrice) {
                        newPrice = currentPrice + maxChange;
                    } else {
                        newPrice = currentPrice - maxChange;
                    }
                }
                
                // Ensure price doesn't go below 50% or above 200% of base price
                newPrice = Math.max(basePrice * 0.5, Math.min(basePrice * 2.0, newPrice));
                
                currentPrices.put(itemId, newPrice);
            }
        }
        
        // Save updated prices
        saveData();
    }
    
    private double calculateDemandFactor(int purchases, int interests, int disinterests) {
        // Simple demand calculation
        double demand = purchases * 2 + interests - disinterests;
        
        if (demand > 0) {
            return 1.0 + (Math.min(demand, 20) / 100.0); // Max 20% increase
        } else if (demand < 0) {
            return 1.0 - (Math.min(Math.abs(demand), 20) / 100.0); // Max 20% decrease
        } else {
            return 1.0;
        }
    }
    
    public void loadData() {
        currentPrices.clear();
        
        if (priceConfig.contains("prices")) {
            for (String itemId : priceConfig.getConfigurationSection("prices").getKeys(false)) {
                double price = priceConfig.getDouble("prices." + itemId);
                currentPrices.put(itemId, price);
            }
        }
    }
    
    public void saveData() {
        // Save all current prices
        for (Map.Entry<String, Double> entry : currentPrices.entrySet()) {
            priceConfig.set("prices." + entry.getKey(), entry.getValue());
        }
        
        try {
            priceConfig.save(priceFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save price data!");
            e.printStackTrace();
        }
    }
}
