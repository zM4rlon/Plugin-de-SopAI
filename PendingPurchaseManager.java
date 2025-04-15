package com.example.shopai.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.example.shopai.models.ShopItem;

public class PendingPurchaseManager {
    
    private final Map<UUID, PendingPurchase> pendingPurchases;
    
    public PendingPurchaseManager() {
        this.pendingPurchases = new HashMap<>();
    }
    
    public void addPendingPurchase(UUID playerId, ShopItem item) {
        pendingPurchases.put(playerId, new PendingPurchase(item));
    }
    
    public PendingPurchase getPendingPurchase(UUID playerId) {
        return pendingPurchases.get(playerId);
    }
    
    public boolean hasPendingPurchase(UUID playerId) {
        return pendingPurchases.containsKey(playerId);
    }
    
    public void removePendingPurchase(UUID playerId) {
        pendingPurchases.remove(playerId);
    }
    
    public static class PendingPurchase {
        private final ShopItem item;
        
        public PendingPurchase(ShopItem item) {
            this.item = item;
        }
        
        public ShopItem getItem() {
            return item;
        }
    }
}
