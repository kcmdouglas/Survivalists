package com.eyecuelab.survivalists.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
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
import com.eyecuelab.survivalists.util.ShakeDetector;
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

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;
    private MainActivity mainActivity;

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

    public static android.support.v4.app.DialogFragment newInstance(int number) {
        MerchantDialogFragment frag = new MerchantDialogFragment();
        Bundle args = new Bundle();
        args.putInt("number", number);

        frag.setArguments(args);
        return frag;
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mainActivity = (MainActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(android.support.v4.app.DialogFragment.STYLE_NO_FRAME, R.style.CustomFragment);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mEditor = mSharedPreferences.edit();
        Bundle bundle = getArguments();

        playerId = mSharedPreferences.getString(Constants.PREFERENCES_GOOGLE_PLAYER_ID, null);
        allItems = bundle.getParcelableArrayList("allItems");
        allWeapons = bundle.getParcelableArrayList("allWeapons");
        userWeaponInventory = bundle.getParcelableArrayList("userWeapons");
        userItemInventory = bundle.getParcelableArrayList("userItems");

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake() {
                createDialog();
            }
        });

        mSensorManager.registerListener(mShakeDetector, mAccelerometer,
                SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_merchant_dialog, container, false);
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        if(allWeapons.size() > 0 && allItems.size() > 0) {
            createDialog();
        } else {
            try {
                Thread.sleep(500);
                createDialog();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        acceptButton.setOnClickListener(this);
        merchantCloseButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.acceptButton:
                addItems();
                mSensorManager.unregisterListener(mShakeDetector);
                dismiss();
                break;
            case R.id.merchantCloseButton:
                mSensorManager.unregisterListener(mShakeDetector);
                dismiss();
                break;
        }
    }


    private void addItems() {
        switch (offerRandomizer){
            case 0:
                determineItemToRemove();
                addItemToInventory(weaponOne);
                mainActivity.setupGridView();
                break;
            case 1:
                determineItemToRemove();
                addItemToInventory(weaponOne);
                addItemToInventory(itemOne);
                mainActivity.setupGridView();
                break;
            case 2:
                determineItemToRemove();
                addItemToInventory(itemOne);
                addItemToInventory(itemTwo);
                mainActivity.setupGridView();
                break;
        }
    }

    private void determineItemToRemove() {
        if (userItemInventory != null && userWeaponInventory != null) {
            if (selectedItemChooser == 0) {
                removeItemFromInventory(selectedInventoryWeapon);
                mainActivity.userInventory.remove(selectedInventoryWeapon);
            } else {
                removeItemFromInventory(selectedInventoryItem);
                mainActivity.userInventory.remove(selectedInventoryItem);
            }
        } else if (userItemInventory != null) {
            removeItemFromInventory(selectedInventoryItem);
            mainActivity.userInventory.remove(selectedInventoryItem);
        } else if (userWeaponInventory != null){
            removeItemFromInventory(selectedInventoryWeapon);
            mainActivity.userInventory.remove(selectedInventoryWeapon);
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
            if (allWeapons.size() > 0 && allItems.size() > 0) {
                if (offerRandomizer == 0) {
                    weaponOne = allWeapons.get(0);
                    weaponTwo = allWeapons.get(1);

                    if (selectedInventoryItem != null && selectedInventoryWeapon != null) {
                        if (selectedItemChooser == 0) {
                            merchantOffer.setText(String.format("I see you have a nice %s, would you trade for these: %s, %s?", selectedInventoryWeapon.getName(), weaponOne.getName(), weaponTwo.getName()));
                        } else {
                            merchantOffer.setText(String.format("I see you have a nice %s, would you trade for these: %s, %s?", selectedInventoryItem.getName(), weaponOne.getName(), weaponTwo.getName()));
                        }
                    } else if (selectedInventoryItem != null) {
                        merchantOffer.setText(String.format("I see you have a nice %s, would you trade for these: %s, %s?", selectedInventoryItem.getName(), weaponOne.getName(), weaponTwo.getName()));
                    } else if (selectedInventoryWeapon != null) {
                        merchantOffer.setText(String.format("I see you have a nice %s, would you trade for these: %s, %s?", selectedInventoryWeapon.getName(), weaponOne.getName(), weaponTwo.getName()));
                    }
                } else if (offerRandomizer == 1) {
                    weaponOne = allWeapons.get(0);
                    itemOne = allItems.get(0);

                    if (selectedInventoryItem != null && selectedInventoryWeapon != null) {
                        if (selectedItemChooser == 0) {
                            merchantOffer.setText(String.format("I see you have a nice %s, would you trade for these: %s, %s?", selectedInventoryWeapon.getName(), weaponOne.getName(), itemOne.getName()));
                        } else {
                            merchantOffer.setText(String.format("I see you have a nice %s, would you trade for these: %s, %s?", selectedInventoryItem.getName(), weaponOne.getName(), itemOne.getName()));
                        }
                    } else if (selectedInventoryItem != null) {
                        merchantOffer.setText(String.format("I see you have a nice %s, would you trade for these: %s, %s?", selectedInventoryItem.getName(), weaponOne.getName(), itemOne.getName()));
                    } else if (selectedInventoryWeapon != null) {
                        merchantOffer.setText(String.format("I see you have a nice %s, would you trade for these: %s, %s?", selectedInventoryWeapon.getName(), weaponOne.getName(), itemOne.getName()));
                    }

                } else if (offerRandomizer == 2) {
                    itemOne = allItems.get(0);
                    itemTwo = allItems.get(1);

                    if (selectedInventoryItem != null && selectedInventoryWeapon != null) {
                        if (selectedItemChooser == 0) {
                            merchantOffer.setText(String.format("I see you have a nice %s, would you trade for these: %s, %s?", selectedInventoryWeapon.getName(), itemTwo.getName(), itemOne.getName()));
                        } else {
                            merchantOffer.setText(String.format("I see you have a nice %s, would you trade for these: %s, %s?", selectedInventoryItem.getName(), itemTwo.getName(), itemOne.getName()));
                        }
                    } else if (selectedInventoryItem != null) {
                        merchantOffer.setText(String.format("I see you have a nice %s, would you trade for these: %s, %s?", selectedInventoryItem.getName(), itemTwo.getName(), itemOne.getName()));
                    } else if (selectedInventoryWeapon != null) {
                        merchantOffer.setText(String.format("I see you have a nice %s, would you trade for these: %s, %s?", selectedInventoryWeapon.getName(), itemTwo.getName(), itemOne.getName()));
                    }
                }
            } else {
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
                    mainActivity.userInventory.add(weapon);
                } else {
                    Toast.makeText(mainActivity.getApplicationContext(), "No room in weapon inventory for " + weapon.getName(), Toast.LENGTH_LONG).show();
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
                    mainActivity.userInventory.add(item);
                } else {
                    Toast.makeText(mainActivity.getApplicationContext(), "No room in item inventory for " + item.getName(), Toast.LENGTH_LONG).show();
                }
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
