package com.herocraftonline.townships.groups.town;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.*;
import com.herocraftonline.townships.api.config.CitizenGroupConfig;
import com.herocraftonline.townships.command.Command;
import com.herocraftonline.townships.groups.town.commands.*;
import com.herocraftonline.townships.groups.town.commands.admin.*;
import com.herocraftonline.townships.groups.town.commands.regions.*;
import com.herocraftonline.townships.groups.town.event.PlayerLeaveTownEvent;
import com.herocraftonline.townships.groups.town.event.TownClaimEvent;
import com.herocraftonline.townships.groups.town.event.TownCreateEvent;
import com.herocraftonline.townships.groups.town.event.TownDisbandEvent;
import com.herocraftonline.townships.util.Messaging;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * @author gabizou
 */

public final class TownManager extends RegionedCitizenGroupManager {

    private BukkitTask task;

    private static TownManager instance;

    final TownConfig townConfig;

    public TownManager(Townships plugin) {
        super(plugin,new File(plugin.getDataFolder(), "townsConfig.yml"));
        if (groupConfig == null || !(groupConfig instanceof TownConfig)) {
            townConfig = (TownConfig) loadGroupConfig();
        } else
        townConfig = (TownConfig) groupConfig;
        instance = this;
        Bukkit.getPluginManager().registerEvents(new TownListener(plugin), plugin);
    }

    public static TownManager getInstance() {
        return instance;
    }

    @Override
    public CitizenGroupConfig loadGroupConfig() {
        return new TownConfig(plugin, this);
    }

    @Override
    public TownConfig getConfig() {
        return townConfig;
    }

    public TownConfig getTownConfig() {
        return townConfig;
    }

    @Override
    public String getName() {
        return "town";
    }

    @Override
    public Map<String, Command> getCommands() {
        if (commands.isEmpty()) {
            registerCommands();
            return commands;
        }
        return commands;
    }


    @Override
    public Class<? extends PendingGroup> getPendingGroupClass() {
        return PendingTown.class;
    }

    @Override
    public Class<? extends CitizenGroup> getCitizenGroupClass() {
        return Town.class;
    }

    private void registerCommands() {
        addCommand(new TownAcceptCommand(this));
        addCommand(new TownBankDepositCommand(this));
        addCommand(new TownBankViewCommand(this));
        addCommand(new TownCharterCommand(this));
        /* Disabled until Herochat integration is fixed
        if (Townships.chat != null && Townships.config.channelsenabled)
            addCommand(new TownChatHerochatCommand(this));
        else
            addCommand(new TownChatCommand(this));
            */
        addCommand(new TownClaimCommand(this));
        addCommand(new TownCostCommand(this));
        addCommand(new TownCreateCommand(this));
        addCommand(new TownDemoteCommand(this));
        addCommand(new TownHelpCommand(this));
        addCommand(new TownInfoCommand(this));
        addCommand(new TownInviteCommand(this));
        addCommand(new TownInvitesCommand(this));
        addCommand(new TownKickCommand(this));
        addCommand(new TownLeaveCommand(this));
        addCommand(new TownListCommand(this));
        addCommand(new TownPromoteCommand(this));
        addCommand(new TownReClaimCommand(this));
        addCommand(new TownRelationCommand(this));
        addCommand(new TownSetCommand(this));
        addCommand(new TownSetRankCommand(this));
        addCommand(new TownSignCommand(this));
        addCommand(new TownTaxCommand(this));
        addCommand(new TownUpgradeCommand(this));
        addCommand(new TownWhoCommand(this));

        // Region commands
        addCommand(new TownRegionAddCommand(this));
        addCommand(new TownRegionCreateCommand(this));
        addCommand(new TownRegionDeleteCommand(this));
        addCommand(new TownRegionInfoCommand(this));
        addCommand(new TownRegionRedefineCommand(this));
        addCommand(new TownRegionRemoveCommand(this));

        // Admin commands
        addCommand(new TownAdminReclaimCommand(this));
        addCommand(new TownAdminAddCitizenCommand(this));
        addCommand(new TownAdminSetRankCommand(this));
        addCommand(new TownAdminForceUpgradeCommand(this));
        addCommand(new TownAdminRegionListCommand(this));
    }

