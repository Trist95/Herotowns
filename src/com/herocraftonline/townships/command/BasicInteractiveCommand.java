package com.herocraftonline.townships.command;

import com.herocraftonline.townships.api.CitizenGroupManager;
import com.herocraftonline.townships.util.Messaging;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public abstract class BasicInteractiveCommand extends BasicCommand implements InteractiveCommand {

    private InteractiveCommandState[] states = new InteractiveCommandState[0];
    private final Map<CommandSender, Integer> userStates = new HashMap<CommandSender, Integer>();

    public BasicInteractiveCommand(CitizenGroupManager manager, String name) {
        super(manager, name);
    }

    @Override
    public final void cancelInteraction(CommandSender executor) {
        userStates.remove(executor);
        onCommandCancelled(executor);
    }

    @Override
    public final boolean execute(CommandSender executor, String identifier, String[] args) {
        if (states.length == 0) {
            throw new IllegalArgumentException("An interactive command must have at least one state.");
        }

        int stateIndex = 0;
        if (userStates.containsKey(executor)) {
            stateIndex = userStates.get(executor);
        }

        final InteractiveCommandState state = states[stateIndex];

        if (stateIndex > 0) {
            if (this.getCancelIdentifier().equalsIgnoreCase(identifier)) {
                Messaging.send(executor, "Exiting command.");
                userStates.remove(executor);
                onCommandCancelled(executor);
                return true;
            } 
        }

        if ((args.length < state.getMinArguments()) || (args.length > state.getMaxArguments()) || !state.execute(executor, identifier, args)) {
            if ((stateIndex > 0) && userStates.containsKey(executor)) {
                Messaging.send(executor, "Invalid input - try again or type $1 to exit.", "/" + this.getCancelIdentifier());
            }
        } else {
            stateIndex++;
            if (states.length > stateIndex) {
                userStates.put(executor, stateIndex++);
            } else {
                userStates.remove(executor);
            }
        }

        return true;
    }

    @Override
    public final boolean isIdentifier(CommandSender executor, String input) {
        int stateIndex = 0;
        if (userStates.containsKey(executor)) {
            stateIndex = userStates.get(executor);
        }

        if (stateIndex > 0) {
            if (this.getCancelIdentifier().equalsIgnoreCase(input)) {
                return true;
            }
        }

        final InteractiveCommandState state = states[stateIndex];
        return state.isIdentifier(input);
    }

    @Override
    public final boolean isInProgress(CommandSender executor) {
        return userStates.containsKey(executor);
    }

    @Override
    public final boolean isInteractive() {
        return true;
    }

    @Override
    public final void setArgumentRange(int min, int max) {}

    public final void setStates(InteractiveCommandState...states) {
        if (states.length == 0) {
            throw new IllegalArgumentException("An interactive command must have at least one state.");
        }

        this.states = states;
        super.setArgumentRange(states[0].getMinArguments(), states[0].getMaxArguments());
    }

}
