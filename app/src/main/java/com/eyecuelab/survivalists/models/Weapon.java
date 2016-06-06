package com.eyecuelab.survivalists.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.eyecuelab.survivalists.Constants;

/**
 * Created by eyecuelab on 5/23/16.
 */
public class Weapon implements Parcelable, InventoryEntity {
    String name;
    String description;
    int hitPoints;
    String pushId;
    int imageId;

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

    protected Weapon(Parcel in) {
        name = in.readString();
        description = in.readString();
        hitPoints = in.readInt();
        pushId = in.readString();
    }

    public static final Creator<Weapon> CREATOR = new Creator<Weapon>() {
        @Override
        public Weapon createFromParcel(Parcel in) {
            return new Weapon(in);
        }

        @Override
        public Weapon[] newArray(int size) {
            return new Weapon[size];
        }
    };

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

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getImageId() {
        return imageId;
    }

    @Override
    public int getItemType() {
        return Constants.WEAPON_TAG;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeInt(hitPoints);
        dest.writeString(pushId);
    }
}
