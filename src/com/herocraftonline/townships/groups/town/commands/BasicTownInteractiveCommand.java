package com.herocraftonline.townships.groups.town.commands;

import com.herocraftonline.townships.command.BasicRegionInteractiveCommand;
import com.herocraftonline.townships.groups.town.TownManager;

/**
 * Author: gabizou
 */
public abstract class BasicTownInteractiveCommand extends BasicRegionInteractiveCommand {

    protected final TownManager manager;

    public BasicTownInteractiveCommand(TownManager manager, String name) {
        super(manager, name);
        this.manager = manager;
    }

}
