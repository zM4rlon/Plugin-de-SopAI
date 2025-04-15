package com.example.shopai.models;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

public class ShopCategory {
    
    private final String id;
    private final String name;
    private final Material icon;
    private final List<ShopItem> items;
    
    public ShopCategory(String id, String name, Material icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.items = new ArrayList<>();
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public Material getIcon() {
        return icon;
    }
    
    public List<ShopItem> getItems() {
        return new ArrayList<>(items);
    }
    
    public void addItem(ShopItem item) {
        items.add(item);
    }
}
