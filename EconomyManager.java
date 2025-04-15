package com.example.shopai.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.example.shopai.ShopAIPlugin;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class EconomyManager {
    
    private final ShopAIPlugin plugin;
    private Economy economy;
    private boolean economyEnabled;
    
    public EconomyManager(ShopAIPlugin plugin) {
        this.plugin = plugin;
        this.economyEnabled = setupEconomy();
        
        if (!economyEnabled) {
            plugin.getLogger().warning("Vault não encontrado ou nenhum plugin de economia está instalado!");
            plugin.getLogger().warning("O plugin funcionará com o sistema de lingotes de ouro como fallback.");
        }
    }
    
    private boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        
        economy = rsp.getProvider();
        return economy != null;
    }
    
    public boolean isEconomyEnabled() {
        return economyEnabled;
    }
    
    public boolean hasMoney(Player player, double amount) {
        if (!economyEnabled) {
            // Fallback to gold ingot system
            return player.getInventory().containsAtLeast(new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLD_INGOT), (int)amount);
        }
        
        return economy.has(player, amount);
    }
    
    public boolean withdrawMoney(Player player, double amount) {
        if (!economyEnabled) {
            // Fallback to gold ingot system
            if (player.getInventory().containsAtLeast(new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLD_INGOT), (int)amount)) {
                player.getInventory().removeItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLD_INGOT, (int)amount));
                return true;
            }
            return false;
        }
        
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        return response.transactionSuccess();
    }
    
    public boolean depositMoney(Player player, double amount) {
        if (!economyEnabled) {
            // Fallback to gold ingot system
            java.util.HashMap<Integer, org.bukkit.inventory.ItemStack> leftover = 
                player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLD_INGOT, (int)amount));
            
            if (!leftover.isEmpty()) {
                // Drop items that didn't fit in inventory
                for (org.bukkit.inventory.ItemStack stack : leftover.values()) {
                    player.getWorld().dropItem(player.getLocation(), stack);
                }
            }
            return true;
        }
        
        EconomyResponse response = economy.depositPlayer(player, amount);
        return response.transactionSuccess();
    }
    
    public String formatMoney(double amount) {
        if (!economyEnabled) {
            return (int)amount + " ouro";
        }
        
        return economy.format(amount);
    }
    
    public String getCurrencyName() {
        if (!economyEnabled) {
            return "ouro";
        }
        
        return economy.currencyNamePlural();
    }
}
