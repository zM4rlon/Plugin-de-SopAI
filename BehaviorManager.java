package com.example.shopai.managers;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.example.shopai.ShopAIPlugin;
import com.example.shopai.models.BehaviorRecord;

public class BehaviorManager {
    
    private final ShopAIPlugin plugin;
    private final Map<UUID, List<BehaviorRecord>> playerBehaviors;
    private File dataFile;
    private FileConfiguration dataConfig;
    
    public BehaviorManager(ShopAIPlugin plugin) {
        this.plugin = plugin;
        this.playerBehaviors = new HashMap<>();
        
        setupDataFile();
        loadData();
    }
    
    private void setupDataFile() {
        dataFile = new File(plugin.getDataFolder(), "behavior_data.yml");
        
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create behavior_data.yml!");
                e.printStackTrace();
            }
        }
        
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }
    
    public void recordBehavior(UUID playerId, String type, String itemId) {
        BehaviorRecord record = new BehaviorRecord(
            LocalDateTime.now(),
            type,
            itemId
        );
        
        if (!playerBehaviors.containsKey(playerId)) {
            playerBehaviors.put(playerId, new ArrayList<>());
        }
        
        playerBehaviors.get(playerId).add(record);
        
        // Save data periodically or on server shutdown
        if (playerBehaviors.get(playerId).size() % 10 == 0) {
            saveData();
        }
    }
    
    public void loadPlayerData(UUID playerId) {
        if (!playerBehaviors.containsKey(playerId)) {
            playerBehaviors.put(playerId, new ArrayList<>());
            
            // Load from config
            if (dataConfig.contains("players." + playerId.toString())) {
                List<Map<?, ?>> records = dataConfig.getMapList("players." + playerId.toString());
                
                for (Map<?, ?> recordMap : records) {
                    String dateTimeStr = (String) recordMap.get("timestamp");
                    LocalDateTime timestamp = LocalDateTime.parse(
                        dateTimeStr, 
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    );
                    
                    String type = (String) recordMap.get("type");
                    String itemId = (String) recordMap.get("itemId");
                    
                    BehaviorRecord record = new BehaviorRecord(timestamp, type, itemId);
                    playerBehaviors.get(playerId).add(record);
                }
            }
        }
    }
    
    public List<BehaviorRecord> getPlayerBehaviors(UUID playerId) {
        if (!playerBehaviors.containsKey(playerId)) {
            loadPlayerData(playerId);
        }
        
        return playerBehaviors.getOrDefault(playerId, new ArrayList<>());
    }
    
    public Map<String, Integer> getItemInterestCounts(String itemId) {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("interest", 0);
        counts.put("disinterest", 0);
        counts.put("purchase", 0);
        
        for (List<BehaviorRecord> records : playerBehaviors.values()) {
            for (BehaviorRecord record : records) {
                if (itemId.equals(record.getItemId()) || record.getItemId() == null) {
                    String type = record.getType();
                    if (counts.containsKey(type)) {
                        counts.put(type, counts.get(type) + 1);
                    }
                }
            }
        }
        
        return counts;
    }
    
    public void loadData() {
        playerBehaviors.clear();
        
        if (dataConfig.contains("players")) {
            ConfigurationSection playersSection = dataConfig.getConfigurationSection("players");
            
            if (playersSection != null) {
                for (String playerIdStr : playersSection.getKeys(false)) {
                    UUID playerId = UUID.fromString(playerIdStr);
                    loadPlayerData(playerId);
                }
            }
        }
    }
    
    public void saveData() {
        // Clear existing data
        dataConfig.set("players", null);
        
        // Save all player behaviors
        for (Map.Entry<UUID, List<BehaviorRecord>> entry : playerBehaviors.entrySet()) {
            UUID playerId = entry.getKey();
            List<BehaviorRecord> records = entry.getValue();
            
            List<Map<String, Object>> recordMaps = new ArrayList<>();
            
            for (BehaviorRecord record : records) {
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put("timestamp", record.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                recordMap.put("type", record.getType());
                recordMap.put("itemId", record.getItemId());
                
                recordMaps.add(recordMap);
            }
            
            dataConfig.set("players." + playerId.toString(), recordMaps);
        }
        
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save behavior data!");
            e.printStackTrace();
        }
    }
}
