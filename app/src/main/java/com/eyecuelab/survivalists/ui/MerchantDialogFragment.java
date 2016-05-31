package com.eyecuelab.survivalists.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.eyecuelab.survivalists.Constants;
import com.eyecuelab.survivalists.R;
import com.eyecuelab.survivalists.models.Item;
import com.eyecuelab.survivalists.models.SafeHouse;
import com.eyecuelab.survivalists.models.Weapon;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.parceler.Parcels;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by eyecuelab on 5/31/16.
 */
public class MerchantDialogFragment extends android.support.v4.app.DialogFragment implements View.OnClickListener{

    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;

    @Bind(R.id.merchantCloseButton) Button merchantCloseButton;
    @Bind(R.id.acceptButton) Button acceptButton;
    @Bind(R.id.merchantOffer) TextView merchantOffer;
    public ArrayList<Item> allItems;
    public ArrayList<Weapon> allWeapons;
    public ArrayList<Weapon> userWeaponInventory;
    public ArrayList<Item> userItemInventory;
    public Item itemOne;
    public Item itemTwo;
    public Weapon weaponOne;
    public Weapon weaponTwo;
    public Item selectedInventoryItem;
    public Weapon selectedInventoryWeapon;
    public String playerId;
    public Integer offerRandomizer;
    public Integer selectedItemChooser;

    //empty constructor required for dialog fragments
    public MerchantDialogFragment() {};

    public static android.support.v4.app.DialogFragment newInstance(int number, SafeHouse safehouse) {
        MerchantDialogFragment frag = new MerchantDialogFragment();
        Bundle args = new Bundle();
        args.putInt("number", number);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mEditor = mSharedPreferences.edit();
        playerId = mSharedPreferences.getString(Constants.PREFERENCES_GOOGLE_PLAYER_ID, null);



        allItems = new ArrayList<>();

        Firebase itemRef = new Firebase(Constants.FIREBASE_URL_ITEMS);

        itemRef.child("weapons").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child: dataSnapshot.getChildren()) {
                    String name = child.child("name").getValue().toString();
                    String description = child.child("description").getValue().toString();
                    long hitPointsLong = (long) child.child("hit_points").getValue();
                    int hitPoints = (int) hitPointsLong;
                    Weapon weapon = new Weapon(name, description, hitPoints);
                    allWeapons.add(weapon);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        itemRef.child("food").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child: dataSnapshot.getChildren()) {
                    String name = child.child("name").getValue().toString();
                    String description = child.child("description").getValue().toString();
                    long hitPointsLong = (long) child.child("health_points").getValue();
                    int hitPoints = (int) hitPointsLong;
                    boolean effectsHealth = (boolean) child.child("effects_health").getValue();
                    Item item = new Item(name, description, hitPoints, effectsHealth);
                    allItems.add(item);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        itemRef.child("medicine").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child: dataSnapshot.getChildren()) {
                    String name = child.child("name").getValue().toString();
                    String description = child.child("description").getValue().toString();
                    long hitPointsLong = (long) child.child("hit_points").getValue();
                    int hitPoints = (int) hitPointsLong;
                    boolean effectsHealth = (boolean) child.child("effects_health").getValue();
                    Item item = new Item(name, description, hitPoints, effectsHealth);
                    allItems.add(item);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        Firebase userRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + playerId + "/character");

