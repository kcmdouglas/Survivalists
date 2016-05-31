package com.eyecuelab.survivalists.models;

import org.parceler.Parcel;

/**
 * Created by eyecue on 5/12/16.
 */

@Parcel
public class SafeHouse {
    int houseId;
    String houseName;
    String description;

    public SafeHouse() {}

    public SafeHouse(SafeHouse safehouse) {
        this.houseId = safehouse.houseId;
        this.houseName = safehouse.houseName;
        this.description = safehouse.description;
    }

    public SafeHouse(int houseId, String houseName, String description) {
        this.houseId = houseId;
        this.houseName = houseName;
        this.description = description;
    }

    public int getHouseId() {
        return houseId;
    }

    public String getHouseName() {
        return houseName;
    }

    public String getDescription() {
        return description;
    }


}
