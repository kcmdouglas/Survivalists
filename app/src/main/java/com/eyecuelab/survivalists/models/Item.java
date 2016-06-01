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
    int imageId;

    public Item () {}

    public Item(Item otherItem) {
        this.name=otherItem.name;
        this.description = otherItem.description;
        this.healthPoints = otherItem.healthPoints;
        this.effectsHealth = otherItem.effectsHealth;
    }

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
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getHealthPoints() {
        return healthPoints;
    }

    public int getImageId() {
        return imageId;
    }

    public void setHealthPoints(int healthPoints) {
        this.healthPoints = healthPoints;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEffectsHealth(boolean effectsHealth) {
        this.effectsHealth = effectsHealth;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
}
