package com.herocraftonline.townships.groups.town;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.api.GroupType;
import com.herocraftonline.townships.groups.town.event.TownCreateEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;

/**
 * Author: gabizou
 */
public class TownListener implements Listener {

    private Townships plugin;

    public TownListener(Townships plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority= EventPriority.LOWEST)
    public void onTownCreateEvent(TownCreateEvent event) {
        Town town = event.getTown();
        GroupType type = town.getType();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onNameTagEvent(AsyncPlayerReceiveNameTagEvent event) {
        // Get the Player "seeing" the tag
        Player player = event.getPlayer();
        Citizen cit = plugin.getCitizenManager().getCitizen(player);
        // Get the player being seen
        Player seenPlayer = event.getNamedPlayer();
        Citizen seenCitizen = plugin.getCitizenManager().getCitizen(seenPlayer);
        if (cit.getTown() == null) {
            return;
        }

        // Check if both citizen's towns are the same
        if (cit.getTown().equals(seenCitizen.getTown())) {
            event.setTag(ChatColor.DARK_GREEN + ChatColor.BOLD.toString() + seenPlayer.getName());
            return;
        }

        // If not, check for the seeing citizen's town relations map for relations towards
        // the seenPlayer.
        if (seenCitizen.getTown() != null && cit.getTown().getGroupRelations().containsKey(seenCitizen.getTown().getName()))
            switch (cit.getTown().getGroupRelations().get(seenCitizen.getTown().getName())) {
                case WAR :
                    event.setTag(ChatColor.RED + ChatColor.BOLD.toString() + seenPlayer.getName());
                    break;
                case ALLY :
                    event.setTag(ChatColor.BLUE + ChatColor.BOLD.toString() + seenPlayer.getName());
                    break;
                case NEUTRAL:
                default :
                    event.setTag(seenPlayer.getName());
            }
        // Ensure that the SeenPlayer's direct relation with the SeeingCitizen's Town
        // is respected
        if (cit.getTown().hasMember(seenPlayer.getName()))
            switch (cit.getTown().getCitizenRank(seenPlayer.getName())) {
                case ENEMY:
                    event.setTag(ChatColor.RED + seenPlayer.getName());
                    break;
                case GUEST:
                    event.setTag(ChatColor.BLUE + ChatColor.BOLD.toString() + seenPlayer.getName());
                    break;
                case NONE:
                default :
                    event.setTag(seenPlayer.getName());
            }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity defender = event.getEntity();
        Entity attacker = event.getDamager();
        if (attacker != null && attacker instanceof Player && defender instanceof Player) {
            Player defend = (Player) defender;
            Player attack = (Player) attacker;
            Citizen attackingCitizen = plugin.getCitizenManager().getCitizen(attack);
            Citizen defendingCitizen = plugin.getCitizenManager().getCitizen(defend);
            if (defendingCitizen.hasTown() && attackingCitizen.hasTown()) {
                Town attackTown = attackingCitizen.getTown();
                Town defendTown = defendingCitizen.getTown();
                if (defendTown.equals(attackTown) && !defendTown.isPvp())
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByMob(EntityDamageByEntityEvent event) {
        Entity defender = event.getEntity();
        Entity attacker = event.getDamager();
        if (attacker instanceof Tameable && defender instanceof Player) {
            if (!(((Tameable) attacker).getOwner() instanceof Player)) {
                return;
            }
            Player defendPlayer = (Player) defender;
            Player beastMaster = (Player) ((Tameable) attacker).getOwner();
            Citizen attackingCitizen = plugin.getCitizenManager().getCitizen(beastMaster);
            Citizen defendingCitizen = plugin.getCitizenManager().getCitizen(defendPlayer);
            if (defendingCitizen.hasTown() && attackingCitizen.hasTown()) {
                Town attackTown = attackingCitizen.getTown();
                Town defendTown = defendingCitizen.getTown();
                if (defendTown.equals(attackTown) && !defendTown.isPvp())
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByProjectile(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile))
            return;
        Entity defender = event.getEntity();
        ProjectileSource source = ((Projectile) event.getDamager()).getShooter();
        if (!(source instanceof LivingEntity))
            return;
        Entity attacker = (LivingEntity) source;
        if (defender instanceof Player) {
            Player player = (Player) defender;
            // Check Player
            // if (event.getDamager() instanceof EnderPearl || event.getDamager() instanceof Snowball) return;
            if (attacker instanceof Player) {
                Player attack = (Player) attacker;
                if (event.getDamager() instanceof EnderPearl && attacker == player) return;
                Citizen attackingCitizen = plugin.getCitizenManager().getCitizen(attack);
                Citizen defendingCitizen = plugin.getCitizenManager().getCitizen(player);
                if (defendingCitizen.hasTown() && attackingCitizen.hasTown()) {
                    Town attackTown = attackingCitizen.getTown();
                    Town defendTown = defendingCitizen.getTown();
                    if (defendTown.equals(attackTown) && !defendTown.isPvp())
                        event.setCancelled(true);
                }
            }
        }

    }
}
