package com.herocraftonline.townships.util;

import com.herocraftonline.townships.api.CitizenGroupCenter;
import com.sk89q.worldedit.BlockVector2D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: gabizou
 */
public class Util {


    /**
     * Generate a usable list of BlockVectors with WorldEdit that makes a roughly circular region.
     * For use in calculating the polygonal region for Township creation.
     * @param location - Location of the center of the town
     * @param radius - Configured radius of the town size
     * @param amount - Number of points making a polygonal circle
     * @return
     */
    public static List<BlockVector2D> createCirclePoints(Location location, int radius, int amount) {
        List<BlockVector2D> points = new ArrayList<>(amount);
        for (double i = 0; i <= 2 * Math.PI; i += ((2 * Math.PI) / amount)) {
            int deltaX = (int) Math.floor((radius + 1) * Math.cos(i));
            int deltaZ = (int) Math.floor((radius + 1) * Math.sin(i));
            BlockVector2D point = new BlockVector2D(location.getX() + deltaX, location.getZ() + deltaZ);
            points.add(point);
        }
        return points;
    }

    public static List<BlockVector2D> createCirclePoints(CitizenGroupCenter center, int radius, int amount) {
        List<BlockVector2D> points = new ArrayList<>(amount);
        for (double i = 0; i <= 2 * Math.PI; i += ((2 * Math.PI) / amount)) {
            int deltaX = (int) Math.floor((radius + 1) * Math.cos(i));
            int deltaZ = (int) Math.floor((radius + 1) * Math.sin(i));
            BlockVector2D point = new BlockVector2D(center.getX() + deltaX, center.getZ() + deltaZ);
            points.add(point);
        }
        return points;
    }
    /**
     * Fetches the block distance between two TownCenters. This ensures that the
     * @param townA
     * @param townB
     * @return
     */
    public static int getDistanceOfGroupCenters(CitizenGroupCenter townA, CitizenGroupCenter townB) {
        int diffX = Math.abs(townA.getX() - townB.getX());
        int diffZ = Math.abs(townA.getZ() - townB.getZ());
        int c = (int) Math.sqrt(Math.pow(diffX,2)+Math.pow(diffZ,2));
        return c;
    }

    /**
     * Fetches the block distance between the current Location and a CitizenGroupCenter.
     * @param loc
     * @param center
     * @return
     */
    public static int getDistanceFromGroupCenter(Location loc, CitizenGroupCenter center) {
        if (center == null)
            return Integer.MAX_VALUE;
        int diffX = Math.abs(loc.getBlockX() - center.getX());
        int diffZ = Math.abs(loc.getBlockZ() - center.getZ());
        int c = (int) Math.sqrt(Math.pow(diffX,2)+Math.pow(diffZ,2));
        return c;
    }

    public static int countItemsInInventory(PlayerInventory inventory, ItemStack stack) {
        int totalAmount = 0;
        for (Integer i : inventory.all(stack.getType()).keySet()) {
            ItemStack thisItem = inventory.getItem(i);
            if (isSameItem(stack,thisItem)) {
                totalAmount += thisItem.getAmount();
            }
        }
        return totalAmount;
    }

    private static boolean isSameItem(ItemStack itemA, ItemStack itemB) {
        boolean same = true;
        if (!itemA.getType().equals(itemB.getType()))
            same = false;
        if (itemA.getDurability() !=itemB.getDurability())
            same = false;
        if (itemA.hasItemMeta() && itemB.hasItemMeta()) {
            if (!Bukkit.getItemFactory().equals(itemA.getItemMeta(),itemB.getItemMeta()))
                same = false;

        } else if ((itemA.hasItemMeta() && !itemB.hasItemMeta()) || (!itemA.hasItemMeta() && itemB.hasItemMeta()))
            same = false;
        return same;
    }

}
