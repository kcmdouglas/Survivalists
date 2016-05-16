package com.eyecuelab.survivalists.models;

/**
 * Created by eyecuelab on 5/10/16.
 */
public class User {
    public String UID;
    public String displayName;
    public String teamId;
    public boolean atSafeHouse;
    public boolean joinedMatch;

    public User() {
    }

    public User (String UID, String  displayName, String teamId, boolean atSafeHouse, boolean joinedMatch) {
        this.UID = UID;
        this.displayName = displayName;
        this.teamId = teamId;
        this.atSafeHouse = atSafeHouse;
        this.joinedMatch = joinedMatch;
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

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamID) {
        this.teamId = teamId;
    }

    public boolean isAtSafeHouse() {
        return atSafeHouse;
    }

    public void setAtSafeHouse(boolean atSafeHouse) {
        this.atSafeHouse = atSafeHouse;
    }

    public boolean isJoinedMatch() {
        return joinedMatch;
    }

    public void setJoinedMatch(boolean joinedMatch) {
        this.joinedMatch = joinedMatch;
    }
}
