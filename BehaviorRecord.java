package com.example.shopai.models;

import java.time.LocalDateTime;

public class BehaviorRecord {
    
    private final LocalDateTime timestamp;
    private final String type; // "purchase", "interest", "disinterest"
    private final String itemId; // can be null for general shop behavior
    
    public BehaviorRecord(LocalDateTime timestamp, String type, String itemId) {
        this.timestamp = timestamp;
        this.type = type;
        this.itemId = itemId;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getType() {
        return type;
    }
    
    public String getItemId() {
        return itemId;
    }
}
