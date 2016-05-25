package com.eyecuelab.survivalists.models;

import android.util.Log;


import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class Character {
    String name;
    String description;
    Integer age;
    Integer health;
    Integer fullnessLevel;
    String characterPictureUrl;
    Integer characterId;
    ArrayList<Object> inventory = new ArrayList<Object>();

    public Character() {
        //Required blank constructor
    }

    public Character(Character anotherCharacter) {
        this.name = anotherCharacter.name;
        this.description = anotherCharacter.description;
        this.age = anotherCharacter.age;
        this.health = anotherCharacter.health;
        this.fullnessLevel = anotherCharacter.fullnessLevel;
        this.characterPictureUrl = anotherCharacter.characterPictureUrl;
        this.characterId = anotherCharacter.characterId;
    }

    public Character(String name, String description, Integer age, Integer health, Integer fullnessLevel, String characterPictureUrl, Integer characterId) {
        this.name = name;
        this.description = description;
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

    public List<Object> getInventory () {
        return inventory;
    }

    public void addToInventory(Object item) {
        if (inventory.size() < 16) {
            inventory.add(item);
            Log.d("Inventory: ", "Item Added");
        } else {
            Log.d("Inventory: ", "Cannot add, inventory full");
        }
    }

    public void removeWeapon(com.eyecuelab.survivalists.models.Weapon weapon) {
        for(int i=0; i < inventory.size(); i++) {
            boolean result = inventory.get(i) instanceof com.eyecuelab.survivalists.models.Weapon;
            com.eyecuelab.survivalists.models.Weapon inventoryWeapon = null;
            if (result) {
                inventoryWeapon = (com.eyecuelab.survivalists.models.Weapon) inventory.get(i);
                if((inventoryWeapon.getName().equals(weapon.getName())) && (inventoryWeapon.getHitPoints() == weapon.getHitPoints())) {
                    inventory.remove(i);
                }
            }
        }
    }

    public void removeItem(Item item) {
        for(int i=0; i < inventory.size(); i++) {
            boolean result = inventory.get(i) instanceof Item;
            Item inventoryItem = null;
            if (result) {
                inventoryItem = (Item) inventory.get(i);
                if((inventoryItem.getName().equals(item.getName())) && (inventoryItem.getHealthPoints() == item.getHealthPoints())) {
                    inventory.remove(i);
                }
            }
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCharacterId(Integer characterId) {
        this.characterId = characterId;
    }

    public void setInventory(ArrayList<Object> inventory) {
        this.inventory = inventory;
    }
}
