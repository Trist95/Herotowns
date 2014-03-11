package com.herocraftonline.townships.listener;

import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.events.RankChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.kitteh.tag.TagAPI;

/**
 * Author: gabizou
 */
public class TagAPIListener  implements Listener {

    public Townships plugin;

    public TagAPIListener(Townships plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRankChange(RankChangeEvent e) {
        Player player = Bukkit.getPlayer(e.getName());
        if (player != null) {
            TagAPI.refreshPlayer(player);
        }
    }
}
