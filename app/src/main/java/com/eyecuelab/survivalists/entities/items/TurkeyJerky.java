package com.eyecuelab.survivalists.entities.items;

import com.eyecuelab.survivalists.entities.interfaces.Inventory;

/**
 * Created by eyecue on 5/17/16.
 */
public class TurkeyJerky implements Inventory {
    public boolean equipped;
    private int itemEffect = 10;

    @Override
    public int useItem(int hunger) {
        return hunger + itemEffect;
    }

    @Override
    public String getName() {
        return "Turkey Jerky";
    }

    @Override
    public String getDescription() {
        return "The turkey gods must be smiling on you.";
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
