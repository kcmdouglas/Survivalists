package com.eyecuelab.survivalists.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
public class MerchantDialogFragment extends android.support.v4.app.DialogFragment implements View.OnClickListener, SensorEventListener{


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

    public void init(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor shakeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public static android.support.v4.app.DialogFragment newInstance(int number) {
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
        allWeapons = new ArrayList<>();
        allItems = new ArrayList<>();
        userItemInventory = new ArrayList<>();
        userWeaponInventory = new ArrayList<>();

        instantiateAllItems();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_merchant_dialog, container, false);
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        instantiateInventory();

        acceptButton.setOnClickListener(this);
        merchantCloseButton.setOnClickListener(this);

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
        final Firebase inventoryRef = new Firebase (Constants.FIREBASE_URL_USERS + "/" + playerId + "/weapons");

        inventoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                inventoryRef.child(weapon.getPushId()).removeValue();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
    private void removeItemFromInventory(final Item item) {
        final Firebase inventoryRef = new Firebase (Constants.FIREBASE_URL_USERS + "/" + playerId + "/items");

        inventoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                inventoryRef.child(item.getPushId()).removeValue();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void randomizeItems() {


    }

    public void createDialog() {
        //0 is weapon, 1 is item
        selectedItemChooser = (int) (Math.random() + .5);
        offerRandomizer = (int) (Math.random() * 2);
        Collections.shuffle(allItems);
        Collections.shuffle(allWeapons);

        if (userItemInventory.size() > 0) {
            Collections.shuffle(userItemInventory);
            selectedInventoryItem = userItemInventory.get(0);
        }

        if (userWeaponInventory.size() > 0) {
            Collections.shuffle(userWeaponInventory);
            selectedInventoryWeapon = userWeaponInventory.get(0);
        }

        if (userWeaponInventory.size() > 0 || userItemInventory.size() > 0) {
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
//            acceptButton.setEnabled(false);
//            merchantOffer.setText("Seems like you have nothing to barter...");
        }
    }

    private void addItemToInventory(final Weapon weapon) {
        final Firebase weaponUpdate = new Firebase(Constants.FIREBASE_URL_USERS + "/" + playerId + "/weapons/");

        weaponUpdate.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int itemAmount = (int) dataSnapshot.getChildrenCount();
                if (itemAmount < 4) {
                    Firebase newWeaponRef = weaponUpdate.push();
                    String weaponPushId = newWeaponRef.getKey();
                    weapon.setPushId(weaponPushId);
                    newWeaponRef.setValue(weapon);
                } else {
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void addItemToInventory(final Item item) {
        final Firebase itemUpdate = new Firebase(Constants.FIREBASE_URL_USERS + "/" + playerId + "/items/");

        itemUpdate.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int itemAmount = (int) dataSnapshot.getChildrenCount();
                if (itemAmount < 12) {
                    Firebase newItemRef = itemUpdate.push();
                    String itemPushId = newItemRef.getKey();
                    item.setPushId(itemPushId);
                    newItemRef.setValue(item);
                } else {
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void instantiateAllItems() {

        Firebase itemRef = new Firebase(Constants.FIREBASE_URL_ITEMS +"/");

        itemRef.child("weapons").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child: dataSnapshot.getChildren()) {
                    Weapon weapon = child.getValue(Weapon.class);
                    Log.d("Weapon:", weapon + "");
                    Log.d("Name:", weapon.getName());
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
                    Item item = child.getValue(Item.class);
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
                    Item item = child.getValue(Item.class);
                    allItems.add(item);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    public void instantiateInventory() {

        Firebase userRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + playerId + "/");

        userRef.child("items").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot iterateItem : dataSnapshot.getChildren()) {
                    Item item = new Item(iterateItem.getValue(Item.class));
                    item.setPushId(iterateItem.child("pushId").getValue().toString());
                    userItemInventory.add(item);
                    Log.d("Item Inventory", userItemInventory.size() + "");
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        userRef.child("weapons").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot iterateWeapon: dataSnapshot.getChildren()) {
                    Weapon weapon = new Weapon(iterateWeapon.getValue(Weapon.class));
                    weapon.setPushId(iterateWeapon.child("pushId").getValue().toString());
                    userWeaponInventory.add(weapon);
                    Log.d("Weapon Inventory", userWeaponInventory.size() + "");
                }
                createDialog();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
