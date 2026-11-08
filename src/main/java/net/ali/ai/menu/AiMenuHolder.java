package net.ali.ai.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class AiMenuHolder implements InventoryHolder {
    private Inventory inventory;
    void attach(Inventory inventory) { this.inventory = inventory; }
    @Override public Inventory getInventory() { return inventory; }
}
