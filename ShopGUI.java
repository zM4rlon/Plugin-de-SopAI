package com.example.shopai.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.example.shopai.ShopAIPlugin;
import com.example.shopai.models.ShopCategory;
import com.example.shopai.models.ShopItem;

public class ShopGUI {
    
    private final ShopAIPlugin plugin;
    private final Player player;
    private Inventory inventory;
    private ShopCategory currentCategory;
    private final Map<Integer, ShopItem> slotToItem;
    private final Map<Integer, ShopCategory> slotToCategory;
    private int currentPage = 0;
    private int maxPage = 0;
    
    private static final int ROWS = 6; // Inventário completo
    private static final int ROW_SIZE = 9;
    private static final int INVENTORY_SIZE = ROWS * ROW_SIZE;
    private static final int ITEMS_PER_PAGE = (ROWS - 1) * ROW_SIZE; // Última linha reservada para navegação
    
    public ShopGUI(ShopAIPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.slotToItem = new HashMap<>();
        this.slotToCategory = new HashMap<>();
        createMainMenu();
    }
    
    private void createMainMenu() {
        List<ShopCategory> categories = plugin.getShopManager().getCategories();
        
        // Sempre usar inventário completo (6 linhas)
        inventory = Bukkit.createInventory(null, INVENTORY_SIZE, "§8Loja - Categorias");
        
        // Calcular slots para centralizar as categorias
        int totalCategories = categories.size();
        int rows = (int) Math.ceil(totalCategories / 7.0); // Máximo 7 itens por linha para centralização
        int startRow = Math.max(1, (ROWS - rows) / 2); // Começar pelo menos na segunda linha
        
        for (int i = 0; i < totalCategories; i++) {
            ShopCategory category = categories.get(i);
            
            // Calcular posição centralizada
            int row = startRow + (i / 7);
            int col = 1 + (i % 7); // Começar da coluna 1 (índice 0-based)
            int slot = (row * ROW_SIZE) + col;
            
            if (slot >= INVENTORY_SIZE - ROW_SIZE) continue; // Evitar última linha
            
            ItemStack displayItem = new ItemStack(category.getIcon());
            ItemMeta meta = displayItem.getItemMeta();
            
            if (meta != null) {
                meta.setDisplayName("§e" + category.getName());
                
                List<String> lore = new ArrayList<>();
                lore.add("§7Clique para ver os itens desta categoria");
                
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
            }
            
            inventory.setItem(slot, displayItem);
            slotToCategory.put(slot, category);
        }
        
        // Adicionar bordas decorativas
        ItemStack border = createBorderItem();
        
        // Primeira e última linha completas
        for (int i = 0; i < ROW_SIZE; i++) {
            inventory.setItem(i, border); // Primeira linha
            inventory.setItem(INVENTORY_SIZE - ROW_SIZE + i, border); // Última linha
        }
        
        // Bordas laterais
        for (int i = 1; i < ROWS - 1; i++) {
            inventory.setItem(i * ROW_SIZE, border); // Borda esquerda
            inventory.setItem(i * ROW_SIZE + ROW_SIZE - 1, border); // Borda direita
        }
    }
    
