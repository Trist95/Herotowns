package com.herocraftonline.townships.groups.town;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.GroupType;
import com.herocraftonline.townships.util.BankItem;
import net.milkbowl.vault.item.Items;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Date;
import java.util.Map;

/**
 * Author: gabizou
 */
public final class TownUtil {

    /**
     * Checks if a given town has all upgrade requirements as per the configuration.
     * @param town
     * @return
     */
    public static boolean townHasUpgradeRequirements(Town town) {
        boolean hasEnough = false;
        boolean hasAll = true;
        for (BankItem is : TownManager.getInstance().townConfig.getGroupTypeResourceCost(town.getType().nextUpgrade())) {
            for (Map.Entry<String, BankItem> entry : town.getBankContents().entrySet()) {
                if (entry.getValue().getItem().isSimilar(is.getItem())) {
                    if (entry.getValue().getAmount() >= is.getAmount()) {
                        hasEnough = true;
                    } else {
                        hasAll = false;
                        break;
                    }
                }
            }
            if (!hasEnough) {
                hasAll = false;
                break;
            }
        }
        return hasAll;
    }

    /**
     * Removes required items from TownBank
     * @param town
     * @param type
     */
    public static void removeTownCosts(Town town, GroupType type) {
        for (BankItem item : TownManager.getInstance().townConfig.getGroupTypeResourceCost(type)) {
            int remaining = 0;
            for (Map.Entry<String,BankItem> bankItem : town.getBankContents().entrySet()) {
                if(item.equals(bankItem.getValue())) {
                    remaining = bankItem.getValue().getAmount();
                    break;
                }
            }
            remaining -= item.getAmount();
            if (remaining > 0) {
                town.getBankContents().remove(item);
                BankItem newItem = new BankItem(item.getItem(),remaining);
                ItemMeta meta = newItem.getItem().getItemMeta();
                String name;
                if (meta.hasDisplayName()) {
                    name = meta.getDisplayName();
                } else {
                    name = Items.itemByStack(newItem.getItem()).getName();
                }
                town.getBankContents().put(name,newItem);
            }
        }
        Townships.getInstance().getStorageManager().getStorage().saveCitizenGroup(town, true);
    }

    public static boolean timeCheck(Town town) {
        long warning = town.getLastCitizenWarning();
        if (warning == 0) {
            town.setLastCitizenWarning(new Date().getTime());
            warning = town.getLastCitizenWarning();
        }
        long time = System.currentTimeMillis();
        return (time - warning > TownManager.getInstance().townConfig.townInterval);
    }

}
