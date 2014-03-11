package com.herocraftonline.townships.api;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.groups.guild.PendingGuild;
import com.herocraftonline.townships.groups.town.PendingTown;
import com.herocraftonline.townships.groups.town.Town;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author gabizou
 */

public class CitizenManager {

    transient Townships plugin;
    transient Map<String, Citizen> citizens = new HashMap<>();

    public CitizenManager(Townships plugin) {
        this.plugin = plugin;
        //Load in the initial citizens for all online players
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            getCitizen(p);
        }
    }

    public Citizen getCitizen(Player player) {
        Citizen citizen = null;

        if (player != null) {
            String name = player.getName();
            if (!citizens.containsKey(name)) {

                citizen = plugin.getStorageManager().getStorage().loadCitizen(player);
                if (citizen == null) {
                    citizen = new Citizen(player);
                    citizen.setLastLogin(new Date());
                    plugin.getStorageManager().getStorage().saveCitizen(citizen, true);
                }
                verifyCitizen(citizen);
                citizens.put(name, citizen);
            }
            citizen = citizens.get(name);
        }
        return citizen;
    }

    public Citizen getCitizen(String name) {
        if (name == null)
            return null;

        if (Bukkit.getPlayer(name) != null)
            return getCitizen(Bukkit.getPlayer(name));
        else {
            Citizen citizen;
            if (!citizens.containsKey(name)) {
                citizen = plugin.getStorageManager().getStorage().loadOfflineCitizen(name);
                if (citizen == null) { // Should happen if and only if the citizen has never logged in
                    citizen = new Citizen(name);
                    plugin.getStorageManager().getStorage().saveCitizen(citizen, true);
                }
                return citizen;
            } else
                return citizens.get(name);
        }
    }

    /**
     * @return collection of all citizens currently loaded
     */
    public Collection<Citizen> getCitizens() {
        return Collections.unmodifiableCollection(citizens.values());
    }

    /**
     * Removes a player from the citizen mapping - should only be called if the player is no longer online
     * @param player
     */
    public void removeCitizen(Player player) {
        if (plugin.getStorageManager().getStorage().getCitizensNotToSave().contains(player.getName()))
            plugin.getStorageManager().getStorage().getCitizensNotToSave().remove(player.getName());
        else
            plugin.getStorageManager().getStorage().saveCitizen(citizens.remove(player.getName()), true);
        citizens.remove(player.getName());
    }

    public void removeCitizen(Citizen citizen) {
        removeCitizen(citizen.getPlayer());
    }

    private void verifyCitizen(Citizen citizen) {
        //Verify that the Citizen should actually be in the town, this checks if the player might have been kicked previously by a manager, or for inactivity.
        if (citizen.getTown() != null) {
            Town town = citizen.getTown();
            // Check if the citizen's set town has the citizen still listed as a citizen
            if (!town.hasCitizen(citizen)) {
                town = null;
                for (CitizenGroup group : plugin.getTownManager().getGroups()) {
                    Town town1 = (Town) group;
                    if (town1.hasCitizen(citizen)) {
                        town = town1;
                        break;
                    }
                }
                if (town != null) {
                    String rankName = town.getRankName(town.getCitizenRank(citizen));
                    Messaging.send(citizen.getPlayer(), Messaging.getMessage("citizen_group_switch"),
                            rankName, town.getName());
                    citizen.setTown(town);
                } else
                    citizen.setTown(null);
            }
        }

        // TODO Do the same check for Guilds

        PendingTown pendingTownName = citizen.getPendingTown();
        PendingGuild pendingGuildName = citizen.getPendingGuild();

        //Check to see if the pending town is no longer Pending - such as if it was created successfully or abandoned.
        if (pendingTownName != null && !plugin.getTownManager().isPending(pendingTownName.name)) {
            citizen.setPendingTown(null);
            citizen.setPendingTownOwner(false);
            //This Adds the player to the proper town when they login after a pending town has been created
            if (citizen.getTown() == null && plugin.getTownManager().exists(pendingTownName.name)) {
                Town town = plugin.getTownManager().get(pendingTownName.name);
                if (town.hasCitizen(citizen))
                    citizen.setTown(town);
                else
                    Messaging.send(citizen.getPlayer(),Messaging.getMessage("pending_group_kick"), pendingTownName);
            } else {
                Messaging.send(citizen.getPlayer(),Messaging.getMessage("peding_group_expire"), pendingTownName);
            }
        }
    }
}
