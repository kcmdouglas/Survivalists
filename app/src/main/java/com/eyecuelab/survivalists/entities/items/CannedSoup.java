package com.eyecuelab.survivalists.entities.items;

import com.eyecuelab.survivalists.entities.interfaces.Inventory;

/**
 * Created by eyecue on 5/16/16.
 */
public class CannedSoup implements Inventory {
    public boolean equipped;
    public int itemEffect = 5;

    @Override
    public int useItem(int health) {
        return health + itemEffect;
    }

    @Override
    public String getDescription() {
        return "Cold soup from the can.";
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
