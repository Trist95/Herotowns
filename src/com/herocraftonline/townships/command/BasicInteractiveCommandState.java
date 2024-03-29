package com.herocraftonline.townships.command;

public abstract class BasicInteractiveCommandState implements InteractiveCommandState {

    private final String[] identifiers;
    private int minArguments = 0;
    private int maxArguments = 0;

    public BasicInteractiveCommandState(String...identifiers) {
        this.identifiers = identifiers;
    }

    @Override
    public int getMaxArguments() {
        return maxArguments;
    }

    @Override
    public int getMinArguments() {
        return minArguments;
    }

    @Override
    public boolean isIdentifier(String input) {
        for (final String ident : identifiers) {
            if (input.equalsIgnoreCase(ident)) {
                return true;
            }
        }
        return false;
    }

    /*
     * @Override
     * public String getIdentifier() {
     * return identifier;
     * }
     */

    public void setArgumentRange(int min, int max) {
        this.minArguments = min;
        this.maxArguments = max;
    }

}
