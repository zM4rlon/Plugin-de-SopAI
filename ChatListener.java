package com.example.shopai.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.example.shopai.ShopAIPlugin;
import com.example.shopai.managers.PendingPurchaseManager.PendingPurchase;
import com.example.shopai.models.ShopItem;

public class ChatListener implements Listener {
    
    private final ShopAIPlugin plugin;
    
    public ChatListener(ShopAIPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        // Check if player has a pending purchase
        if (plugin.getPendingPurchaseManager().hasPendingPurchase(player.getUniqueId())) {
            event.setCancelled(true); // Cancel the chat message
            
            String message = event.getMessage();
            
            // Process the quantity input
            try {
                int quantity = Integer.parseInt(message);
                
                if (quantity <= 0) {
                    player.sendMessage(plugin.getConfig().getString("messages.quantity-greater-than-zero", "§cA quantidade deve ser maior que zero!"));
                    plugin.getPendingPurchaseManager().removePendingPurchase(player.getUniqueId());
                    return;
                }
                
                // Get the pending purchase
                PendingPurchase pendingPurchase = plugin.getPendingPurchaseManager().getPendingPurchase(player.getUniqueId());
                ShopItem item = pendingPurchase.getItem();
                
                // Execute the purchase (must be done on the main thread)
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (plugin.getShopManager().buyItem(player, item, quantity)) {
                        double totalPrice = item.getPrice() * quantity;
                        String formattedPrice = plugin.getEconomyManager().formatMoney(totalPrice);
                        
                        player.sendMessage(plugin.getConfig().getString("messages.item-bought", "§aVocê comprou §f{quantity}x {item} §apor §f{price}")
                                .replace("{quantity}", String.valueOf(quantity))
                                .replace("{item}", item.getName())
                                .replace("{price}", formattedPrice));
                        
                        plugin.getBehaviorManager().recordBehavior(player.getUniqueId(), "purchase", item.getId());
                    }
                    
                    // Remove the pending purchase
                    plugin.getPendingPurchaseManager().removePendingPurchase(player.getUniqueId());
                });
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getConfig().getString("messages.invalid-number", "§cPor favor, digite um número válido!"));
                plugin.getPendingPurchaseManager().removePendingPurchase(player.getUniqueId());
            }
        }
    }
}
