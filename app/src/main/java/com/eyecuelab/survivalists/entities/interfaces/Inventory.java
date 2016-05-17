package com.eyecuelab.survivalists.entities.interfaces;

/**
 * Created by eyecue on 5/16/16.
 */
public interface Inventory {

    public int useItem(int effectedCharacteristic);

    public String getName();

    public String getDescription();

    public void drop();

    public void pickup();
}
