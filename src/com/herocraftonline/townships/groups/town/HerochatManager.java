package com.herocraftonline.townships.groups.town;

import com.dthielke.herochat.*;
import com.herocraftonline.townships.Townships;
import com.herocraftonline.townships.api.Citizen;
import com.herocraftonline.townships.api.CitizenGroup;
import com.herocraftonline.townships.api.Rank;
import com.herocraftonline.townships.api.Upgradable;
import com.herocraftonline.townships.util.Messaging;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: gabizou
 */
public final class HerochatManager {

    private final transient Townships plugin;
    private final transient ChannelManager channelManager;
    private final transient ChatterManager chatterManager;

    private final transient TownManager manager;

    private Map<CitizenGroup,Channel> townChannels = new HashMap<>();

    public HerochatManager(Townships plugin, TownManager manager) {
        this.plugin = plugin;
        channelManager = Herochat.getChannelManager();
        chatterManager = Herochat.getChatterManager();
        this.manager = manager;
        loadTownChannels();
    }

    private void loadTownChannels() {
        for (CitizenGroup group : plugin.getTownManager().getGroups()) {
            Town town = (Town) group;
            if (manager.townConfig.getGroupTypeChannelEnabled(town.getType())) {
                addTownChannel(town);
            }
        }
    }

    /**
     * Creates a {@link TownChannel} for the given {@link Town} and stores it. The {@link TownChannel} created
     * will attempt to add all online Citizens of the {@link Town}.
     * @param town
     */
    public void addTownChannel(CitizenGroup town) {
        if (!townChannels.containsKey(town)) {
            TownChannel channel = new TownChannel(town);
            Herochat.getChannelManager().addChannel(channel);
            for (Citizen online : town.getOnlineCitizens()) {
                Chatter chatter = Herochat.getChatterManager().getChatter(online.getPlayer());
                channel.addMember(chatter,false,false);
                if (town.getCitizenRank(online).ordinal() >= Rank.MANAGER.ordinal()) {
                    channel.setModerator(chatter.getName(),true);
                } else {
                    channel.setModerator(chatter.getName(),false);
                }
            }
            townChannels.put(town,channel);
        }

    }

    /**
     * Removes a Town's channel from the system
     */
    public void removeTownChannel(Town town) {
        Channel channel = townChannels.get(town);
        if (channel != null) {
            channelManager.removeChannel(channel);
            townChannels.remove(town);
        }
    }

    /**
     * Requests a channel from Herochat for a given town.
     *
     * WARNING: DOES NOT CHECK IF CHANNEL EXISTS, CHECK IF NULL AFTER CALLING METHOD
     *
     * @param group The town requesting the channel
     * @return The town's channel if not null
     */
    public Channel getGroupChannel(CitizenGroup group) {
        Channel channel;
        if (group != null) {
            if (group instanceof Upgradable && manager.townConfig.getGroupTypeChannelEnabled(((Upgradable) group).getType())) {

                if (townChannels.containsKey(group)) {
                    channel = townChannels.get(group);
                    return channel;
                } else {
                    addTownChannel(group);
                    channel = townChannels.get(group);
                    return channel;
                }
            }
        }
        return null;
    }

    public boolean hasGroupChannel(CitizenGroup group) {
        return townChannels.containsKey(group);
    }

    /**
     * This attempts to log the Citizen into their active Town's TownChannel. It is transient so Herochat
     * does not keep this channel in Storage. Ensures that the player is logging in to the proper Channel.
     *
     * @param citizen
     * @return
     */
    public boolean loginGroupChannel(Citizen citizen) {
        Channel channel = getGroupChannel(citizen.getTown());
        if (channel == null)
            return false;
        Channel chatChannel = Herochat.getChannelManager().getChannel(citizen.getTown().getName());
        if (chatChannel instanceof TownChannel) {
            Chatter chatter = chatterManager.getChatter(citizen.getPlayer());
            TownChannel townChannel = (TownChannel) chatChannel;
            Channel previous = chatter.getActiveChannel();
            Channel before = chatter.getLastActiveChannel();
            chatter.addChannel(townChannel,false,false);
            townChannel.addMember(chatter, false, false);
            chatter.setActiveChannel(before,false,false);
            chatter.setActiveChannel(previous,false,false);
            return true;
        }
        return false;
    }

    /**
     * Tells Herochat to set the player to join their respective town channel.
     * This method assumes that
     * @param citizen the citizen in question wishing to join the channel.
     * @return whether the player was able to join their TownChannel or not.
     */
    public boolean joinGroupChannel(Citizen citizen) {
        Channel townChannel = getGroupChannel(citizen.getTown());
        // Ensure that the channel actually exists
        if (townChannel == null)
            return false;
        Chatter chatter = chatterManager.getChatter(citizen.getPlayer());
        Chatter.Result result = chatter.canJoin(townChannel,townChannel.getPassword());
        switch (result) {
            case INVALID :
                Messaging.send(citizen.getPlayer(), Messaging.getMessage("town_chat_channel_invalid"));
                return true;
            case NO_PERMISSION :
                Messaging.send(citizen.getPlayer(), Messaging.getMessage("town_chat_channel_no_perms"));
                return true;
            case BANNED :
                Messaging.send(citizen.getPlayer(), Messaging.getMessage("town_chat_channel_banned"));
                return true;
            default :
        }
        townChannel.addMember(chatter, true, true);
        chatter.addChannel(townChannel,false,false);
        Messaging.send(citizen.getPlayer(), Messaging.getMessage("town_chat_channel_join"),
                townChannel.getColor(),townChannel.getName());
        return true;
    }

    /**
     * Handles quick messaging a citizen's town channel. The channel does not change, nor does
     * the player have to know the name of the town channel. This bit of logic is similar to how Herochat
     * handles quick messaging.
     *
     * @param citizen The citizen wishing to send a message to their town channel
     * @param args The message
     * @return If the town channel exists, and the message was sent.
     */
    public boolean sendMessageToTownChannel(Citizen citizen, String[] args) {
        Channel townChannel = channelManager.getChannel(citizen.getTown().getName());
        if (townChannel == null)
            return false;
        Chatter chatter = chatterManager.getChatter(citizen.getPlayer());

        StringBuilder msg = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            msg.append(args[i]).append(" ");
        }

        Channel active = chatter.getActiveChannel();

        chatter.setActiveChannel(townChannel, false, false);
        Herochat.getMessageHandler().handle(citizen.getPlayer(), msg.toString().trim(), "<%1$s> %2$s");
        chatter.setActiveChannel(active, false, false);
        return true;
    }
}
