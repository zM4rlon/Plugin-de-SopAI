package com.example.shopai.models;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

public class ShopItem {
    
    private final String id;
    private final String name;
    private final Material material;
    private final double basePrice;
    private double buyPrice;
    private double sellPrice;
    private final List<String> lore;
    
    public ShopItem(String id, String name, Material material, double basePrice, List<String> lore) {
        this.id = id;
        this.name = name;
        this.material = material;
        this.basePrice = basePrice;
        this.buyPrice = basePrice;
        this.sellPrice = basePrice * 0.7; // Venda por padrão é 70% do preço de compra
        this.lore = lore != null ? lore : new ArrayList<>();
    }
    
    public ShopItem(String id, String name, Material material, double buyPrice, double sellPrice, List<String> lore) {
        this.id = id;
        this.name = name;
        this.material = material;
        this.basePrice = buyPrice; // Usamos o preço de compra como base
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.lore = lore != null ? lore : new ArrayList<>();
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public double getBasePrice() {
        return basePrice;
    }
    
    public double getBuyPrice() {
        return buyPrice;
    }
    
    public void setBuyPrice(double price) {
        this.buyPrice = price;
    }
    
    public double getSellPrice() {
        return sellPrice;
    }
    
    public void setSellPrice(double price) {
        this.sellPrice = price;
    }
    
    // Para compatibilidade com código existente
    public double getPrice() {
        return buyPrice;
    }
    
    public void setPrice(double price) {
        this.buyPrice = price;
        this.sellPrice = price * 0.7;
    }
    
    public List<String> getLore() {
        return new ArrayList<>(lore);
    }
}
