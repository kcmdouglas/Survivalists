package com.eyecuelab.survivalists.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
    public ArrayList<Object> allItems;
    public ArrayList<Object> userInventory;
    public Object itemOne;
    public Object itemTwo;
    public Object selectedInventoryItem;
    public String playerId;

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
                    allItems.add(weapon);
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
                    userInventory.add(item);
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
                    userInventory.add(weapon);
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
                dismiss();
                break;
            case R.id.merchantCloseButton:
                dismiss();
                break;
        }
    }

    public void randomizeItems() {
        Collections.shuffle(allItems);
        Collections.shuffle(userInventory);
        selectedInventoryItem = userInventory.get(0);
        itemOne = allItems.get(0);
        itemTwo = allItems.get(1);



    }

}
