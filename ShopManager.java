package com.example.shopai.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.example.shopai.ShopAIPlugin;
import com.example.shopai.models.ShopCategory;
import com.example.shopai.models.ShopItem;

public class ShopManager {
    
    private final ShopAIPlugin plugin;
    private final PriceManager priceManager;
    private final EconomyManager economyManager;
    private final List<ShopCategory> categories;
    private final Map<String, ShopItem> itemsById;
    
    public ShopManager(ShopAIPlugin plugin, PriceManager priceManager, EconomyManager economyManager) {
        this.plugin = plugin;
        this.priceManager = priceManager;
        this.economyManager = economyManager;
        this.categories = new ArrayList<>();
        this.itemsById = new HashMap<>();
        
        loadShopCategories();
    }
    
    private void loadShopCategories() {
        ConfigurationSection categoriesSection = plugin.getConfig().getConfigurationSection("shop.categories");
        
        if (categoriesSection != null) {
            for (String categoryId : categoriesSection.getKeys(false)) {
                ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryId);
                
                if (categorySection != null) {
                    String name = categorySection.getString("name", "Categoria");
                    Material icon = Material.valueOf(categorySection.getString("icon", "CHEST"));
                    
                    ShopCategory category = new ShopCategory(categoryId, name, icon);
                    categories.add(category);
                    
                    // Carregar itens para esta categoria
                    ConfigurationSection itemsSection = categorySection.getConfigurationSection("items");
                    if (itemsSection != null) {
                        for (String itemId : itemsSection.getKeys(false)) {
                            ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemId);
                            
                            if (itemSection != null) {
                                String itemName = itemSection.getString("name", "Item");
                                Material material = Material.valueOf(itemSection.getString("material", "STONE"));
                                double buyPrice = itemSection.getDouble("buy-price", 100.0);
                                double sellPrice = itemSection.getDouble("sell-price", buyPrice * 0.7);
                                List<String> lore = itemSection.getStringList("lore");
                                
                                ShopItem item = new ShopItem(itemId, itemName, material, buyPrice, sellPrice, lore);
                                category.addItem(item);
                                itemsById.put(itemId, item);
                                
                                // Inicializar preço no gerenciador de preços
                                priceManager.initializePrice(itemId, buyPrice);
                            }
                        }
                    }
                }
            }
        }
        
        // Se nenhuma categoria foi carregada, adicionar as padrões
        if (categories.isEmpty()) {
            addDefaultCategories();
        }
    }
    
    private void addDefaultCategories() {
        // Criar categorias padrão
        ShopCategory blocks = new ShopCategory("blocks", "Blocos", Material.GRASS_BLOCK);
        ShopCategory drops = new ShopCategory("drops", "Drops", Material.BLAZE_ROD);
        ShopCategory farm = new ShopCategory("farm", "Fazenda", Material.WHEAT);
        ShopCategory dyes = new ShopCategory("dyes", "Corantes", Material.RED_DYE);
        ShopCategory decoration = new ShopCategory("decoration", "Decoração", Material.FLOWER_POT);
        ShopCategory fish = new ShopCategory("fish", "Peixes", Material.COD);
        ShopCategory ores = new ShopCategory("ores", "Minérios", Material.DIAMOND_ORE);
        ShopCategory food = new ShopCategory("food", "Comidas", Material.COOKED_BEEF);
        ShopCategory special = new ShopCategory("special", "Especiais", Material.ENCHANTED_GOLDEN_APPLE);
        
        // Adicionar itens à categoria Blocos
        addItemToCategory(blocks, "stone", "Pedra", Material.STONE, 10.0, 5.0, "Um bloco básico de construção");
        addItemToCategory(blocks, "granite", "Granito", Material.GRANITE, 12.0, 6.0, "Um bloco decorativo");
        addItemToCategory(blocks, "diorite", "Diorito", Material.DIORITE, 12.0, 6.0, "Um bloco decorativo");
        addItemToCategory(blocks, "andesite", "Andesito", Material.ANDESITE, 12.0, 6.0, "Um bloco decorativo");
        addItemToCategory(blocks, "cobblestone", "Pedregulho", Material.COBBLESTONE, 8.0, 4.0, "Um bloco básico de construção");
        addItemToCategory(blocks, "oak_planks", "Tábuas de Carvalho", Material.OAK_PLANKS, 15.0, 7.5, "Um bloco de madeira");
        addItemToCategory(blocks, "spruce_planks", "Tábuas de Pinheiro", Material.SPRUCE_PLANKS, 15.0, 7.5, "Um bloco de madeira");
        addItemToCategory(blocks, "birch_planks", "Tábuas de Bétula", Material.BIRCH_PLANKS, 15.0, 7.5, "Um bloco de madeira");
        addItemToCategory(blocks, "jungle_planks", "Tábuas de Selva", Material.JUNGLE_PLANKS, 15.0, 7.5, "Um bloco de madeira");
        addItemToCategory(blocks, "acacia_planks", "Tábuas de Acácia", Material.ACACIA_PLANKS, 15.0, 7.5, "Um bloco de madeira");
        addItemToCategory(blocks, "dark_oak_planks", "Tábuas de Carvalho Escuro", Material.DARK_OAK_PLANKS, 15.0, 7.5, "Um bloco de madeira");
        addItemToCategory(blocks, "sand", "Areia", Material.SAND, 10.0, 5.0, "Um bloco básico");
        addItemToCategory(blocks, "red_sand", "Areia Vermelha", Material.RED_SAND, 12.0, 6.0, "Um bloco decorativo");
        addItemToCategory(blocks, "gravel", "Cascalho", Material.GRAVEL, 10.0, 5.0, "Um bloco básico");
        addItemToCategory(blocks, "glass", "Vidro", Material.GLASS, 20.0, 10.0, "Um bloco transparente");
        addItemToCategory(blocks, "smooth_stone", "Pedra Lisa", Material.SMOOTH_STONE, 15.0, 7.5, "Um bloco decorativo");
        addItemToCategory(blocks, "bricks", "Tijolos", Material.BRICKS, 25.0, 12.5, "Um bloco decorativo");
        addItemToCategory(blocks, "bookshelf", "Estante", Material.BOOKSHELF, 50.0, 25.0, "Um bloco decorativo");
        addItemToCategory(blocks, "obsidian", "Obsidiana", Material.OBSIDIAN, 100.0, 50.0, "Um bloco resistente");
        addItemToCategory(blocks, "quartz_block", "Bloco de Quartzo", Material.QUARTZ_BLOCK, 40.0, 20.0, "Um bloco decorativo");
        
        // Adicionar itens à categoria Drops
        addItemToCategory(drops, "blaze_rod", "Vara de Blaze", Material.BLAZE_ROD, 80.0, 40.0, "Um drop de Blaze");
        addItemToCategory(drops, "bone", "Osso", Material.BONE, 20.0, 10.0, "Um drop de Esqueleto");
        addItemToCategory(drops, "ender_pearl", "Pérola do End", Material.ENDER_PEARL, 100.0, 50.0, "Um drop de Enderman");
        addItemToCategory(drops, "ghast_tear", "Lágrima de Ghast", Material.GHAST_TEAR, 120.0, 60.0, "Um drop de Ghast");
        addItemToCategory(drops, "gunpowder", "Pólvora", Material.GUNPOWDER, 30.0, 15.0, "Um drop de Creeper");
        addItemToCategory(drops, "magma_cream", "Creme de Magma", Material.MAGMA_CREAM, 60.0, 30.0, "Um drop de Cubo de Magma");
        addItemToCategory(drops, "rotten_flesh", "Carne Podre", Material.ROTTEN_FLESH, 10.0, 5.0, "Um drop de Zumbi");
        addItemToCategory(drops, "slime_ball", "Bola de Slime", Material.SLIME_BALL, 40.0, 20.0, "Um drop de Slime");
        addItemToCategory(drops, "spider_eye", "Olho de Aranha", Material.SPIDER_EYE, 25.0, 12.5, "Um drop de Aranha");
        addItemToCategory(drops, "string", "Linha", Material.STRING, 15.0, 7.5, "Um drop de Aranha");
        addItemToCategory(drops, "prismarine_crystals", "Cristais de Prismarino", Material.PRISMARINE_CRYSTALS, 70.0, 35.0, "Um drop de Guardiões");
        addItemToCategory(drops, "prismarine_shard", "Fragmento de Prismarino", Material.PRISMARINE_SHARD, 70.0, 35.0, "Um drop de Guardiões");
        
        // Adicionar itens à categoria Fazenda
        addItemToCategory(farm, "wheat", "Trigo", Material.WHEAT, 15.0, 7.5, "Um item de fazenda");
        addItemToCategory(farm, "melon_slice", "Fatia de Melancia", Material.MELON_SLICE, 10.0, 5.0, "Um item de fazenda");
        addItemToCategory(farm, "honeycomb", "Favo de Mel", Material.HONEYCOMB, 30.0, 15.0, "Um item de apicultura");
        addItemToCategory(farm, "crimson_fungus", "Fungo Carmesim", Material.CRIMSON_FUNGUS, 25.0, 12.5, "Um fungo do Nether");
        addItemToCategory(farm, "warped_fungus", "Fungo Distorcido", Material.WARPED_FUNGUS, 25.0, 12.5, "Um fungo do Nether");
        addItemToCategory(farm, "cactus", "Cacto", Material.CACTUS, 15.0, 7.5, "Uma planta do deserto");
        addItemToCategory(farm, "cocoa_beans", "Sementes de Cacau", Material.COCOA_BEANS, 20.0, 10.0, "Um item de fazenda");
        addItemToCategory(farm, "sweet_berries", "Bagas Doces", Material.SWEET_BERRIES, 15.0, 7.5, "Um item de fazenda");
        addItemToCategory(farm, "sugar_cane", "Cana-de-Açúcar", Material.SUGAR_CANE, 15.0, 7.5, "Um item de fazenda");
        addItemToCategory(farm, "pumpkin", "Abóbora", Material.PUMPKIN, 20.0, 10.0, "Um item de fazenda");
        addItemToCategory(farm, "carrot", "Cenoura", Material.CARROT, 15.0, 7.5, "Um item de fazenda");
        addItemToCategory(farm, "potato", "Batata", Material.POTATO, 15.0, 7.5, "Um item de fazenda");
        addItemToCategory(farm, "beetroot", "Beterraba", Material.BEETROOT, 15.0, 7.5, "Um item de fazenda");
        addItemToCategory(farm, "bamboo", "Bambu", Material.BAMBOO, 20.0, 10.0, "Uma planta de floresta");
        addItemToCategory(farm, "kelp", "Alga", Material.KELP, 10.0, 5.0, "Uma planta aquática");
        
        // Adicionar itens à categoria Corantes
        addItemToCategory(dyes, "white_dye", "Corante Branco", Material.WHITE_DYE, 15.0, 7.5, "Um corante");
        addItemToCategory(dyes, "orange_dye", "Corante Laranja", Material.ORANGE_DYE, 15.0, 7.5, "Um corante");
        addItemToCategory(dyes, "magenta_dye", "Corante Magenta", Material.MAGENTA_DYE, 15.0, 7.5, "Um corante");
        addItemToCategory(dyes, "light_blue_dye", "Corante Azul Claro", Material.LIGHT_BLUE_DYE, 15.0, 7.5, "Um corante");
        addItemToCategory(dyes, "yellow_dye", "Corante Amarelo", Material.YELLOW_DYE, 15.0, 7.5, "Um corante");
        addItemToCategory(dyes, "lime_dye", "Corante Verde Limão", Material.LIME_DYE, 15.0, 7.5, "Um corante");
        addItemToCategory(dyes, "pink_dye", "Corante Rosa", Material.PINK_DYE, 15.0, 7.5, "Um corante");
        addItemToCategory(dyes, "gray_dye", "Corante Cinza", Material.GRAY_DYE, 15.0, 7.5, "Um corante");
        addItemToCategory(dyes, "light_gray_dye", "Corante Cinza Claro", Material.LIGHT_GRAY_DYE, 15.0, 7.5, "Um corante");
        addItemToCategory(dyes, "cyan_dye", "Corante Ciano", Material.CYAN_DYE, 15.0, 7.5, "Um corante");
        addItemToCategory(dyes, "purple_dye", "Corante Roxo", Material.PURPLE_DYE, 15.0, 7.5, "Um corante");
        addItemToCategory(dyes, "blue_dye", "Corante Azul", Material.BLUE_DYE, 15.0, 7.5, "Um corante");
        addItemToCategory(dyes, "brown_dye", "Corante Marrom", Material.BROWN_DYE, 15.0, 7.5, "Um corante");
        addItemToCategory(dyes, "green_dye", "Corante Verde", Material.GREEN_DYE, 15.0, 7.5, "Um corante");
        addItemToCategory(dyes, "red_dye", "Corante Vermelho", Material.RED_DYE, 15.0, 7.5, "Um corante");
        addItemToCategory(dyes, "black_dye", "Corante Preto", Material.BLACK_DYE, 15.0, 7.5, "Um corante");
        
        // Adicionar itens à categoria Decoração
        addItemToCategory(decoration, "flower_pot", "Vaso de Flores", Material.FLOWER_POT, 30.0, 15.0, "Um item decorativo");
        addItemToCategory(decoration, "painting", "Pintura", Material.PAINTING, 25.0, 12.5, "Um item decorativo");
        addItemToCategory(decoration, "item_frame", "Moldura", Material.ITEM_FRAME, 20.0, 10.0, "Um item decorativo");
        addItemToCategory(decoration, "armor_stand", "Suporte de Armadura", Material.ARMOR_STAND, 50.0, 25.0, "Um item decorativo");
        addItemToCategory(decoration, "lantern", "Lanterna", Material.LANTERN, 35.0, 17.5, "Um item decorativo luminoso");
        addItemToCategory(decoration, "sea_lantern", "Lanterna do Mar", Material.SEA_LANTERN, 60.0, 30.0, "Um item decorativo luminoso");
        addItemToCategory(decoration, "glowstone", "Pedra Luminosa", Material.GLOWSTONE, 50.0, 25.0, "Um item decorativo luminoso");
        addItemToCategory(decoration, "redstone_lamp", "Lâmpada de Redstone", Material.REDSTONE_LAMP, 45.0, 22.5, "Um item decorativo luminoso");
        addItemToCategory(decoration, "end_rod", "Bastão do End", Material.END_ROD, 70.0, 35.0, "Um item decorativo luminoso");
        addItemToCategory(decoration, "bell", "Sino", Material.BELL, 100.0, 50.0, "Um item decorativo interativo");
        addItemToCategory(decoration, "chain", "Corrente", Material.CHAIN, 30.0, 15.0, "Um item decorativo");
        addItemToCategory(decoration, "campfire", "Fogueira", Material.CAMPFIRE, 40.0, 20.0, "Um item decorativo funcional");
        addItemToCategory(decoration, "soul_campfire", "Fogueira de Almas", Material.SOUL_CAMPFIRE, 50.0, 25.0, "Um item decorativo funcional");
        
        // Adicionar itens à categoria Peixes
        addItemToCategory(fish, "cod", "Bacalhau", Material.COD, 15.0, 7.5, "Um peixe comum");
        addItemToCategory(fish, "salmon", "Salmão", Material.SALMON, 20.0, 10.0, "Um peixe comum");
        addItemToCategory(fish, "tropical_fish", "Peixe Tropical", Material.TROPICAL_FISH, 30.0, 15.0, "Um peixe exótico");
        addItemToCategory(fish, "pufferfish", "Baiacu", Material.PUFFERFISH, 25.0, 12.5, "Um peixe venenoso");
        
        // Adicionar itens à categoria Minérios
        addItemToCategory(ores, "coal", "Carvão", Material.COAL, 15.0, 7.5, "Um minério comum");
        addItemToCategory(ores, "iron_ingot", "Lingote de Ferro", Material.IRON_INGOT, 50.0, 25.0, "Um minério comum");
        addItemToCategory(ores, "gold_ingot", "Lingote de Ouro", Material.GOLD_INGOT, 100.0, 50.0, "Um minério valioso");
        addItemToCategory(ores, "diamond", "Diamante", Material.DIAMOND, 500.0, 250.0, "Um minério raro");
        addItemToCategory(ores, "emerald", "Esmeralda", Material.EMERALD, 600.0, 300.0, "Um minério muito raro");
        addItemToCategory(ores, "lapis_lazuli", "Lápis-Lazúli", Material.LAPIS_LAZULI, 40.0, 20.0, "Um minério para encantamentos");
        addItemToCategory(ores, "redstone", "Redstone", Material.REDSTONE, 30.0, 15.0, "Um minério para circuitos");
        addItemToCategory(ores, "quartz", "Quartzo", Material.QUARTZ, 35.0, 17.5, "Um minério do Nether");
        addItemToCategory(ores, "netherite_ingot", "Lingote de Netherita", Material.NETHERITE_INGOT, 2000.0, 1000.0, "O minério mais raro");
        
        // Adicionar itens à categoria Comidas
        addItemToCategory(food, "cooked_beef", "Bife Assado", Material.COOKED_BEEF, 30.0, 15.0, "Uma comida nutritiva");
        addItemToCategory(food, "cooked_chicken", "Frango Assado", Material.COOKED_CHICKEN, 25.0, 12.5, "Uma comida nutritiva");
        addItemToCategory(food, "cooked_porkchop", "Costeleta de Porco Assada", Material.COOKED_PORKCHOP, 30.0, 15.0, "Uma comida nutritiva");
        addItemToCategory(food, "cooked_mutton", "Carneiro Assado", Material.COOKED_MUTTON, 30.0, 15.0, "Uma comida nutritiva");
        addItemToCategory(food, "cooked_rabbit", "Coelho Assado", Material.COOKED_RABBIT, 35.0, 17.5, "Uma comida nutritiva");
        addItemToCategory(food, "bread", "Pão", Material.BREAD, 20.0, 10.0, "Uma comida básica");
        addItemToCategory(food, "cookie", "Biscoito", Material.COOKIE, 10.0, 5.0, "Um lanche rápido");
        addItemToCategory(food, "cake", "Bolo", Material.CAKE, 100.0, 50.0, "Uma sobremesa especial");
        addItemToCategory(food, "pumpkin_pie", "Torta de Abóbora", Material.PUMPKIN_PIE, 40.0, 20.0, "Uma sobremesa nutritiva");
        addItemToCategory(food, "golden_carrot", "Cenoura Dourada", Material.GOLDEN_CARROT, 120.0, 60.0, "Uma comida especial");
        addItemToCategory(food, "golden_apple", "Maçã Dourada", Material.GOLDEN_APPLE, 500.0, 250.0, "Uma comida mágica");
        
        // Adicionar itens à categoria Especiais
        addItemToCategory(special, "enchanted_golden_apple", "Maçã Dourada Encantada", Material.ENCHANTED_GOLDEN_APPLE, 5000.0, 2500.0, "O item mais raro do jogo");
        addItemToCategory(special, "smithing_template", "Molde de Ferraria", Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 3000.0, 1500.0, "Um item para melhorar equipamentos");
        
        // Adicionar categorias à lista
        categories.add(blocks);
        categories.add(drops);
        categories.add(farm);
        categories.add(dyes);
        categories.add(decoration);
        categories.add(fish);
        categories.add(ores);
        categories.add(food);
        categories.add(special);
        
        // Salvar categorias padrão na configuração
        saveDefaultCategoriesToConfig();
    }
    
    private void addItemToCategory(ShopCategory category, String id, String name, Material material, double buyPrice, double sellPrice, String loreText) {
        List<String> lore = new ArrayList<>();
        lore.add(loreText);
        
        ShopItem item = new ShopItem(id, name, material, buyPrice, sellPrice, lore);
        category.addItem(item);
        itemsById.put(id, item);
        priceManager.initializePrice(id, buyPrice);
    }
    
    private void saveDefaultCategoriesToConfig() {
        for (ShopCategory category : categories) {
            String categoryPath = "shop.categories." + category.getId();
            plugin.getConfig().set(categoryPath + ".name", category.getName());
            plugin.getConfig().set(categoryPath + ".icon", category.getIcon().toString());
            
            for (ShopItem item : category.getItems()) {
                String itemPath = categoryPath + ".items." + item.getId();
                plugin.getConfig().set(itemPath + ".name", item.getName());
                plugin.getConfig().set(itemPath + ".material", item.getMaterial().toString());
                plugin.getConfig().set(itemPath + ".buy-price", item.getBuyPrice());
                plugin.getConfig().set(itemPath + ".sell-price", item.getSellPrice());
                plugin.getConfig().set(itemPath + ".lore", item.getLore());
            }
        }
        
        plugin.saveConfig();
    }
    
    public boolean buyItem(Player player, ShopItem item, int quantity) {
        // Obter preço atual
        double price = item.getBuyPrice();
        double totalPrice = price * quantity;
        
        // Verificar se o jogador tem dinheiro suficiente
        if (!economyManager.hasMoney(player, totalPrice)) {
            String formattedPrice = economyManager.formatMoney(totalPrice);
            player.sendMessage(plugin.getConfig().getString("messages.not-enough-money", "§cVocê não tem dinheiro suficiente para comprar este item!")
                    .replace("{price}", formattedPrice));
            return false;
        }
        
        // Retirar dinheiro
        if (!economyManager.withdrawMoney(player, totalPrice)) {
            player.sendMessage("§cErro ao processar o pagamento. Tente novamente.");
            return false;
        }
        
        // Dar itens
        ItemStack itemStack = new ItemStack(item.getMaterial(), quantity);
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(itemStack);
        
        if (!leftover.isEmpty()) {
            // Dropar itens que não couberam no inventário
            for (ItemStack stack : leftover.values()) {
                player.getWorld().dropItem(player.getLocation(), stack);
            }
            player.sendMessage("§eAlguns itens não couberam no seu inventário e foram jogados no chão.");
        }
        
        return true;
    }
    
    // Manter o método buyItem original para compatibilidade
    public boolean buyItem(Player player, ShopItem item) {
        return buyItem(player, item, 1);
    }
    
    public boolean sellItem(Player player, ShopItem item, boolean sellAll) {
        Material material = item.getMaterial();
        double sellPrice = item.getSellPrice();
        
        if (sellAll) {
            // Contar quantos deste item o jogador tem
            int count = 0;
            for (ItemStack stack : player.getInventory().getContents()) {
                if (stack != null && stack.getType() == material) {
                    count += stack.getAmount();
                }
            }
            
            if (count == 0) {
                player.sendMessage(plugin.getConfig().getString("messages.no-items-to-sell", "§cVocê não tem nenhum {item} para vender!")
                        .replace("{item}", item.getName()));
                return false;
            }
            
            // Remover todos os itens deste tipo
            player.getInventory().remove(material);
            
            // Dar dinheiro
            double totalPayment = sellPrice * count;
            if (!economyManager.depositMoney(player, totalPayment)) {
                player.sendMessage("§cErro ao processar o pagamento. Tente novamente.");
                return false;
            }
            
            String formattedPrice = economyManager.formatMoney(totalPayment);
            player.sendMessage(plugin.getConfig().getString("messages.all-items-sold", "§aVocê vendeu todos os seus §f{item} §apara a loja")
                    .replace("{item}", item.getName())
                    .replace("{count}", String.valueOf(count))
                    .replace("{price}", formattedPrice));
            return true;
        } else {
            // Vender apenas um
            if (!player.getInventory().contains(material)) {
                player.sendMessage(plugin.getConfig().getString("messages.no-items-to-sell", "§cVocê não tem nenhum {item} para vender!")
                        .replace("{item}", item.getName()));
                return false;
            }
            
            // Remover um deste item
            ItemStack itemToRemove = new ItemStack(material, 1);
            player.getInventory().removeItem(itemToRemove);
            
            // Dar dinheiro
            if (!economyManager.depositMoney(player, sellPrice)) {
                player.sendMessage("§cErro ao processar o pagamento. Tente novamente.");
                return false;
            }
            
            String formattedPrice = economyManager.formatMoney(sellPrice);
            player.sendMessage(plugin.getConfig().getString("messages.item-sold", "§aVocê vendeu §f{item} §apor §f{price}")
                    .replace("{item}", item.getName())
                    .replace("{price}", formattedPrice));
            
            return true;
        }
    }
    
    public List<ShopCategory> getCategories() {
        // Atualizar preços para todos os itens
        for (ShopCategory category : categories) {
            for (ShopItem item : category.getItems()) {
                double buyPrice = priceManager.getCurrentPrice(item.getId());
                item.setBuyPrice(buyPrice);
                item.setSellPrice(buyPrice * 0.7); // Manter a proporção de venda
            }
        }
        
        return categories;
    }
    
    public ShopItem getItemById(String itemId) {
        ShopItem item = itemsById.get(itemId);
        if (item != null) {
            double buyPrice = priceManager.getCurrentPrice(itemId);
            item.setBuyPrice(buyPrice);
            item.setSellPrice(buyPrice * 0.7); // Manter a proporção de venda
        }
        return item;
    }
    
    public boolean addItemToShop(String itemId, String categoryId, double buyPrice, double sellPrice, Player player) {
        // Verificar se a categoria existe
        ShopCategory targetCategory = null;
        for (ShopCategory category : categories) {
            if (category.getId().equalsIgnoreCase(categoryId)) {
                targetCategory = category;
                break;
            }
        }
        
        if (targetCategory == null) {
            return false;
        }
        
        // Verificar se o item já existe
        if (itemsById.containsKey(itemId)) {
            return false;
        }
        
        // Obter o item da mão do jogador
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType() == Material.AIR) {
            return false;
        }
        
        Material material = handItem.getType();
        String name = handItem.hasItemMeta() && handItem.getItemMeta().hasDisplayName() 
                ? handItem.getItemMeta().getDisplayName() 
                : formatMaterialName(material.toString());
        
        List<String> lore = new ArrayList<>();
        lore.add("Item adicionado por " + player.getName());
        
        // Criar e adicionar o item
        ShopItem newItem = new ShopItem(itemId, name, material, buyPrice, sellPrice, lore);
        targetCategory.addItem(newItem);
        itemsById.put(itemId, newItem);
        priceManager.initializePrice(itemId, buyPrice);
        
        // Salvar na configuração
        String itemPath = "shop.categories." + categoryId + ".items." + itemId;
        plugin.getConfig().set(itemPath + ".name", name);
        plugin.getConfig().set(itemPath + ".material", material.toString());
        plugin.getConfig().set(itemPath + ".buy-price", buyPrice);
        plugin.getConfig().set(itemPath + ".sell-price", sellPrice);
        plugin.getConfig().set(itemPath + ".lore", lore);
        plugin.saveConfig();
        
        return true;
    }
    
    private String formatMaterialName(String materialName) {
        String[] parts = materialName.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        
        for (String part : parts) {
            if (part.length() > 0) {
                result.append(Character.toUpperCase(part.charAt(0)))
                      .append(part.substring(1))
                      .append(" ");
            }
        }
        
        return result.toString().trim();
    }
    
    public void reload() {
        // Limpar dados atuais
        categories.clear();
        itemsById.clear();
        
        // Recarregar configuração
        plugin.reloadConfig();
        
        // Carregar categorias e itens
        loadShopCategories();
    }
}
