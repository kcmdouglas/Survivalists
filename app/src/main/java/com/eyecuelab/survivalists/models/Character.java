package com.eyecuelab.survivalists.models;

import com.eyecuelab.survivalists.entities.interfaces.Inventory;
import com.eyecuelab.survivalists.entities.interfaces.Weapon;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class Character {
    String name;
    Integer age;
    Integer health;
    Integer fullnessLevel;
    String characterPictureUrl;
    Integer characterId;
    List<Object> inventory = new ArrayList<Object>();

    public Character() {
        //Required blank constructor
    }

    public Character(String name, Integer age, Integer health, Integer fullnessLevel, String characterPictureUrl, Integer characterId) {
        this.name = name;
        this.age = age;
        this.health = health;
        this.fullnessLevel = fullnessLevel;
        this.characterPictureUrl = characterPictureUrl;
        this.characterId = characterId;
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

    public int getCharacterId() {
        return characterId;
    }

    public int getFullnessLevel() {
        return fullnessLevel;
    }

    public void setFullnessLevel(Integer fullnessLevel) {
        this.fullnessLevel = fullnessLevel;
    }

}