    public void createCategoryMenu(ShopCategory category) {
        this.currentCategory = category;
        List<ShopItem> items = category.getItems();
        
        // Calcular número máximo de páginas
        maxPage = (int) Math.ceil(items.size() / (double) ITEMS_PER_PAGE) - 1;
        if (maxPage < 0) maxPage = 0;
        
        // Sempre usar inventário completo (6 linhas)
        inventory = Bukkit.createInventory(null, INVENTORY_SIZE, "§8Loja - " + category.getName() + " (Pág. " + (currentPage + 1) + ")");
        
        // Limpar mapeamentos anteriores
        slotToItem.clear();
        
        // Adicionar itens da página atual
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, items.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            ShopItem item = items.get(i);
            int slot = (i - startIndex);
            
            // Pular slots de borda
            if (slot % ROW_SIZE == 0 || slot % ROW_SIZE == ROW_SIZE - 1) {
                slot++; // Mover para o próximo slot
            }
            
            // Verificar se ultrapassou o limite da última linha
            if (slot >= INVENTORY_SIZE - ROW_SIZE) break;
            
            ItemStack displayItem = new ItemStack(item.getMaterial());
            ItemMeta meta = displayItem.getItemMeta();
            
            if (meta != null) {
                meta.setDisplayName("§e" + item.getName());
                
                List<String> lore = new ArrayList<>(item.getLore());
                lore.add("");
                lore.add("§aPreço de compra: §f" + plugin.getEconomyManager().formatMoney(item.getBuyPrice()));
                lore.add("§cPreço de venda: §f" + plugin.getEconomyManager().formatMoney(item.getSellPrice()));
                lore.add("");
                lore.add("§7Clique esquerdo para comprar");
                lore.add("§7Clique direito para vender");
                lore.add("§7Shift + Clique direito para vender todos");
                
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
            }
            
            inventory.setItem(slot, displayItem);
            slotToItem.put(slot, item);
        }
        
        // Adicionar bordas decorativas
        ItemStack border = createBorderItem();
        
        // Primeira linha completa
        for (int i = 0; i < ROW_SIZE; i++) {
            inventory.setItem(i, border);
        }
        
        // Bordas laterais
        for (int i = 1; i < ROWS - 1; i++) {
            inventory.setItem(i * ROW_SIZE, border); // Borda esquerda
            inventory.setItem(i * ROW_SIZE + ROW_SIZE - 1, border); // Borda direita
        }
        
        // Última linha (navegação)
        for (int i = 0; i < ROW_SIZE; i++) {
            inventory.setItem(INVENTORY_SIZE - ROW_SIZE + i, border);
        }
        
        // Botões de navegação na última linha
        
        // Botão voltar para o menu principal
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§cVoltar para o menu principal");
            backButton.setItemMeta(backMeta);
        }
        inventory.setItem(INVENTORY_SIZE - ROW_SIZE + 4, backButton);
        
        // Botão página anterior (se não for a primeira página)
        if (currentPage > 0) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName("§aPágina anterior");
                prevButton.setItemMeta(prevMeta);
            }
            inventory.setItem(INVENTORY_SIZE - ROW_SIZE + 2, prevButton);
        }
        
        // Botão próxima página (se não for a última página)
        if (currentPage < maxPage) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextButton.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName("§aPróxima página");
                nextButton.setItemMeta(nextMeta);
            }
            inventory.setItem(INVENTORY_SIZE - ROW_SIZE + 6, nextButton);
        }
    }
    
    private ItemStack createBorderItem() {
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(" ");
            border.setItemMeta(borderMeta);
        }
        return border;
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    public boolean isInCategoryMenu() {
        return currentCategory != null;
    }
    
    public boolean isBackButton(int slot) {
        if (!isInCategoryMenu()) return false;
        return slot == INVENTORY_SIZE - ROW_SIZE + 4; // Botão voltar no meio da última linha
    }
    
    public boolean isPrevPageButton(int slot) {
        if (!isInCategoryMenu()) return false;
        return slot == INVENTORY_SIZE - ROW_SIZE + 2; // Botão página anterior
    }
    
    public boolean isNextPageButton(int slot) {
        if (!isInCategoryMenu()) return false;
        return slot == INVENTORY_SIZE - ROW_SIZE + 6; // Botão próxima página
    }
    
    public void nextPage() {
        if (currentPage < maxPage) {
            currentPage++;
            createCategoryMenu(currentCategory);
        }
    }
    
    public void prevPage() {
        if (currentPage > 0) {
            currentPage--;
            createCategoryMenu(currentCategory);
        }
    }
    
    public ShopCategory getCurrentCategory() {
        return currentCategory;
    }
    
    public ShopCategory getCategoryFromSlot(int slot) {
        return slotToCategory.get(slot);
    }
    
    public ShopItem getItemFromSlot(int slot) {
        return slotToItem.get(slot);
    }
}
