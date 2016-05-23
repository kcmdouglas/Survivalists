package com.eyecuelab.survivalists.entities.interfaces;

import com.eyecuelab.survivalists.models.Character;

/**
 * Created by eyecue on 5/16/16.
 */
public interface Inventory {
    public void useItem(Character character);

    public String getName();

    public String getDescription();

    public void drop();

    public void pickup();
}
