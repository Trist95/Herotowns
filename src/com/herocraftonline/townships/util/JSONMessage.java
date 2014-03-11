package com.herocraftonline.townships.util;

import net.milkbowl.vault.item.Items;
import net.minecraft.server.v1_7_R1.ChatSerializer;
import net.minecraft.server.v1_7_R1.PacketPlayOutChat;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.JSONException;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Author: gabizou
 */
public class JSONMessage {

    private final List<MessagePart> messageParts;

    public JSONMessage(final String firstPartText) {
        messageParts = new ArrayList<MessagePart>();
        messageParts.add(new MessagePart(firstPartText));
    }

    public JSONMessage color(final ChatColor color) {
        if (!color.isColor()) {
            throw new IllegalArgumentException(color.name() + " is not a color");
        }
        latest().color = color;
        return this;
    }

    public JSONMessage style(final ChatColor... styles) {
        for (final ChatColor style : styles) {
            if (!style.isFormat()) {
                throw new IllegalArgumentException(style.name() + " is not a style");
            }
        }
        latest().styles = styles;
        return this;
    }

    public JSONMessage file(final String path) {
        onClick("open_file", path);
        return this;
    }

    public JSONMessage link(final String url) {
        onClick("open_url", url);
        return this;
    }

    public JSONMessage suggest(final String command) {
        onClick("suggest_command", command);
        return this;
    }

    public JSONMessage command(final String command) {
        onClick("run_command", command);
        return this;
    }

    public JSONMessage achievementTooltip(final String name) {
        onHover("show_achievement", "achievement." + name);
        return this;
    }

    public JSONMessage itemTooltip(final String itemJSON) {
        onHover("show_item", itemJSON);
        return this;
    }

    public JSONMessage bankItemTooltip(BankItem item) {
        String itemName;
        if (item.getItem().getItemMeta().hasDisplayName())
            itemName = item.getItem().getItemMeta().getDisplayName();
        else
            itemName = Items.itemByStack(item.getItem()).getName();
        ItemMeta meta = item.getItem().getItemMeta();
        String loreString = "{id:"+item.getItem().getTypeId()+",tag:";
        loreString = loreString + "{display:{Name:" + Messaging.colorize(itemName);
        if (meta.hasLore()) {
            loreString = loreString + ",Lore:[\"";
            List<String> loreData = meta.getLore();
            Iterator<String> lore = loreData.iterator();
            while (lore.hasNext()) {
                String loreline = lore.next();
                loreString = loreString + loreline.replace("\"","\\\"");
                if (lore.hasNext()) {
                    loreString = loreString + "\",\"";
                } else {
                    loreString = loreString + "\"]";
                }
            }
        }
        loreString = loreString + "}}}";
        loreString = loreString.replace("'","");
        return itemTooltip(loreString);
    }

    public JSONMessage tooltip(final String text) {
        onHover("show_text", text);
        return this;
    }

    public JSONMessage then(final Object obj) {
        messageParts.add(new MessagePart(obj.toString()));
        return this;
    }

    public String toJSONString() {
        final JSONStringer json = new JSONStringer();
        try {
            if (messageParts.size() == 1) {
                latest().writeJson(json);
            } else {
                json.object().key("text").value("").key("extra").array();
                for (final MessagePart part : messageParts) {
                    part.writeJson(json);
                }
                json.endArray().endObject();
            }
        } catch (final JSONException e) {
            throw new RuntimeException("invalid message");
        }
        return json.toString();
    }

    public void send(Player player){
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ChatSerializer.a(toJSONString())));
    }

    private MessagePart latest() {
        return messageParts.get(messageParts.size() - 1);
    }

    private void onClick(final String name, final String data) {
        final MessagePart latest = latest();
        latest.clickActionName = name;
        latest.clickActionData = data;
    }

    private void onHover(final String name, final String data) {
        final MessagePart latest = latest();
        latest.hoverActionName = name;
        latest.hoverActionData = data;
    }

}