    public Town createTown(PendingGroup group, GroupType type, String mayor) {
        if (groups.containsKey(group.name))
            return null;

        Town town = new Town(group.name, 0);
        town.setType(type);
        groups.put(town.getName(),town);
//        plugin.getChatManager().addTownChannel(town);
        for (String name : group.getMembers()) {
            // Make sure we're ADDING members, not SETTING members.
            if (town.addMember(name, Rank.CITIZEN)) {
                Player p = Bukkit.getPlayer(name);
                if (p != null) {
                    Messaging.send(p, Messaging.getMessage("town_formation_announcement_for_player"), town.getName());
                    Citizen c = plugin.getCitizenManager().getCitizen(p);
                    c.setTown(town);
                    c.setPendingTown(null);
                    c.setPendingTownOwner(false);
                    plugin.getStorageManager().getStorage().saveCitizen(c, true);
//                    plugin.getChatManager().joinGroupChannel(c);
                }
            }
        }
        town.setNewCitizenRank(mayor, Rank.OWNER);
        town.setDisplayName(group.name);
        town.setCreationDate(new Date().getTime());
        town.setLastTax(new Date().getTime());
        TownCreateEvent event = new TownCreateEvent(town);
        Bukkit.getPluginManager().callEvent(event);
        plugin.getStorageManager().getStorage().saveCitizenGroup(town, true);
        return town;
    }

