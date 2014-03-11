package com.herocraftonline.townships.util;

import org.bukkit.inventory.ItemStack;

/**
 * Author: gabizou
 * BankItem Represents an ItemStack and an amount usable for CitizenGroup Banks.
 */
public class BankItem {

    private ItemStack itemStack;
    private int amount;

    public BankItem(ItemStack itemStack, int amount) {
        this.itemStack = itemStack.clone();
//        this.itemStack.setAmount(0);
        this.amount = amount;
    }

    public ItemStack getItem() {
        return itemStack;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public final int hashCode() {
        int hash = 1;
        hash = hash * 31 + itemStack.getTypeId();
        hash = hash * 31 + itemStack.getDurability() & 0xFFFF;
        hash = hash * 31 + (itemStack.hasItemMeta() ? itemStack.getItemMeta().hashCode() : 0 );
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof BankItem) &&
                ((BankItem) obj).getItem().getData().equals(itemStack.getData()) &&
                ((BankItem) obj).getItem().getType().equals(itemStack.getType()) &&
                ((BankItem) obj).getItem().getItemMeta().equals(itemStack.getItemMeta());
    }
}
