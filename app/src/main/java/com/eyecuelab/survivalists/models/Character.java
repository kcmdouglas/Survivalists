package com.eyecuelab.survivalists.models;

import com.eyecuelab.survivalists.entities.interfaces.Inventory;

import org.parceler.Parcel;

import java.util.ArrayList;

@Parcel
public class Character {
    String name;
    Integer age;
    Integer health;
    String characterPictureUrl;

    public Character() {
        //Required blank constructor
    }

    public Character(String name, Integer age, Integer health, String characterPictureUrl) {
        this.name = name;
        this.age = age;
        this.health = health;
        this.characterPictureUrl = characterPictureUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getHealth() {
        return health;
    }

    public void setHealth(Integer health) {
        this.health = health;
    }

    public String getCharacterPictureUrl() {
        return characterPictureUrl;
    }

    public void setCharacterPictureUrl(String characterPictureUrl) {
        this.characterPictureUrl = characterPictureUrl;
    }
}
