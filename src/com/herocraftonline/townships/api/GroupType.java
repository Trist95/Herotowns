package com.herocraftonline.townships.api;

/**
 * Author: gabizou
 */
public enum GroupType {
    CHARTER,
    OUTPOST,
    SMALL,
    MEDIUM,
    LARGE,
    HUGE;

    public GroupType nextUpgrade() {
        switch (this) {
            case CHARTER:
                return OUTPOST;
            case OUTPOST:
                return SMALL;
            case SMALL:
                return MEDIUM;
            case MEDIUM:
                return LARGE;
            case LARGE:
                return HUGE;
            default:
                return null;
        }
    }

    public GroupType downgrade() {
        switch (this) {
            case CHARTER :
                return null;
            case OUTPOST :
                return CHARTER;
            case SMALL :
                return OUTPOST;
            case MEDIUM :
                return SMALL;
            case LARGE :
                return MEDIUM;
            case HUGE :
                return LARGE;
            default :
                return null;
        }
    }
}
