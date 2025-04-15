package com.example.shopai.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.example.shopai.ShopAIPlugin;
import com.example.shopai.gui.ShopGUI;

public class ShopCommand implements CommandExecutor {
    
    private final ShopAIPlugin plugin;
    
    public ShopCommand(ShopAIPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Comando /shop sem argumentos - abrir a loja
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cApenas jogadores podem usar este comando.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("shopai.use")) {
                player.sendMessage("§cVocê não tem permissão para usar este comando.");
                return true;
            }
            
            // Abrir GUI da loja
            ShopGUI shopGUI = new ShopGUI(plugin, player);
            shopGUI.open();
            
            // Registrar comportamento - jogador mostrou interesse ao abrir a loja
            plugin.getBehaviorManager().recordBehavior(player.getUniqueId(), "interest", null);
            
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            // Comando /shop reload - recarregar configuração
            if (!sender.hasPermission("shopai.admin")) {
                sender.sendMessage("§cVocê não tem permissão para recarregar o plugin.");
                return true;
            }
            
            plugin.getShopManager().reload();
            sender.sendMessage("§aPlugin ShopAI recarregado com sucesso!");
            return true;
        } else if (args.length == 5 && args[0].equalsIgnoreCase("adicionar")) {
            // Comando /shop adicionar [ID] [CATEGORIA] [PREÇO_COMPRA] [PREÇO_VENDA]
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cApenas jogadores podem usar este comando.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("shopai.admin")) {
                player.sendMessage("§cVocê não tem permissão para adicionar itens à loja.");
                return true;
            }
            
            String itemId = args[1];
            String categoryId = args[2];
            
            double buyPrice;
            double sellPrice;
            
            try {
                buyPrice = Double.parseDouble(args[3]);
                sellPrice = Double.parseDouble(args[4]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cOs preços devem ser números válidos.");
                return true;
            }
            
            if (buyPrice <= 0 || sellPrice <= 0) {
                player.sendMessage("§cOs preços devem ser maiores que zero.");
                return true;
            }
            
            if (plugin.getShopManager().addItemToShop(itemId, categoryId, buyPrice, sellPrice, player)) {
                player.sendMessage("§aItem adicionado com sucesso à loja!");
            } else {
                player.sendMessage("§cNão foi possível adicionar o item. Verifique se a categoria existe e se você está segurando um item.");
            }
            
            return true;
        }
        
        // Comando inválido - mostrar ajuda
        sender.sendMessage("§cUso: /" + label + " [reload|adicionar <id> <categoria> <preço_compra> <preço_venda>]");
        return true;
    }
}