        userRef.child("items").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren()) {
                    String name = child.child("name").getValue().toString();
                    String description = child.child("description").getValue().toString();
                    long hitPointsLong = (long) child.child("hit_points").getValue();
                    int hitPoints = (int) hitPointsLong;
                    boolean effectsHealth = (boolean) child.child("effects_health").getValue();
                    Item item = new Item(name, description, hitPoints, effectsHealth);
                    userItemInventory.add(item);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        userRef.child("weapons").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child: dataSnapshot.getChildren()) {
                    String name = child.child("name").getValue().toString();
                    String description = child.child("description").getValue().toString();
                    long hitPointsLong = (long) child.child("hit_points").getValue();
                    int hitPoints = (int) hitPointsLong;
                    Weapon weapon = new Weapon(name, description, hitPoints);
                    userWeaponInventory.add(weapon);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_merchant_dialog, container, false);
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);


        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.acceptButton:
                addItems();
                dismiss();
                break;
            case R.id.merchantCloseButton:
                dismiss();
                break;
        }
    }

    private void addItems() {
        switch (offerRandomizer){
            case 0:
                determineItemToRemove();
                addItemToInventory(weaponOne);
                addItemToInventory(weaponTwo);
                break;
            case 1:
                determineItemToRemove();
                addItemToInventory(weaponOne);
                addItemToInventory(itemOne);
                break;
            case 2:
                determineItemToRemove();
                addItemToInventory(itemOne);
                addItemToInventory(itemTwo);
                break;
        }
    }

    private void determineItemToRemove() {
        if (userItemInventory != null && userWeaponInventory != null) {
            if (selectedItemChooser == 0) {
                removeItemFromInventory(selectedInventoryWeapon);
            } else {
                removeItemFromInventory(selectedInventoryItem);
            }
        } else if (userItemInventory != null) {
            removeItemFromInventory(selectedInventoryItem);
        } else if (userWeaponInventory != null){
            removeItemFromInventory(selectedInventoryWeapon);
        }
    }

    private void removeItemFromInventory(final Weapon weapon) {
        Firebase inventoryRef = new Firebase (Constants.FIREBASE_URL_USERS + "/" + playerId + "/character/weapons");

        inventoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if (child.getKey().toString().equals(weapon.getName()))  {
                        child.getRef().removeValue();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
    private void removeItemFromInventory(final Item item) {
        Firebase inventoryRef = new Firebase (Constants.FIREBASE_URL_USERS + "/" + playerId + "/character/items");

        inventoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if (child.getKey().toString().equals(item.getName()))  {
                        child.getRef().removeValue();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void randomizeItems() {
        //0 is weapon, 1 is item
        selectedItemChooser = (int) (Math.random() +.5);
        offerRandomizer = (int) (Math.random() * 2);
        Collections.shuffle(allItems);

        if(userItemInventory != null) {
            Collections.shuffle(userItemInventory);
            selectedInventoryItem = userItemInventory.get(0);
        }

        if(userWeaponInventory != null) {
            Collections.shuffle(userWeaponInventory);
            selectedInventoryWeapon = userWeaponInventory.get(0);
        }

        if (userWeaponInventory != null || userItemInventory != null) {
            if (offerRandomizer == 0) {
                weaponOne = allWeapons.get(0);
                weaponTwo = allWeapons.get(1);

                if (selectedInventoryItem != null && selectedInventoryWeapon !=null) {
                    if (selectedItemChooser == 0) {
                        merchantOffer.setText(String.format("I see you have a nice %s, would you trade for these: %s, %s?", selectedInventoryWeapon.getName(), weaponOne.getName(), weaponTwo.getName()));
                    } else {
                        merchantOffer.setText(String.format("I see you have a nice %s, would you trade for these: %s, %s?", selectedInventoryItem.getName(), weaponOne.getName(), weaponTwo.getName()));
                    }
                } else if (selectedInventoryItem != null){
                    merchantOffer.setText(String.format("I see you have a nice %s, would you trade for these: %s, %s?", selectedInventoryItem.getName(), weaponOne.getName(), weaponTwo.getName()));
                } else if (selectedInventoryWeapon != null) {
                    merchantOffer.setText(String.format("I see you have a nice %s, would you trade for these: %s, %s?", selectedInventoryWeapon.getName(), weaponOne.getName(), weaponTwo.getName()));
                }
            } else if (offerRandomizer == 1) {
                weaponOne = allWeapons.get(0);
                itemOne = allItems.get(0);

                if (selectedInventoryItem != null && selectedInventoryWeapon !=null) {
                    if (selectedItemChooser == 0) {
                        merchantOffer.setText(String.format("I see you have a nice %s, would you trade for these: %s, %s?", selectedInventoryWeapon.getName(), weaponOne.getName(), itemOne.getName()));
                    } else {
                        merchantOffer.setText(String.format("I see you have a nice %s, would you trade for these: %s, %s?", selectedInventoryItem.getName(), weaponOne.getName(), itemOne.getName()));
                    }
                } else if (selectedInventoryItem != null){
                    merchantOffer.setText(String.format("I see you have a nice %s, would you trade for these: %s, %s?", selectedInventoryItem.getName(), weaponOne.getName(), itemOne.getName()));
                } else if (selectedInventoryWeapon != null) {
                    merchantOffer.setText(String.format("I see you have a nice %s, would you trade for these: %s, %s?", selectedInventoryWeapon.getName(), weaponOne.getName(), itemOne.getName()));
                }

            } else if (offerRandomizer == 2) {
                itemOne = allItems.get(0);
                itemTwo = allItems.get(1);

                if (selectedInventoryItem != null && selectedInventoryWeapon !=null) {
                    if (selectedItemChooser == 0) {
                        merchantOffer.setText(String.format("I see you have a nice %s, would you trade for these: %s, %s?", selectedInventoryWeapon.getName(), itemTwo.getName(), itemOne.getName()));
                    } else {
                        merchantOffer.setText(String.format("I see you have a nice %s, would you trade for these: %s, %s?", selectedInventoryItem.getName(), itemTwo.getName(), itemOne.getName()));
                    }
                } else if (selectedInventoryItem != null){
                    merchantOffer.setText(String.format("I see you have a nice %s, would you trade for these: %s, %s?", selectedInventoryItem.getName(), itemTwo.getName(), itemOne.getName()));
                } else if (selectedInventoryWeapon != null) {
                    merchantOffer.setText(String.format("I see you have a nice %s, would you trade for these: %s, %s?", selectedInventoryWeapon.getName(), itemTwo.getName(), itemOne.getName()));
                }
            }
        } else {
            acceptButton.setEnabled(false);
            merchantOffer.setText("Seems like you have nothing to barter...");
        }
    }

    private void addItemToInventory(final Weapon weapon) {
        final Firebase mFirebaseInventoryUpdate = new Firebase(Constants.FIREBASE_URL_USERS + "/" + playerId + "/character/weapons/");

        mFirebaseInventoryUpdate.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int itemAmount = (int) dataSnapshot.getChildrenCount();
                if (itemAmount < 4) {
                    mFirebaseInventoryUpdate.child(weapon.getName()).setValue(weapon);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void addItemToInventory(final Item item) {
        final Firebase mFirebaseInventoryUpdate = new Firebase(Constants.FIREBASE_URL_USERS + "/" + playerId + "/character/items/");

        mFirebaseInventoryUpdate.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int itemAmount = (int) dataSnapshot.getChildrenCount();
                if (itemAmount < 12) {
                    mFirebaseInventoryUpdate.child(item.getName()).setValue(item);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

}
