package com.herocraftonline.townships.util;

import com.herocraftonline.townships.api.Region;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Create a region printout, as used in /region info to show information about
 * a region.
 */
public class RegionPrintoutBuilder {

    private final Region region;
    private final StringBuilder builder = new StringBuilder();

    /**
     * Create a new instance with a region to report on.
     *
     * @param region the region
     */
    public RegionPrintoutBuilder(Region region) {
        this.region = region;
    }

    /**
     * Add a new line.
     */
    private void newLine() {
        builder.append("\n");
    }

    /**
     * Add region name, type, and priority.
     */
    public void appendBasics() {
        builder.append(ChatColor.BLUE);
        builder.append("Region: ");
        builder.append(ChatColor.YELLOW);
        builder.append(region.getWorldGuardRegion().getId());

        builder.append(ChatColor.GRAY);
        builder.append(" (type=");
        builder.append(region.getWorldGuardRegion().getTypeName());

        builder.append(ChatColor.GRAY);
        builder.append(", priority=");
        builder.append(region.getWorldGuardRegion().getPriority());
        builder.append(")");

        newLine();
    }

    /**
     * Add information about flags.
     */
    public void appendFlags() {
        builder.append(ChatColor.BLUE);
        builder.append("Flags: ");

        appendFlagsList(true);

        newLine();
    }

    /**
     * Append just the list of flags (without "Flags:"), including colors.
     *
     * @param useColors true to use colors
     */
    public void appendFlagsList(boolean useColors) {
        boolean hasFlags = false;

        for (Flag<?> flag : DefaultFlag.getFlags()) {
            Object val = region.getWorldGuardRegion().getFlag(flag), group = null;

            // No value
            if (val == null) {
                continue;
            }

            if (hasFlags) {
                builder.append(", ");
            } else {
                if (useColors) {
                    builder.append(ChatColor.YELLOW);
                }
            }

            RegionGroupFlag groupFlag = flag.getRegionGroupFlag();
            if (groupFlag != null) {
                group = region.getWorldGuardRegion().getFlag(groupFlag);
            }

            if(group == null) {
                builder.append(flag.getName()).append(": ")
                        .append(String.valueOf(val));
            } else {
                builder.append(flag.getName()).append(" -g ")
                        .append(String.valueOf(group)).append(": ")
                        .append(String.valueOf(val));
            }

            hasFlags = true;
        }

        if (!hasFlags) {
            if (useColors) {
                builder.append(ChatColor.RED);
            }
            builder.append("(none)");
        }
    }

    /**
     * Add information about parents.
     */
    public void appendParents() {
        appendParentTree(true);
    }

    /**
     * Add information about parents.
     *
     * @param useColors true to use colors
     */
    public void appendParentTree(boolean useColors) {
        if (region.getWorldGuardRegion().getParent() == null) {
            return;
        }

        List<ProtectedRegion> inheritance = new ArrayList<ProtectedRegion>();

        ProtectedRegion r = region.getWorldGuardRegion();
        inheritance.add(r);
        while (r.getParent() != null) {
            r = r.getParent();
            inheritance.add(r);
        }

        ListIterator<ProtectedRegion> it = inheritance.listIterator(
                inheritance.size());

        int indent = 0;
        while (it.hasPrevious()) {
            ProtectedRegion cur = it.previous();
            if (useColors) {
                builder.append(ChatColor.GREEN);
            }

            // Put symbol for child
            if (indent != 0) {
                for (int i = 0; i < indent; i++) {
                    builder.append("  ");
                }
                builder.append("\u2517");
            }

            // Put name
            builder.append(cur.getId());

            // Put (parent)
            if (!cur.equals(region)) {
                if (useColors) {
                    builder.append(ChatColor.GRAY);
                }
                builder.append(" (parent, priority=" + cur.getPriority() + ")");
            }

            indent++;
            newLine();
        }
    }

    /**
     * Add information about members.
     */
    public void appendDomain() {
        builder.append(ChatColor.BLUE);
        builder.append("Owners: ");
        DefaultDomain owners = new DefaultDomain();
        for (String guest : region.getOwners()) {
            owners.addPlayer(guest);
        }
        if (owners.size() != 0) {
            builder.append(ChatColor.YELLOW);
            builder.append(owners.toUserFriendlyString());
        } else {
            builder.append(ChatColor.RED);
            builder.append("(no owners)");
        }

        newLine();

        builder.append(ChatColor.BLUE);
        builder.append("Managers: ");
        DefaultDomain managers = new DefaultDomain();
        for (String manager : region.getManagers()) {
            managers.addPlayer(manager);
        }
        if (managers.size() != 0) {
            builder.append(ChatColor.YELLOW);
            builder.append(managers.toUserFriendlyString());
        } else {
            builder.append(ChatColor.RED);
            builder.append("(no managers)");
        }


        newLine();

        builder.append(ChatColor.BLUE);
        builder.append("Guests: ");
        DefaultDomain members = new DefaultDomain();
        for (String guest : region.getGuests()) {
            members.addPlayer(guest);
        }
        if (members.size() != 0) {
            builder.append(ChatColor.YELLOW);
            builder.append(members.toUserFriendlyString());
        } else {
            builder.append(ChatColor.RED);
            builder.append("(no guests)");
        }

        newLine();
    }

    /**
     * Add information about coordinates.
     */
    public void appendBounds() {
        BlockVector min = region.getWorldGuardRegion().getMinimumPoint();
        BlockVector max = region.getWorldGuardRegion().getMaximumPoint();
        builder.append(ChatColor.BLUE);
        builder.append("Bounds:");
        builder.append(ChatColor.YELLOW);
        builder.append(" (" + min.getBlockX() + "," + min.getBlockY() + "," + min.getBlockZ() + ")");
        builder.append(" -> (" + max.getBlockX() + "," + max.getBlockY() + "," + max.getBlockZ() + ")");

        newLine();
    }

    /**
     * Append all the default fields used for /rg info.
     */
    public void appendRegionInfo() {
        builder.append(ChatColor.GRAY);
        builder.append("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550");
        builder.append(" Region Info ");
        builder.append("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550");
        newLine();
        appendBasics();
        appendFlags();
        appendParents();
        appendDomain();
        appendBounds();
    }

    /**
     * Send the report to a {@link CommandSender}.
     *
     * @param sender the recepient
     */
    public void send(CommandSender sender) {
        sender.sendMessage(toString());
    }

    public StringBuilder append(boolean b) {
        return builder.append(b);
    }

    public StringBuilder append(char c) {
        return builder.append(c);
    }

    public StringBuilder append(char[] str, int offset, int len) {
        return builder.append(str, offset, len);
    }

    public StringBuilder append(char[] str) {
        return builder.append(str);
    }

    public StringBuilder append(CharSequence s, int start, int end) {
        return builder.append(s, start, end);
    }

    public StringBuilder append(CharSequence s) {
        return builder.append(s);
    }

    public StringBuilder append(double d) {
        return builder.append(d);
    }

    public StringBuilder append(float f) {
        return builder.append(f);
    }

    public StringBuilder append(int i) {
        return builder.append(i);
    }

    public StringBuilder append(long lng) {
        return builder.append(lng);
    }

    public StringBuilder append(Object obj) {
        return builder.append(obj);
    }

    public StringBuilder append(String str) {
        return builder.append(str);
    }

    public StringBuilder append(StringBuffer sb) {
        return builder.append(sb);
    }

    public StringBuilder appendCodePoint(int codePoint) {
        return builder.appendCodePoint(codePoint);
    }

    @Override
    public String toString() {
        return builder.toString().trim();
    }

}
