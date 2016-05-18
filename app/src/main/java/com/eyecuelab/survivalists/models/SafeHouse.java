package com.eyecuelab.survivalists.models;

import org.parceler.Parcel;

/**
 * Created by eyecue on 5/12/16.
 */

@Parcel
public class SafeHouse {
    String houseId;
    String houseName;
    String description;
    int stepsRequired;
    boolean arrivedAtSafeHouse = false;

    public SafeHouse() {}

    public SafeHouse(String houseId, String houseName, String description,int stepsRequired) {
        this.houseId = houseId;
        this.houseName = houseName;
        this.description = description;
        this.stepsRequired = stepsRequired;
    }

    public String getHouseId() {
        return houseId;
    }

    public String getHouseName() {
        return houseName;
    }

    public String getDescription() {
        return description;
    }

    public int getStepsRequired() {
        return stepsRequired;
    }

    public boolean reachedSafehouse(int dailySteps) {
        if (dailySteps >= stepsRequired) {
            arrivedAtSafeHouse = true;
        }
        return arrivedAtSafeHouse;
    }

    public int stepsLeftToHouse(int dailySteps) {
        if (dailySteps < stepsRequired) {
            return stepsRequired - dailySteps;
        } else {
            return -1;
        }
    }

}
