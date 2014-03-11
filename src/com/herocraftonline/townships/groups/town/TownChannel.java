package com.herocraftonline.townships.groups.town;

import com.dthielke.herochat.Chatter;
import com.dthielke.herochat.Herochat;
import com.dthielke.herochat.MessageNotFoundException;
import com.dthielke.herochat.StandardChannel;
import com.herocraftonline.townships.api.CitizenGroup;
import com.herocraftonline.townships.api.Rank;
import com.herocraftonline.townships.util.Messaging;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: gabizou
 */
public class TownChannel extends StandardChannel {
    private static final Pattern msgPattern = Pattern.compile("(.*)<(.*)%1\\$s(.*)> %2\\$s");
    private static final String format = "{color}[{townnick}] {townRank}{color}{sender}{color}: {msg}";
    private static final String announcement = "{color}[{townnick}] {townRank}{color}{sender}{color} {msg}";

    private final CitizenGroup town;

    protected TownChannel(CitizenGroup town, Chatter... chatter) {
        super(Herochat.getChannelManager().getStorage(),town.getName(),town.getPrefix(),Herochat.getChannelManager());
        for (Chatter chat : chatter) {
            addMember(chat, false, false);
        }
        setNick(town.getPrefix());
        this.town = town;
        setColor(town.getColor());
        setFormat(format);
        setCrossWorld(true);
    }

    @Override
    public boolean addMember(Chatter chatter, boolean announce, boolean flagUpdate) {
        return super.addMember(chatter, false, false);
    }

    @Override
    public void addWorld(String world) {}

    @Override
    public void announce(String message) {
        //handle colors first for announcements.
        String colorized = message.replaceAll("(?i)&([a-fklmno0-9])", "\u00A7$1");
        message = applyFormat(announcement, "").replace("%2$s", colorized);
        for (Chatter member : getMembers()) {
            member.getPlayer().sendMessage(message);
        }
        Herochat.logChat(ChatColor.stripColor(message));
    }

    @Override
    public String applyFormat(String format, String originalFormat) {
        format = format.replace("{msg}", "%2$s");
        format = format.replace("{color}",getColor().toString());
        format = format.replace("{townnick}", getNick());
        format = format.replace("{townName}", getName());
        // add existing prefixes and suffixes (data that isn't a part of the default minecraft format string)
        Matcher matcher = msgPattern.matcher(originalFormat);
        if (matcher.matches() && matcher.groupCount() == 3) {
            format = format.replace("{sender}", matcher.group(1) + matcher.group(2) + "%1$s" + matcher.group(3));
        } else {
            format = format.replace("{sender}", "%1$s");
        }

        format = format.replaceAll("(?i)&([a-fklmnor0-9])", "\u00A7$1");
        return format;
    }

    @Override
    public String applyFormat(String format, String originalFormat, Player sender) {
        format = applyFormat(format, originalFormat);
        Chat chat = Herochat.getChatService();
        if (chat != null) {
            try {
                String prefix = chat.getPlayerPrefix(sender);
                if (prefix == null || prefix.equals("")) {
                    prefix = chat.getPlayerPrefix((String) null, sender.getName());
                }
                String suffix = chat.getPlayerSuffix(sender);
                if (suffix == null || suffix.equals("")) {
                    suffix = chat.getPlayerSuffix((String) null, sender.getName());
                }
                String group = chat.getPrimaryGroup(sender);
                String groupPrefix = group == null ? "" : chat.getGroupPrefix(sender.getWorld(), group);
                if ( group != null && (groupPrefix == null || groupPrefix.equals(""))) {
                    groupPrefix = chat.getGroupPrefix((String) null, group);
                }
                String groupSuffix = group == null ? "" : chat.getGroupSuffix(sender.getWorld(), group);
                if ( group != null && (groupSuffix == null || groupSuffix.equals(""))) {
                    groupSuffix = chat.getGroupSuffix((String) null, group);
                }
                format = format.replace("{prefix}", prefix == null ? "" : prefix.replace("%", "%%"));
                format = format.replace("{suffix}", suffix == null ? "" : suffix.replace("%", "%%"));
                format = format.replace("{group}", group == null ? "" : group.replace("%", "%%"));
                format = format.replace("{groupprefix}", groupPrefix == null ? "" : groupPrefix.replace("%", "%%"));
                format = format.replace("{groupsuffix}", groupSuffix == null ? "" : groupSuffix.replace("%", "%%"));
            } catch (UnsupportedOperationException ignored) {
            }
        } else {
            format = format.replace("{prefix}", "");
            format = format.replace("{suffix}", "");
            format = format.replace("{group}", "");
            format = format.replace("{groupprefix}", "");
            format = format.replace("{groupsuffix}", "");
        }
        Rank citizenRank = town.getCitizenRank(sender.getName());
        String rankName = town.getRankName(citizenRank);
        switch (citizenRank) {
            case OWNER:
                format = format.replace("{townRank}","&7[&6" + rankName + "&7]");
                break;
            case SUCCESSOR:
                format = format.replace("{townRank}","&4[&9" + rankName + "&4]");
                break;
            case MANAGER:
                format = format.replace("{townRank}","&7[&8" + rankName + "&7]");
                break;
            default:
                format = format.replace("{townRank}","");
        }
        format = format.replaceAll("(?i)&([a-fklmno0-9])", "\u00A7$1");
        return format;
    }

    @Override
    public boolean banMember(Chatter chatter, boolean announce) {
        if (!getMembers().contains(chatter))
            return false;
        if (town.hasCitizen(chatter.getName()))
            return false;
        removeMember(chatter, false, true);
        setBanned(chatter.getName(), true);
        if (announce) {
            String message = Messaging.parameterizeMessage(
                    Messaging.getMessage("town_chat_channel_announce_banned"),chatter.getName(),getColor().toString());
            announce(message);
        }
        return true;
    }

    @Override
    public int getDistance() {
        return 0;
    }

    @Override
    public Set<String> getWorlds() {
        return new HashSet<>();
    }

    @Override
    public boolean isCrossWorld() {
        return true;
    }

    @Override
    public boolean hasWorld(World world) {
        return true;
    }

    @Override
    public boolean hasWorld(String world) {
        return true;
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public boolean isShortcutAllowed() {
        return false;
    }

    @Override
    public boolean isTransient() {
        return true;
    }

    @Override
    public boolean kickMember(Chatter chatter, boolean announce) {
        if (!getMembers().contains(chatter)) {
            return false;
        }
        if (town.hasCitizen(chatter.getName()))
            return false;

        removeMember(chatter, false, true);

        if (announce) {
            String message = Messaging.parameterizeMessage(
                    Messaging.getMessage("town_chat_channel_announce_kicked"),chatter.getName(),getColor().toString());
            announce(message);
        }

        return true;
    }

    @Override
    public boolean removeMember(Chatter chatter, boolean announce, boolean flagUpdate) {
        if (super.removeMember(chatter,announce,flagUpdate))
        if (!getMembers().contains(chatter)) {
            return false;
        }

        if (chatter.hasChannel(this)) {
            chatter.removeChannel(this, announce, flagUpdate);
        }

        if (announce && isVerbose()) {
            try {
                announce(Herochat.getMessage("channel_leave").replace("$1", chatter.getPlayer().getDisplayName()));
            } catch (MessageNotFoundException e) {
                Herochat.severe("Messages.properties is missing: channel_leave");
            }
        }

        return true;
    }

    @Override
    public void removeWorld(String world) {}

    @Override
    public void setShortcutAllowed(boolean shortcutAllowed) {}

    @Override
    public void setWorlds(Set<String> worlds) {}
}