    @Override
    public boolean claimArea(CitizenGroup group, Location loc) {
        Region townRegion = wgm.createAndSaveGroupRegion(loc, ((Regionable) group).getType(), group.getName(), ((Mayoral) group).getMayor());
        if (townRegion == null) {
            return false;
        }
        Town town = (Town) group;
        town.setClaimedWorld(loc.getWorld().getName());
        town.setCenter(new CitizenGroupCenter(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        town.setRegion(townRegion);
        TownClaimEvent event = new TownClaimEvent(town,townRegion);
        Bukkit.getPluginManager().callEvent(event);
        wgm.addRegion(group, townRegion);
        Townships.debugLog(Level.INFO, "A town has claimed a region at: " + loc.getBlockX() + ", " + loc.getBlockY() +
                ", " + loc.getBlockZ() + " in world:" + loc.getWorld());
        Messaging.sendAnnouncement(plugin, Messaging.getMessage("town_claim_announcement"), ((Mayoral) group).getMayor(), loc.getBlockX(), loc.getBlockZ(), group.getName());
        wgm.commitChanges(loc.getWorld());
        plugin.getStorageManager().getStorage().saveCitizenGroup(group, true);
        plugin.getStorageManager().getStorage().saveManagerData(this);
        return true;
    }

    @Override
    public boolean reclaimArea(CitizenGroup group, Location location) {
        ProtectedRegion wgregion = wgm.getRegionManager(location.getWorld()).getRegion(group.getName());
        if (wgregion == null)
            return false;
        Region existingRegion = new Region(wgregion, ((Mayoral) group).getMayor());
        for (String name : wgregion.getMembers().getPlayers()) {
            existingRegion.addGuest(name);
        }
        for (String name : wgregion.getOwners().getPlayers()) {
            existingRegion.addOwner(name);
        }
        Region newRegion = wgm.redefineRegion(location, existingRegion, ((Regionable) group).getType(), group.getName());
        if (newRegion == null)
            return false;
        Town town = (Town) group;
        town.setRegion(newRegion);
        town.setClaimedWorld(location.getWorld().getName());
        CitizenGroupCenter center = new CitizenGroupCenter(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        town.setCenter(center);
        wgm.addRegion(group, newRegion);
        wgm.commitChanges(location.getWorld());
        TownClaimEvent event = new TownClaimEvent(town, newRegion);
        Bukkit.getPluginManager().callEvent(event);
        Townships.debugLog(Level.INFO, "A town has claimed a region at: " + location.getBlockX() + ", " + location.getBlockY() +
                ", " + location.getBlockZ() + " in world:" + location.getWorld());
        Messaging.sendAnnouncement(plugin, Messaging.getMessage("town_claim_announcement"), ((Mayoral) group).getMayor(),
                location.getBlockX(), location.getBlockZ(), group.getName());
        plugin.getStorageManager().getStorage().saveCitizenGroup(group, true);
        plugin.getStorageManager().getStorage().saveManagerData(this);
        return true;
    }

    @Override
    public void load() {
        super.load();
        task = Bukkit.getScheduler().runTaskTimer(plugin, new TownTimer(), 1 * 60 * 20, 15 * 60 * 20);
        if (task.getTaskId() == -1) {
            Townships.log(Level.SEVERE,Messaging.getMessage("town_auto_schedule_task_fail"));
        }
    }

    @Override
    public void shutdown() {
        if (task != null)
            task.cancel();
        save();
    }

    @Override
    public void delete(CitizenGroup group) {
        CitizenGroup cg = groups.remove(group.getName().toLowerCase());
        TownDisbandEvent event = new TownDisbandEvent((Town) group);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;
        Set<String> allMembers = group.getCitizens().keySet();
        // Because HashMap.keySet() does not support addAll() operations
        for (String name : group.getNoncitizens().keySet()) {
            allMembers.add(name);
        }
        for (String name : allMembers) {
            Bukkit.getPluginManager().callEvent(new PlayerLeaveTownEvent(name, (Town) group));
        }
        for (Citizen citizen : cg.getOnlineCitizens()) {
            citizen.setTown(null);
            Messaging.send(citizen.getPlayer(), "$1 has been disbanded.", cg.getName());
        }
        Messaging.sendAnnouncement(plugin,Messaging.getMessage("town_delete_town"),cg.getName());
        if (cg != null) {
            plugin.getStorageManager().getStorage().deleteCitizenGroup(this, cg);
        }
//        plugin.getChatManager().removeTownChannel((Town) group);
    }

    @Override
    public void removeCitizenFromPendingGroup(String name, Citizen citizen, boolean message) {
        name = name.toLowerCase();
        if (pendingGroups.containsKey(name)) {
            Player player = citizen.getPlayer();
            pendingGroups.get(name).removeMember(player.getName());
        }
        citizen.setPendingTown(null);
        citizen.setPendingTownOwner(false);
        save();
        plugin.getStorageManager().getStorage().saveCitizen(citizen, true);
    }

    @Override
    public void addCitizenToPendingGroup(String name, String citizen) {
        name = name.toLowerCase();
        if (pendingGroups.containsKey(name)) {
            pendingGroups.get(name).addMember(citizen);
            save();
        }
    }

    public Town get(String townName) {
        return townName != null ? (Town) groups.get(townName.toLowerCase()) : null;
    }

    @Override
    public void registerRegionManager() {
        if (wgm == null)
            wgm = new TownRegionManager(this);
        plugin.getWorldGuardManager().registerRegionManager(this,wgm);
    }

    /**
     * TownTimer is the automatic system that makes a series of checks for Towns. The following are checked:
     * - Town Taxes, if the town has not paid enough taxes, the town is removed.
     * - Town Citizen Pruning, if the town has inactive citizens, inactive citizens are removed
     * - Town Citizen Requirement Checks, if the town has too few citizens for the town size, it is downgraded or removed
     */
    public class TownTimer implements Runnable {
        @Override
        public void run() {
            if (Townships.isDoneLoading()) {
                long time = System.currentTimeMillis();
                for (Iterator<Map.Entry<String, CitizenGroup>> it = groups.entrySet().iterator(); it.hasNext(); ) {

                    Town town = (Town) it.next().getValue();
                    /* Disabled until rewritten
                    switch (taxTown(town)) {
                        case MISSED_TAX:
                            town.sendAnnouncement(Messaging.getMessage("town_missed_tax"));

                            break;
                        case EARLY_NO_MISSED_TAXES:

                            break;
                        case PAID_ALL_TAXES:
                            break;
                        case  PAID_NO_MISSED_TAXES:
                            break;
                        case  PAID_MISSING_TAXES:
                            break;
                        case  PAID_TAX:

                            break;
                        default:
                            break;
                    }
                    */
                    pruneTown(town, time);
                    // Make sure that the town still has citizens, if not, remove entirely
                    if (town.getCitizens().size() == 0) {
                        wgm.removeGroupRegions(town);
                        processDelete(town);
                        it.remove();
                    }
                    // Process town size requirements
                    if (town.getCitizens().size() < townConfig.getGroupTypeMinResidents(town.getType())) {
                        if (TownUtil.timeCheck(town)) {
                            town.sendAnnouncement(Messaging.getMessage("town_auto_downgrade_town"),town.getName());
                            GroupType minType = townConfig.defaultTownType;
                            GroupType minRegionType = townConfig.minregionedTownType;
                            if (canDownSize(town, minType)) {
                                town.setType(town.getType().downgrade());
                                if (town.getRegion() != null) {
                                    if (canKeepRegion(town.getType(), minRegionType)) {
                                        wgm.downsizeGroupRegion(town, town.getType());
                                        wgm.commitChanges(Bukkit.getWorld(town.getClaimedWorld()));
                                    } else {
                                        wgm.removeGroupRegions(town);
                                    }

                                }

                            } else {
                                String log = Messaging.parameterizeMessage(Messaging.getMessage("town_auto_remove_town_announcement"), town.getName());
                                Townships.log(Level.INFO,log);
                                town.sendAnnouncement(Messaging.getMessage("town_auto_remove_town_announcement"), town.getName());
                                processDelete(town);
                                it.remove();
                                Messaging.sendAnnouncement(plugin,log);
                            }
                        } else {
                            long expireTime = townConfig.townInterval + town.getLastCitizenWarning();
                            Date expire = new Date(expireTime);
                            String format = new SimpleDateFormat("EEE, d MMM yyyy 'around' HH:mm").format(expire);
                            town.sendAnnouncement(Messaging.getMessage("town_auto_pending_downgrade"),format);
                        }

                    } else if (town.getLastCitizenWarning() != 0) { // Reset the warning
                        town.setLastCitizenWarning(0);
                    }
                }
                for (Iterator<Map.Entry<String, PendingGroup>> it = pendingGroups.entrySet().iterator(); it.hasNext();) {
                    PendingTown pendingTown = (PendingTown) it.next().getValue();
                    if (pendingTown.getCreationDate() + townConfig.townInterval < time) {
                        it.remove();
                        Messaging.sendAnnouncement(plugin, Messaging.getMessage("town_auto_charter_removal"), pendingTown.name);
                    }
                }
                plugin.getStorageManager().getStorage().saveManagerData(TownManager.this);
                plugin.getStorageManager().getStorage().saveCitizenGroups(plugin.getGroupManager("town"));
            }

        }

        private TownManager.TaxResult taxTown(Town town) {
            long time = System.currentTimeMillis();
            Townships.debugLog(Level.INFO, "The Town: " + town.getName() + " with a last tax time of: " +
                    town.getLastTax() + " and " + town.getMissedPayments() + " missed payments is going through the Automator.");
            Townships.debugLog(Level.INFO, "The Townships configured tax interval is: " + townConfig.taxInterval);
            if (time >= town.getLastTax() + townConfig.taxInterval) {
                Townships.log(Level.INFO, "[Townships] Attempting to tax " + town.getName() + " now.");
                town.setLastTax(time);
                if (!town.collectTax()) {
                    town.setMissedPayments(town.getMissedPayments() + 1);
                    return TaxResult.MISSED_TAX;
                } else {
                    int missed = town.getMissedPayments();
                    if (missed == 0)
                        return TaxResult.PAID_TAX;
                    else {
                        for (int i = missed; i > 0; i--) {
                            town.setMissedPayments(i);
                            if (!town.collectTax())
                                return TaxResult.PAID_MISSING_TAXES;
                        }
                        town.setMissedPayments(0);
                        town.setLastTaxWarning(0);
                        return TaxResult.PAID_ALL_TAXES;
                    }
                }
            } else { // This attempts to reconcile all missed taxes even if the town's tax date isn't due yet.
                Townships.log(Level.INFO, "[Townships] Checking Missed Taxes for " + town.getName());
                if (town.getMissedPayments() > 0) {
                    int missed = town.getMissedPayments();
                    for (int i = missed; i >= 0; i--) {
                        if (!town.collectTax()) {
                            town.setMissedPayments(i);
                            Townships.debugLog(Level.INFO, "[Taxes] The Town: " + town.getName() + " still has missing taxes it can't pay for.");
                            return TaxResult.EARLY_MISSING_TAXES;
                        }
                        missed = i;
                    }
                    if (missed == 0) {
                        town.setMissedPayments(0);
                        town.setLastTaxWarning(0);
                        Townships.debugLog(Level.INFO, "[Taxes] The Town: " + town.getName() + "has paid all missing taxes early.");
                        return TaxResult.EARLY_PAID_ALL_TAXES;
                    } else {
                        town.setMissedPayments(missed);
                        Townships.debugLog(Level.INFO, "[Taxes] The Town: " + town.getName() + " has paid some taxes but is" +
                                "still missing " + missed);
                        return TaxResult.EARLY_PAID_MISSING_TAXES;
                    }
                }
                Townships.debugLog(Level.INFO, "[Taxes] The Town: " + town.getName() + " has no missing taxes and paid this week's tax.");
                return TaxResult.EARLY_NO_MISSED_TAXES;
            }
        }

        private Set<String> pruneTown(Town town, long time) {
            Set<String> prunedMembers = new HashSet<>();
            Set<String> names = new HashSet<>(town.getCitizens().keySet());
            for (String name : names) {
                long login = town.getCitizenLastLogin(name);

                if (time > townConfig.citizenInterval + login) {
                    if (town.getCitizenRank(name) == Rank.SUCCESSOR) {
                        town.sendAnnouncement(Messaging.getMessage("town_auto_request_successor"));
                        town.removeMember(name);
                    } else if (town.getCitizenRank(name) == Rank.OWNER) {
                        town.sendAnnouncement(Messaging.getMessage("town_auto_kick_mayor"), name);
                        town.removeMember(name);
                    } else {
                        town.removeMember(name);
                        town.sendAnnouncement(Messaging.getMessage("town_auto_kick_citizen"), name);
                    }
                    Townships.log(Level.INFO,Messaging.getMessage("town_auto_kick_citizen").replace("$1", name));
                    prunedMembers.add(name);
                }
            }
            return prunedMembers;
        }

        private boolean canDownSize(Town town, GroupType min) {
            return town.getType().ordinal() <= min.ordinal() && town.getType().downgrade() != null;
        }

        private boolean canKeepRegion(GroupType type, GroupType regions) {
            return townConfig.isGroupTypeRegionsEnabled(type) && type.ordinal() >= regions.ordinal();
        }

        private void processDelete(CitizenGroup group) {
            Set<String> allMembers = group.getCitizens().keySet();
            // Because HashMap.keySet() does not support addAll() operations
            for (String name : group.getNoncitizens().keySet()) {
                allMembers.add(name);
            }
            for (String name : allMembers) {
                Bukkit.getPluginManager().callEvent(new PlayerLeaveTownEvent(name, (Town) group));
            }
            for (Citizen citizen : group.getOnlineCitizens()) {
                citizen.setTown(null);
                Messaging.send(citizen.getPlayer(), "$1 has been disbanded.", group.getName());
            }
            Messaging.sendAnnouncement(plugin,Messaging.getMessage("town_delete_town"),group.getName());
            if (group != null) {
                plugin.getStorageManager().getStorage().deleteCitizenGroup(TownManager.this, group);
            }
        }

    }
    private enum TaxResult {
        EARLY_MISSING_TAXES,
        EARLY_NO_MISSED_TAXES,
        EARLY_PAID_MISSING_TAXES,
        EARLY_PAID_ALL_TAXES,
        PAID_ALL_TAXES,
        PAID_NO_MISSED_TAXES,
        PAID_MISSING_TAXES,
        MISSED_TAX,
        PAID_TAX

    }

}
