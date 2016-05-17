package com.eyecuelab.survivalists.entities.items;

import com.eyecuelab.survivalists.entities.interfaces.Inventory;

/**
 * Created by eyecue on 5/17/16.
 */
public class CliffBar implements Inventory {
    public boolean equipped;
    private int itemEffect = 5;

    @Override
    public int useItem(int hunger) {
        return hunger + itemEffect;
    }

    @Override
    public String getName() {
        return "Cliff Bar";
    }

    @Override
    public String getDescription() {
        //TODO: Write better descriptions
        return "Avoiding zombies is a great workout.";
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
