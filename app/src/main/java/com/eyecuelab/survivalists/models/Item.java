package com.eyecuelab.survivalists.models;


import android.os.Parcelable;

import com.eyecuelab.survivalists.Constants;

import org.parceler.Parcel;

/**
 * Created by eyecuelab on 5/23/16.
 */
public class Item implements Parcelable, InventoryEntity {
    String name;
    String description;
    int healthPoints;
    boolean effectsHealth;
    int imageId;
    String pushId;

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

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    //Parcelling logic
    protected Item(android.os.Parcel in) {
        name = in.readString();
        description = in.readString();
        healthPoints = in.readInt();
        effectsHealth = in.readByte() != 0;
        imageId = in.readInt();
        pushId = in.readString();
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(android.os.Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeInt(healthPoints);
        dest.writeByte((byte) (effectsHealth ? 1 : 0));
        dest.writeInt(imageId);
        dest.writeString(pushId);
    }
}
