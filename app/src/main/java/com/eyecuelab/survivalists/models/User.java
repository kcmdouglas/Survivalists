package com.eyecuelab.survivalists.models;

import com.eyecuelab.survivalists.entities.interfaces.Inventory;

import java.util.ArrayList;

public class User {
    public String UID;
    public String displayName;
    public String teamId;
    public boolean atSafeHouse;
    public boolean joinedMatch;
    public Character playedCharacter;
    ArrayList<String> interactionsWithCharacterA;
    ArrayList<String> interactionsWithCharacterB;
    ArrayList<String> interactionsWithCharacterC;
    ArrayList<String> interactionsWithCharacterD;

    public User() {
    }

    public User (String UID, String  displayName, String teamId) {
        this.UID = UID;
        this.displayName = displayName;
        this.teamId = teamId;
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

    public ArrayList<String> getInteractionsWithCharacterA() {
        return interactionsWithCharacterA;
    }

    public ArrayList<String> getInteractionsWithCharacterB() {
        return interactionsWithCharacterB;
    }

    public ArrayList<String> getInteractionsWithCharacterC() {
        return interactionsWithCharacterC;
    }

    public ArrayList<String> getInteractionsWithCharacterD() {
        return interactionsWithCharacterD;
    }

    public void setPlayedCharacter(Character playedCharacter) { this.playedCharacter = playedCharacter; }

    public Character getPlayedCharacter() { return playedCharacter; }
}
