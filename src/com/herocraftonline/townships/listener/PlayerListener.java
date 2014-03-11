package com.herocraftonline.townships.listener;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.api.CitizenGroup;
import com.herocraftonline.townships.api.events.CitizenLogOffEvent;
import com.herocraftonline.townships.api.events.CitizenLoginEvent;
import com.herocraftonline.townships.command.Command;
import com.herocraftonline.townships.util.JSONMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Date;
import java.util.logging.Level;

/**
 * Author: gabizou
 */
public class PlayerListener implements Listener {

    private Townships plugin;

    public PlayerListener(Townships plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        //Load in the citizen for this player
        final Citizen citizen = plugin.getCitizenManager().getCitizen(event.getPlayer());
        CitizenGroup town = citizen.getTown();
        if (town != null) {
            town.sendAnnouncement(event.getPlayer().getDisplayName() + " has logged in.");
            if (!town.citizenLogin(citizen)) {
                Townships.debugLog(Level.SEVERE, "Could not set " + event.getPlayer().getName() +
                        " as an online citizen!");
            }
        }
        citizen.setLastLogin(new Date());
        CitizenLoginEvent loginEvent = new CitizenLoginEvent(event.getPlayer().getName(), citizen);
        Bukkit.getPluginManager().callEvent(loginEvent);
//        plugin.getChatManager().loginGroupChannel(citizen);
        JSONMessage message = new JSONMessage(event.getPlayer().getName());
        message.color(ChatColor.YELLOW)
                .then(", you have successfully logged in!")
                    .color(ChatColor.AQUA)
                .then(" Visit our website for the latest news!")
                    .color(ChatColor.DARK_AQUA)
                    .style(ChatColor.ITALIC, ChatColor.BOLD, ChatColor.UNDERLINE)
                    .link("http://hc.to")
                    .tooltip("Herocraft Website");
        message.send(event.getPlayer());
        Townships.debugLog(Level.INFO, citizen.getName() + "'s UUID from Bukkit is: " + event.getPlayer().getUniqueId());

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        //Unload the citizen
        Citizen citizen = plugin.getCitizenManager().getCitizen(event.getPlayer());
        if (citizen.getTown() != null) {
            citizen.getTown().citizenLogoff(citizen);
        }
        // Update Last Login date
        citizen.setLastLogin(new Date());
        CitizenLogOffEvent logOffEvent = new CitizenLogOffEvent(event.getPlayer().getName(), citizen);
        Bukkit.getPluginManager().callEvent(logOffEvent);
        plugin.getCitizenManager().removeCitizen(event.getPlayer());
        for (final Command command : plugin.getCommandHandler().getCommands()) {
            if (command.isInteractive()) {
                command.cancelInteraction(event.getPlayer());
            }
        }
    }
}
