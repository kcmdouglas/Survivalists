package com.eyecuelab.survivalists.entities.items;

import com.eyecuelab.survivalists.entities.interfaces.Weapon;

/**
 * Created by eyecue on 5/17/16.
 */
public class TireIron implements Weapon {
    private int damageInflicted = 2;
    public boolean equipped;

    @Override
    public int useWeapon(int enemyHP) {
        return enemyHP - damageInflicted;
    }

    @Override
    public String getName() {
        return "Tire Iron";
    }

    @Override
    public String getDescription() {
        return "Where we're going, we don't need roads!";
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
