package com.eyecuelab.survivalists.models;

/**
 * Created by eyecue on 5/12/16.
 */
public class SafeHouse {
    public String houseId;
    public String houseName;
    public int stepsRequired;
    private boolean arrivedAtSafeHouse = false;

    public SafeHouse() {}

    public SafeHouse(String houseId, String houseName, int stepsRequired) {
        this.houseId = houseId;
        this.houseName = houseName;
        this.stepsRequired = stepsRequired;
    }

    public String getHouseId() {
        return houseId;
    }

    public String getHouseName() {
        return houseName;
    }

    public int getStepsRequired() {
        return stepsRequired;
    }

    public void setStepsRequired(int stepsRequired) {
        this.stepsRequired = stepsRequired;
    }

    public boolean reachSafeHouse(int dailySteps) {
        if (dailySteps == stepsRequired) {
            arrivedAtSafeHouse = true;
        }
        return arrivedAtSafeHouse;
    }

}
