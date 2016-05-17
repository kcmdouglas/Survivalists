package com.eyecuelab.survivalists.entities.items;

import com.eyecuelab.survivalists.entities.interfaces.Weapon;

/**
 * Created by eyecue on 5/17/16.
 */
public class Axe implements Weapon {
    private int damageInflicted = 5;

    public boolean equipped;

    @Override
    public int useWeapon(int enemyHP) {
        return enemyHP - damageInflicted;
    }

    @Override
    public String getName() {
        return "Fireman's Axe";
    }

    @Override
    public String getDescription() {
        return "You're a lumber jack now!";
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
