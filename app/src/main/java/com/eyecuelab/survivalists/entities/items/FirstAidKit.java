package com.eyecuelab.survivalists.entities.items;

import com.eyecuelab.survivalists.entities.interfaces.Inventory;

/**
 * Created by eyecue on 5/17/16.
 */
public class FirstAidKit implements Inventory {
    public boolean equipped;
    public int itemEffect = 5;

    @Override
    public int useItem(int health) {
        return health + itemEffect;
    }

    @Override
    public String getName() {
        return "First Aid Kit";
    }

    @Override
    public String getDescription() {
        return "Bandages and morphine will get you through this... for now.";
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
