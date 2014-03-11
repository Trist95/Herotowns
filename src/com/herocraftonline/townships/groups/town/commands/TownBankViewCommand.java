package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.groups.town.Town;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.util.BankItem;
import com.herocraftonline.townships.util.JSONMessage;
import com.herocraftonline.townships.util.Messaging;
import net.milkbowl.vault.item.Items;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Author: gabizou
 */
public class TownBankViewCommand extends BasicTownCommand {

    public TownBankViewCommand(TownManager manager) {
        super(manager, "TownBankView");
        setDescription("Lists the contents of the town bank.");
        setUsage("/town bank view [page]");
        setArgumentRange(0, 1);
        setIdentifiers("bank view", "b v");
        setPermission("townships.town.bank.view");
    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {
        if (!(sender instanceof Player)) {
            Messaging.send(sender, getMessage("town_not_a_player"));
            return false;
        }

        Player player = (Player) sender;
        Citizen citizen = getCitizen(player);
        if (!citizen.hasTown()) {
            Messaging.send(sender, getMessage("town_not_part_of_town"));
            return false;
        }
        Town town = citizen.getTown();
        List<BankItem> items = new ArrayList<>(town.getBankContents().entrySet().size());
        for (Map.Entry<String,BankItem> entry : town.getBankContents().entrySet()) {
            items.add(entry.getValue());
        }
        if (items.isEmpty()) {
            Messaging.send(sender, getMessage("town_bank_econ"), town.getBankMoney(),
                    Townships.econ.currencyNamePlural());
            Messaging.send(sender, getMessage("town_bank_empty"));
            return true;
        }
        int maxPage = items.size() / 8;
        if (items.size() % 8 != 0) {
            maxPage++;
        }
        int page = 0;
        if (args.length == 1) {
            try {
                page = Integer.parseInt(args[0]) - 1;
                if (page > maxPage || page < 0) {
                    page = 0;
                }
            } catch (NumberFormatException e) {
                // Squelched! just assume page 0
            }
        }
        int start = 8 * page;
        int end = 8 * (page + 1);
        if (end > items.size()) {
            end = items.size();
        }

        Messaging.send(sender, getMessage("town_bank_item_list"),
                town.getBankMoney(), Townships.econ.currencyNamePlural(), page + 1, maxPage);
        for (; start < end; start++) {
            BankItem is = items.get(start);
            String itemName;

            if (is.getItem().getItemMeta().hasDisplayName()) {
                itemName = is.getItem().getItemMeta().getDisplayName();
            } else {
                itemName = Items.itemByStack(is.getItem()).getName();
            }
            int amount = town.getBankContents().get(itemName).getAmount();
            ItemMeta meta = is.getItem().getItemMeta();
            String name;
            if (meta.hasLore()) {
                JSONMessage message = new JSONMessage("");
                message.color(ChatColor.WHITE);
                message.then(amount)
                        .color(ChatColor.GRAY)
                        .then(" - ");
                message.then(itemName)
                        .bankItemTooltip(is);
                message.send((Player) sender);
                Townships.debugLog(Level.INFO,message.toJSONString());

            } else {
                name = Items.itemByStack(is.getItem()).getName();
                if (amount > 1) {
                    name = name + ChatColor.RESET.toString() + "(s)";
                }
                Messaging.send(sender, getMessage("town_bank_item"), amount, name);
            }
        }
        return true;
    }
}
