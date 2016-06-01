package com.eyecuelab.survivalists.models;

/**
 * Created by eyecuelab on 5/23/16.
 */
public class Weapon{
    String name;
    String description;
    int hitPoints;

    public Weapon () {}

    public Weapon (Weapon otherWeapon) {
        this.name=otherWeapon.name;
        this.description=otherWeapon.description;
        this.hitPoints=otherWeapon.hitPoints;
    }

    public Weapon (String name, String description, int hitPoints) {
        this.name=name;
        this.description=description;
        this.hitPoints=hitPoints;
    }

    public int useWeapon(int enemyHP, Character character) {
        int result;
        result = hitPoints - enemyHP;
        if (result > 0) {
            setHitPoints(result);
        } else {
            int priorHealth = character.getHealth();
            int currentHealth = priorHealth + result;
            character.removeWeapon(this);
            character.setHealth(currentHealth);
        }
        return result;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public void setHitPoints(int hitPoints) {
        this.hitPoints = hitPoints;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
