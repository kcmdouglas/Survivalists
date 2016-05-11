package com.eyecuelab.survivalists.models;

/**
 * Created by eyecuelab on 5/10/16.
 */
public class User {
    public String UID;
    public String displayName;
    public String teamID;

    public User() {
    }

    public User (String UID, String  displayName, String teamID) {
        this.UID = UID;
        this.displayName = displayName;
        this.teamID = teamID;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getTeamID() {
        return teamID;
    }

    public void setTeamID(String teamID) {
        this.teamID = teamID;
    }
}
