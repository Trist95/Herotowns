package com.herocraftonline.townships.command.commands;

import com.herocraftonline.townships.command.BasicCommand;
import org.bukkit.command.CommandSender;

/**
 * Author: gabizou
 */
public class TownshipsGeneralCommand extends BasicCommand {


    public TownshipsGeneralCommand() {
        super(null, "TownshipsGeneral");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
        return false;
    }
}
