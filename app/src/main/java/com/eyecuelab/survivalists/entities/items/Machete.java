package com.eyecuelab.survivalists.entities.items;

import com.eyecuelab.survivalists.entities.interfaces.Inventory;
import com.eyecuelab.survivalists.entities.interfaces.Weapon;

/**
 * Created by eyecue on 5/16/16.
 */
public class Machete implements Weapon {
    private int damageInflicted = 2;

    public boolean equipped;

    @Override
    public int useWeapon(int enemyHP) {
        return enemyHP - damageInflicted;
    }

    @Override
    public String getDescription() {
        return "The weapon of choice for uprising in tropical countries, also efficient at dispatching zombies.";
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
