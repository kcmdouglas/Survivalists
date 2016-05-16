package com.eyecuelab.survivalists.entities.items;

import com.eyecuelab.survivalists.entities.interfaces.Inventory;

/**
 * Created by eyecue on 5/16/16.
 */
public class WaterBottle implements Inventory {
    public boolean equipped;
    public int itemEffect = 1;

    @Override
    public int useItem(int health) {
        return health + itemEffect;
    }

    @Override
    public String getDescription() {
        return "Stay hydrated, stay healthy.";
    }

    @Override
    public void drop() {
    equipped = false;
    }

    @Override
    public void pickup() {
    equipped = true;
    }
}
