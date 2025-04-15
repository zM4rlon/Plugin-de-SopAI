package com.example.shopai;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.example.shopai.commands.ShopCommand;
import com.example.shopai.listeners.ChatListener;
import com.example.shopai.listeners.PlayerListener;
import com.example.shopai.managers.BehaviorManager;
import com.example.shopai.managers.EconomyManager;
import com.example.shopai.managers.PriceManager;
import com.example.shopai.managers.ShopManager;
import com.example.shopai.managers.PendingPurchaseManager;

public class ShopAIPlugin extends JavaPlugin {
    
    private ShopManager shopManager;
    private BehaviorManager behaviorManager;
    private PriceManager priceManager;
    private PendingPurchaseManager pendingPurchaseManager;
    private EconomyManager economyManager;
    
    @Override
    public void onEnable() {
        // Salvar configuração padrão
        saveDefaultConfig();
        
        // Inicializar gerenciadores
        this.economyManager = new EconomyManager(this);
        this.behaviorManager = new BehaviorManager(this);
        this.priceManager = new PriceManager(this);
        this.shopManager = new ShopManager(this, priceManager, economyManager);
        this.pendingPurchaseManager = new PendingPurchaseManager();
        
        // Registrar comandos
        getCommand("loja").setExecutor(new ShopCommand(this));
        
        // Registrar listeners de eventos
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        
        // Agendar atualizações de preços
        schedulePriceUpdates();
        
        getLogger().info("ShopAI Plugin foi ativado!");
        
        if (economyManager.isEconomyEnabled()) {
            getLogger().info("Integração com Vault ativada com sucesso!");
        } else {
            getLogger().warning("Vault não encontrado ou nenhum plugin de economia está instalado!");
            getLogger().warning("O plugin funcionará com o sistema de lingotes de ouro como fallback.");
        }
    }
    
    @Override
    public void onDisable() {
        // Salvar dados
        behaviorManager.saveData();
        priceManager.saveData();
        
        getLogger().info("ShopAI Plugin foi desativado!");
    }
    
    private void schedulePriceUpdates() {
        long updateInterval = getConfig().getLong("price-update-interval", 72000); // Padrão: 1 hora
        
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            getLogger().info("Atualizando preços com base no comportamento dos jogadores...");
            priceManager.updatePrices();
        }, updateInterval, updateInterval);
    }
    
    public ShopManager getShopManager() {
        return shopManager;
    }
    
    public BehaviorManager getBehaviorManager() {
        return behaviorManager;
    }
    
    public PriceManager getPriceManager() {
        return priceManager;
    }
    
    public PendingPurchaseManager getPendingPurchaseManager() {
        return pendingPurchaseManager;
    }
    
    public EconomyManager getEconomyManager() {
        return economyManager;
    }
}
