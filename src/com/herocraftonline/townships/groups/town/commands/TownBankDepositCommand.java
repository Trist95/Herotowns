package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.command.BasicInteractiveCommandState;
import com.herocraftonline.townships.groups.town.Town;
import com.herocraftonline.townships.groups.town.TownManager;
import com.herocraftonline.townships.util.Messaging;
import com.herocraftonline.townships.util.Util;
import net.milkbowl.vault.item.ItemInfo;
import net.milkbowl.vault.item.Items;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Author: gabizou
 */
public class TownBankDepositCommand extends BasicTownInteractiveCommand {

    private Map<String, ItemStack> pending = new HashMap<>();
    private Map<String, Integer> pendingAmount = new HashMap<>();

    public TownBankDepositCommand(TownManager manager) {
        super(manager, "TownBankDeposit");
        setDescription("Deposit materials into the town bank.");
        setUsage("/town bank §9deposit <item> <amount>");
        setStates(new StateA(), new StateB());
        setPermission("townships.town.bank.deposit");
        setIdentifiers("bank deposit");
    }

    @Override
    public String getCancelIdentifier() {
        return "cancel";
    }

    @Override
    public void onCommandCancelled(CommandSender sender) {
        if (!(sender instanceof Player)) {
            return;
        }
        pending.remove(((Player) sender).getPlayer().getName());
    }

    class StateA extends BasicInteractiveCommandState {

        public StateA() {
            super("bank deposit", "b d");
            setArgumentRange(0, 2);
        }

        @Override
        public boolean execute(CommandSender sender, String identifier, String[] args) {
            if (!(sender instanceof Player)) {
                Messaging.send(sender, getMessage("town_not_a_player"));
                cancelInteraction(sender);
                return false;
            }
            Player player = (Player) sender;
            Citizen citizen = getCitizen(player);
            if (!citizen.hasTown()) {
                Messaging.send(sender, getMessage("town_not_part_of_town"));
                cancelInteraction(sender);
                return false;
            }
            ItemStack deposit = null;
            ItemInfo info;
            String iName = null;
            int amount = 0;
            if (args.length == 0) {
                deposit = player.getItemInHand();
                if (deposit != null && deposit.getType() != Material.AIR) {
                    info = Items.itemByStack(deposit);
                    iName = info.getName();
                    amount = deposit.getAmount();
                }
            } else if (args.length == 2) {
                try {
                    amount = Integer.parseInt(args[1]);
                    if (amount < 1) {
                        Messaging.send(sender, getMessage("town_deposit_nothing"));
                        cancelInteraction(sender);
                        return false;
                    }
                } catch (NumberFormatException e) {
                    // do nothing deposit will be null anyway
                    Messaging.send(sender, getMessage("town_deposit_nothing"));
                    cancelInteraction(sender);
                    return false;
                }
                switch (args[0].toLowerCase()) {
                    case "money" :
                        if (!Townships.econ.has(sender.getName(), amount)) {
                            Messaging.send(sender,getMessage("town_insufficient_econ"),
                                    amount, Townships.econ.currencyNamePlural());
                            cancelInteraction(sender);
                            return false;
                        }
                        iName = Townships.econ.currencyNameSingular();
                        pendingAmount.put(sender.getName(), amount);
                        break;
                    case "this" :
                        deposit = player.getItemInHand().clone();
                        if (deposit != null && deposit.getType() != Material.AIR) {
                            info = Items.itemByStack(deposit);
                            iName = info.getName();
                            deposit.setAmount(amount);
                        }
                        break;
                    default:
                        info = Items.itemByString(args[0]);
                        if (info == null) {
                            Messaging.send(player, getMessage("town_deposit_invalid"), args[0]);
                            cancelInteraction(sender);
                            return false;
                        } else {
                            deposit = info.toStack();
                            deposit.setAmount(amount);
                            iName = info.getName();
                        }
                }

            } else {
                Messaging.send(sender, getMessage("town_deposit_missing_amount"), args[0]);
                cancelInteraction(sender);
                return false;
            }

            if (iName == null) {
                cancelInteraction(sender);
                return false;
            }

            if (!iName.equals("money")) {
                pending.put(player.getName(), deposit);
            }
            Messaging.send(sender, getMessage("town_deposit_warning1"), amount, iName);
            Messaging.send(sender, getMessage("town_deposit_warning2"));
            Messaging.send(player, getMessage("town_command_request_confirm"));
            return true;
        }
    }

    class StateB extends BasicInteractiveCommandState {

        public StateB() {
            super("confirm");
            setArgumentRange(0,0);
        }

        @Override
        public boolean execute(CommandSender sender, String identifier, String[] args) {
            if (!(sender instanceof Player)) {
                return false;
            }
            ItemStack stack = pending.remove(sender.getName());

            Player player = (Player) sender;
            Town t = getCitizen(player).getTown();
            //Player must have left the town while pending
            if (t == null) {
                Messaging.send(sender, getMessage("town_no_longer_in_town"));
                pendingAmount.remove(player.getName());
                return true;
            }
            int amount;
            String iName;
            if (stack == null) {
                amount = pendingAmount.remove(sender.getName());
                if (!Townships.econ.has(sender.getName(), amount)) {
                    Messaging.send(sender, getMessage("town_insufficient_econ"),
                            amount,Townships.econ.currencyNamePlural());
                    return true;
                } else {
                    Townships.econ.withdrawPlayer(sender.getName(), amount);
                    iName = Townships.econ.currencyNamePlural();
                    t.deposit(amount);
                }
            } else {
                amount = stack.getAmount();
                iName = Items.itemByStack(stack).name;
                int count = Util.countItemsInInventory(player.getInventory(), stack);
                if ( count < amount) {
                    amount = count;
                    stack.setAmount(amount);
                    Messaging.send(player, getMessage("town_deposit_reduced"), amount);
                }
                Map<Integer, ItemStack> leftover = player.getInventory().removeItem(stack);
                if (!leftover.isEmpty()) {
                    Townships.log(Level.WARNING, "There was an issue removing all " + amount +
                            " items from " + player.getName() + "'s inventory.");
                }
                t.depositItem(stack);
                Townships.log(Level.INFO, Messaging.parameterizeMessage(getMessage("town_deposit_complete"),
                        player.getName(),stack.getAmount(),stack.getType().toString(),t.getName()));
            }
            t.sendAnnouncement(Messaging.parameterizeMessage(getMessage("town_deposit_complete"),
                    player.getName(),amount,iName,t.getName()));
            plugin.getStorageManager().getStorage().saveCitizenGroup(t,true);
            Townships.log(Level.INFO, player.getName() + " has deposited " + amount + " " +
                    iName + " into " + t.getName());
            return true;
        }
    }
}
