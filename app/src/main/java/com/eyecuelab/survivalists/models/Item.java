package com.eyecuelab.survivalists.models;


import org.parceler.Parcel;

/**
 * Created by eyecuelab on 5/23/16.
 */
@Parcel
public class Item {
    String name;
    String description;
    int healthPoints;
    boolean effectsHealth;

    public Item () {}

    public Item(String name, String description, int healthPoints, boolean effectsHealth) {
        this.name=name;
        this.description = description;
        this.healthPoints = healthPoints;
        this.effectsHealth = effectsHealth;
    }

    public void useItem(Character character) {
        int startingEffectiveness = getHealthPoints();
        if (effectsHealth == true) {
            int priorHealth = character.getHealth();
            int currentHealth = priorHealth;
            int effectiveness = startingEffectiveness;

            while(currentHealth < 100 && effectiveness > 0) {
                effectiveness --;
                currentHealth ++;
            }

            character.setHealth(currentHealth);
            if ((100 - priorHealth) >= startingEffectiveness) {
                character.removeItem(this);
            } else {
                setHealthPoints(effectiveness);
            }
        } else {
            int priorFullness = character.getFullnessLevel();
            int currentFullness = priorFullness;
            int effectiveness = startingEffectiveness;

            while (currentFullness < 100 && effectiveness > 0) {
                effectiveness--;
                currentFullness++;
            }

            character.setFullnessLevel(currentFullness);
            if ((100 - priorFullness) >= startingEffectiveness) {
                character.removeItem(this);
            } else {
                setHealthPoints(effectiveness);
            }
        }
    }

    public String getName() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public int getHealthPoints() {
        return healthPoints;
    }

    public void setHealthPoints(int healthPoints) {
        this.healthPoints = healthPoints;
    }
}
