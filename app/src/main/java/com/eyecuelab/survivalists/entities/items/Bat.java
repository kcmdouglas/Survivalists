package com.eyecuelab.survivalists.entities.items;

import com.eyecuelab.survivalists.entities.interfaces.Weapon;

/**
 * Created by eyecue on 5/16/16.
 */
public class Bat implements Weapon {
    private int damageInflicted = 1;

    public boolean equipped;

    @Override
    public int useWeapon(int enemyHP) {
        return enemyHP - damageInflicted;
    }

    @Override
    public String getDescription() {
        return "This Louisville Slugger will come in handy.";
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
