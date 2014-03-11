package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.command.BasicRegionCommand;
import com.herocraftonline.townships.groups.town.TownManager;

/**
 * Author: gabizou
 */
public abstract class BasicTownCommand extends BasicRegionCommand {

    protected final TownManager manager;

    protected BasicTownCommand(TownManager manager, String name) {
        super(manager, name);
        this.manager = manager;
    }

}
