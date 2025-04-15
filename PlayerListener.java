package com.example.shopai.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.example.shopai.ShopAIPlugin;
import com.example.shopai.gui.ShopGUI;
import com.example.shopai.models.ShopCategory;
import com.example.shopai.models.ShopItem;

public class PlayerListener implements Listener {
    
    private final ShopAIPlugin plugin;
    
    public PlayerListener(ShopAIPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (title.startsWith("§8Loja")) {
            event.setCancelled(true);
            
            if (event.getCurrentItem() == null) return;
            
            Player player = (Player) event.getWhoClicked();
            int slot = event.getRawSlot();
            
            // Criar uma nova instância de ShopGUI para este jogador
            ShopGUI shopGUI = new ShopGUI(plugin, player);
            
            if (title.equals("§8Loja - Categorias")) {
                // Menu principal - lidar com seleção de categoria
                ShopCategory category = shopGUI.getCategoryFromSlot(slot);
                if (category != null) {
                    shopGUI.createCategoryMenu(category);
                    shopGUI.open();
                }
            } else if (title.startsWith("§8Loja - ")) {
                // Menu de categoria
                
                // Verificar se é o botão voltar
                if (shopGUI.isBackButton(slot)) {
                    // Voltar para o menu principal
                    shopGUI = new ShopGUI(plugin, player);
                    shopGUI.open();
                    return;
                }
                
                // Verificar se é o botão de página anterior
                if (shopGUI.isPrevPageButton(slot)) {
                    // Obter a categoria atual do título
                    String categoryName = title.substring(8, title.lastIndexOf(" (Pág."));
                    ShopCategory category = null;
                    
                    // Encontrar a categoria pelo nome
                    for (ShopCategory cat : plugin.getShopManager().getCategories()) {
                        if (cat.getName().equals(categoryName)) {
                            category = cat;
                            break;
                        }
                    }
                    
                    if (category != null) {
                        shopGUI.createCategoryMenu(category);
                        shopGUI.prevPage();
                        shopGUI.open();
                    }
                    return;
                }
                
                // Verificar se é o botão de próxima página
                if (shopGUI.isNextPageButton(slot)) {
                    // Obter a categoria atual do título
                    String categoryName = title.substring(8, title.lastIndexOf(" (Pág."));
                    ShopCategory category = null;
                    
                    // Encontrar a categoria pelo nome
                    for (ShopCategory cat : plugin.getShopManager().getCategories()) {
                        if (cat.getName().equals(categoryName)) {
                            category = cat;
                            break;
                        }
                    }
                    
                    if (category != null) {
                        shopGUI.createCategoryMenu(category);
                        shopGUI.nextPage();
                        shopGUI.open();
                    }
                    return;
                }
                
                // Lidar com cliques em itens
                ShopItem item = shopGUI.getItemFromSlot(slot);
                if (item != null) {
                    if (event.isLeftClick()) {
                        // Perguntar quantidade no chat
                        player.closeInventory();
                        plugin.getPendingPurchaseManager().addPendingPurchase(player.getUniqueId(), item);
                        
                        String formattedPrice = plugin.getEconomyManager().formatMoney(item.getBuyPrice());
                        player.sendMessage(plugin.getConfig().getString("messages.enter-quantity", "§eDigite no chat a quantidade de §f{item} §eque deseja comprar:")
                                .replace("{item}", item.getName()));
                        player.sendMessage(plugin.getConfig().getString("messages.price-per-unit", "§7(Cada unidade custa §f{price}§7)")
                                .replace("{price}", formattedPrice));
                    } else if (event.isRightClick() && !event.isShiftClick()) {
                        // Vender item
                        if (plugin.getShopManager().sellItem(player, item, false)) {
                            plugin.getBehaviorManager().recordBehavior(player.getUniqueId(), "sell", item.getId());
                        }
                    } else if (event.isRightClick() && event.isShiftClick()) {
                        // Vender todos os itens deste tipo
                        if (plugin.getShopManager().sellItem(player, item, true)) {
                            plugin.getBehaviorManager().recordBehavior(player.getUniqueId(), "sell_all", item.getId());
                        }
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();
        if (title.startsWith("§8Loja")) {
            Player player = (Player) event.getPlayer();
            
            // Só registrar desinteresse se o jogador não tiver uma compra pendente
            if (!plugin.getPendingPurchaseManager().hasPendingPurchase(player.getUniqueId())) {
                plugin.getBehaviorManager().recordBehavior(player.getUniqueId(), "disinterest", null);
            }
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Carregar dados do jogador se necessário
        Player player = event.getPlayer();
        plugin.getBehaviorManager().loadPlayerData(player.getUniqueId());
    }
}
