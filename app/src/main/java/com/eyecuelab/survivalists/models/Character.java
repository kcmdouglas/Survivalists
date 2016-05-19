package com.eyecuelab.survivalists.models;

import org.parceler.Parcel;

@Parcel
public class Character {
    String name;
    Integer age;
    Integer health;
    Integer fullnessLevel;
    String characterPictureUrl;
    Integer characterId;

    public Character() {
        //Required blank constructor
    }

    public Character(String name, Integer age, Integer health, Integer fullnessLevel, String characterPictureUrl, Integer characterId) {
        this.name = name;
        this.age = age;
        this.health = health;
        this.fullnessLevel = fullnessLevel
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
