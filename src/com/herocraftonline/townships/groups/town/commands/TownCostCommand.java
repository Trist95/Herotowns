package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.api.GroupType;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.util.BankItem;
import com.herocraftonline.townships.util.JSONMessage;
import com.herocraftonline.townships.util.Messaging;
import net.milkbowl.vault.item.Items;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Set;
import java.util.logging.Level;

/**
 * Author: gabizou
 */
public class TownCostCommand extends BasicTownCommand {

    public TownCostCommand(TownManager manager) {
        super(manager, "TownCost");
        setArgumentRange(0, 2);
        setUsage("/town cost <towntype> [page#]");
        setIdentifiers("cost");
        setPermission("townships.town.cost");
    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {
        GroupType type = GroupType.SMALL;
        int page = 0;
        if (args.length > 0) {
            try {
                type = GroupType.valueOf(args[0].toUpperCase());
                if (args.length > 1)
                    page = Integer.parseInt(args[1]);
            } catch (IllegalArgumentException e) {
                Messaging.send(sender, getMessage("town_cost_invalid_type"));
                return false;
            }
        } else if (sender instanceof Player) {
            Player player = (Player) sender;
            Citizen citizen = getCitizen(player);
            if (citizen.hasTown() && citizen.getTown() != null) {
                type = citizen.getTown().getType().nextUpgrade();
            }
        }
        Set<BankItem> costs = manager.getTownConfig().getGroupTypeResourceCost(type);
        int maxPage = costs.size() / 8;
        if (costs.size() % 8 != 0) {
            maxPage++;
        }

        if (args.length == 2) {
            try {
                page = Integer.parseInt(args[1]) - 1;
                if (page > maxPage || page < 0) {
                    page = 0;
                }
            } catch (NumberFormatException e) {
                // Squelched! just assume page 0
            }
        }
        int start = 8 * page;
        int end = 8 * (page + 1);
        if (end > costs.size()) {
            end = costs.size();
        }
        Messaging.send(sender, getMessage("town_cost_list"), type.name(), page + 1, maxPage);
        Messaging.send(sender, getMessage("town_cost_econ"),
                Townships.econ.format(manager.getTownConfig().getGroupTypeCost(type)));
        // TODO Improve this with ChatBlocks
        for (BankItem item : costs) {
            ItemMeta meta = item.getItem().getItemMeta();
            String name;
            String itemName;
            int amount = item.getAmount();

            if (item.getItem().getItemMeta().hasDisplayName()) {
                itemName = item.getItem().getItemMeta().getDisplayName();
            } else {
                itemName = Items.itemByStack(item.getItem()).getName();
            }
            if (meta.hasDisplayName() || meta.hasLore()) {
                JSONMessage message = new JSONMessage("" + amount + " - ");

                message.then(Messaging.colorize(itemName).replace("'",""))
                        .bankItemTooltip(item);
                message.send((Player) sender);
                Townships.debugLog(Level.INFO, message.toJSONString());

            } else {
                name = Items.itemByStack(item.getItem()).getName();
                if (amount > 1) {
                    name = name + ChatColor.RESET.toString() + "(s)";
                }
                Messaging.send(sender, getMessage("town_bank_item"), amount, name);
            }
        }
        return false;
    }
}
