package com.eyecuelab.survivalists.models;

import java.util.ArrayList;

/**
 * Created by eyecue on 5/18/16.
 */
public class Campaign {
    int campaignId;
    ArrayList<Character> playerArray;
    ArrayList<Boolean> isSafeArray;
    String difficulty;

    public Campaign() {}

    public Campaign(int campaignId, ArrayList<Character> playerArray, ArrayList<Boolean> isSafeArray, String difficulty) {
        this.campaignId = campaignId;
        this.playerArray = playerArray;
        this.isSafeArray = isSafeArray;
        this.difficulty = difficulty;
    }

    public int getCampaignId() {
        return campaignId;
    }

    public ArrayList<Character> getPlayerArray() {
        return playerArray;
    }

    public ArrayList<Boolean> getIsSafeArray() {
        return isSafeArray;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setCampaignId(int campaignId) {
        this.campaignId = campaignId;
    }

    public void setPlayerArray(ArrayList<Character> playerArray) {
        this.playerArray = playerArray;
    }

    public void setIsSafeArray(ArrayList<Boolean> isSafeArray) {
        this.isSafeArray = isSafeArray;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}
