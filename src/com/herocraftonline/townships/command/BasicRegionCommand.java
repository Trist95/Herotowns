package com.herocraftonline.townships.command;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.RegionedCitizenGroupManager;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;

/**
 * Author: gabizou
 */
public abstract class BasicRegionCommand extends BasicCommand {

    protected WorldGuardPlugin wgp;
    protected RegionedCitizenGroupManager manager;

    public BasicRegionCommand(RegionedCitizenGroupManager manager, String name) {
        super(manager, name);
        this.manager = manager;
        wgp = plugin.getWorldGuardManager().getWorldGuard();
    }

    @Override
    public void setIdentifiers(String... args) {
        for (String arg : args) {
            arg = "region " + arg;
        }
        super.setIdentifiers(args);
    }

    /**
     * Get a WorldEdit selection for a player, or emit an exception if there is none
     * available.
     *
     * @param player the player
     * @return the selection
     * @throws com.sk89q.minecraft.util.commands.CommandException thrown on an error
     */
    protected static Selection getSelection(Player player) {
        WorldEditPlugin worldEdit;
        WorldGuardPlugin worldGuard = Townships.getInstance().getWorldGuardManager().getWorldGuard();

        try {
            worldEdit = worldGuard.getWorldEdit();
            Selection selection = worldEdit.getSelection(player);
            return selection;
        } catch (CommandException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create a {@link com.sk89q.worldguard.protection.regions.ProtectedRegion} from the player's selection.
     *
     * @param player the player
     * @param id the ID of the new region
     * @return a new region
     * @throws CommandException thrown on an error
     */
    protected static ProtectedRegion createRegionFromSelection(Player player, String id) {

        Selection selection = getSelection(player);

        // Detect the type of region from WorldEdit
        if (selection instanceof Polygonal2DSelection) {
            Polygonal2DSelection polySel = (Polygonal2DSelection) selection;
            int minY = polySel.getNativeMinimumPoint().getBlockY();
            int maxY = polySel.getNativeMaximumPoint().getBlockY();
            return new ProtectedPolygonalRegion(id, polySel.getNativePoints(), minY, maxY);
        } else if (selection instanceof CuboidSelection) {
            BlockVector min = selection.getNativeMinimumPoint().toBlockVector();
            BlockVector max = selection.getNativeMaximumPoint().toBlockVector();
            return new ProtectedCuboidRegion(id, min, max);
        } else {
            return null;
        }
    }

    /**
     * Validate a region ID.
     *
     * @param id the id
     * @param allowGlobal whether __global__ is allowed
     * @return the id given
     * @throws CommandException thrown on an error
     */
    protected static String validateRegionId(String id, boolean allowGlobal) {
        if (!ProtectedRegion.isValidId(id)) {
            return null;
        }

        if (!allowGlobal && id.equalsIgnoreCase("__global__")) { // Sorry, no global
            return null;
        }

        return id;
    }

}
