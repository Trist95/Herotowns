package com.herocraftonline.townships.util;

import com.herocraftonline.townships.MessageNotFoundException;
import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.CitizenGroup;
import com.herocraftonline.townships.api.Rank;
import net.milkbowl.vault.item.Items;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.logging.Level;


public final class Messaging {

    private static ResourceBundle messages;

    public static void broadcast(Townships plugin, String msg, Object... params) {
        plugin.getServer().broadcastMessage(parameterizeMessage(msg, params));
    }

    public static String parameterizeMessage(String msg, Object... params) {
        msg = ChatColor.GRAY + msg;
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                msg = msg.replace("$" + (i + 1), ChatColor.WHITE + params[i].toString() + ChatColor.GRAY);
            }
        }
        return msg;
    }

    public static final int CHARS_PER_LINE = 52;

    public static boolean hasOnlyLetters(String s) {
        return (s.matches("\\w+"));
    }

    public static void send(CommandSender sender, String msg, Object... params) {
        sender.sendMessage(parameterizeMessage(msg, params));
    }

    /**
     * Send a message to a list of players
     *
     * @param names
     * @param msg
     * @param plugin
     * @param params
     */
    public static void send(Collection<String> names, String msg, Plugin plugin, Object... params) {
        for (String name : names) {
            Player player = plugin.getServer().getPlayer(name);
            if (player == null)
                continue;

            send(player, msg, params);
        }
    }

    /**
     * Cause bukkit left this out of it's configuration class
     * @param raw
     * @param def
     * @return
     */

    @SuppressWarnings("rawtypes")
    public static List<String> getStringList(List raw, List<String> def) {
        if (raw == null) {
            return def != null ? def : new ArrayList<String>();
        }

        List<String> list = new ArrayList<String>();

        for (Object o : raw) {
            if (o == null) {
                continue;
            }

            list.add(o.toString());
        }
        return list;
    }

    public static String[] formatCollection(Collection<String> c) {
        if (c.isEmpty())
            return new String[] { "" };
        else {
            List<String> lines = new ArrayList<String>();
            StringBuilder sb = new StringBuilder();
            String s;
            for (Iterator<String> iter = c.iterator(); iter.hasNext();) {
                s = iter.next();
                if (sb.length() + s.length() > CHARS_PER_LINE && sb.length() != 0) {
                    lines.add(sb.toString());
                    sb = new StringBuilder();
                }
                sb.append(s);
                if (iter.hasNext()) {
                    if (sb.length() + 2 > CHARS_PER_LINE && sb.length() != 0) {
                        lines.add(sb.toString());
                        sb = new StringBuilder();
                    } else {
                        sb.append(", ");
                    }
                } else {
                    lines.add(sb.toString());
                }
            }
            return lines.toArray(new String[lines.size()]);
        }
    }

    public static String formatCitizenGroup(Collection<CitizenGroup> c) {
        if (c.isEmpty())
            return "";
        else {
            String msg = "";
            for (CitizenGroup cg : c) {
                msg += "  " + cg.getName();
            }
            return msg;
        }
    }

    public static String formatTownMembers(Map<String,Rank> members) {
        return null;
    }

    public static void sendAnnouncement(Townships plugin, String msg, Object...args) {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            send(p, msg, args);
        }
    }

    public static int countItemsInInventory(PlayerInventory inventory, ItemStack item) {
        int totalAmount = 0;
        boolean isDurable = Items.itemByStack(item).isDurable();

        for (Integer i : inventory.all(item.getType()).keySet()) {
            ItemStack thisStack = inventory.getItem(i);
            if (!isDurable && thisStack.getDurability() != item.getDurability()) {
                continue;
            }
            totalAmount += thisStack.getAmount();
        }
        return totalAmount;
    }

    /**
     * A safe method to retrieve a message string based on Locale of the plugin. It will automatically
     * colorize the configured messages and if the message is not loaded in the language pack, it will
     * return an empty string and send a warning to log.
     * @param key
     * @return
     */
    public static String getMessage(String key) {
        try {
            return getMessage1(key);
        } catch (MessageNotFoundException e) {
            Townships.log(Level.SEVERE,"Messages.properties is missing: " + key);
            return "";
        }
    }

    /**
     * Retrieves the localized message for Townships
     * @param key - Message key to be obtained
     * @return - Message that is localized to the configured Language
     * @throws MessageNotFoundException
     */
    private static String getMessage1(String key) throws MessageNotFoundException {
        String msg = messages.getString(key);
        if (msg == null) {
            throw new MessageNotFoundException();
        } else {
            msg = colorize(msg);
            return msg;
        }
    }

    /**
     * Colorizes the message prior to being sent out. Eliminates the use of
     * ChatColor and characters that could break if characterformat is changed.
     */
    public static String colorize(String message) {
        String colorized = message.replaceAll("(?i)&([a-fklmno0-9])", "\u00A7$1");
        return colorized;
    }

    /**
     * Defines the current Language for the plugin, if the localization is available.
     * @param locale - Locale to be used
     * @throws ClassNotFoundException
     */
    public static void setLocale(Locale locale) throws ClassNotFoundException {
        messages = ResourceBundle.getBundle("resources.Messages.Messages", locale);
        if (messages == null) {
            throw new ClassNotFoundException("resources.Messages");
        }
    }
}
