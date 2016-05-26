package com.eyecuelab.survivalists.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eyecuelab.survivalists.Constants;
import com.eyecuelab.survivalists.R;
import com.eyecuelab.survivalists.models.Character;
import com.eyecuelab.survivalists.models.Item;
import com.eyecuelab.survivalists.models.Weapon;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by eyecuelab on 5/13/16.
 */
public class EventDialogFragment extends android.support.v4.app.DialogFragment implements View.OnClickListener {
    private TextView dialogDescription;
    private TextView dialogConsequence;
    private TextView dialogTitle;
    private Button affirmativeButton;
    private Button negativeButton;
    private Button closeButton;
    private int dialogChooser;
    private String[] dialogOptions;
    private Resources res;
    private Firebase mFirebaseEventRef;
    private Firebase mFirebaseItemRef;
    private String description;
    private String title;
    private String outcomeA;
    private String outcomeB;
    private int penaltyHP;
    private int stepsRequired;
    private boolean getItemOnFlee = false;
    private boolean getItemOnInspect = false;
    private Weapon weapon = null;
    private Item item = null;
    private boolean effectsHealth;
    private int attackOrInspect;
    private Firebase mFirebaseStepsRef;
    private String mPlayerId;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private Character mCurrentCharacter;


    //Empty constructor required for DialogFragments
    public EventDialogFragment(){}

    public static android.support.v4.app.DialogFragment newInstance(int number) {
        EventDialogFragment frag = new EventDialogFragment();

        Bundle args = new Bundle();
        args.putInt("number", number);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_dialog, container, false);
        super.onViewCreated(view, savedInstanceState);
        mPlayerId = mSharedPreferences.getString(Constants.PREFERENCES_GOOGLE_PLAYER_ID, null);
        String characterJson = mSharedPreferences.getString(Constants.PREFERENCES_CHARACTER, null);
        Gson gson = new Gson();
        mCurrentCharacter = gson.fromJson(characterJson, Character.class);


        //TODO: change this to account for the size of the hashmap array in firebase--maybe move into an indv. listener
        int eventNumber = (int) Math.floor(Math.random() * 10 + 1);

        //0 is an attack event, 1 is an inspect event
        attackOrInspect = (int) (Math.random() +0.5);

        affirmativeButton = (Button) view.findViewById(R.id.affirmativeButton);
        affirmativeButton.setOnClickListener(this);
        negativeButton = (Button) view.findViewById(R.id.negativeButton);
        negativeButton.setOnClickListener(this);
        closeButton = (Button) view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(this);
        closeButton.setVisibility(View.GONE);

        if(attackOrInspect == 0) {
            mFirebaseEventRef = new Firebase(Constants.FIREBASE_URL_EVENTS + "/attack/");
            mFirebaseEventRef.child(Integer.toString(eventNumber)).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            description = dataSnapshot.child("description").getValue().toString();
                            outcomeA = dataSnapshot.child("description").getValue().toString();
                            outcomeB = dataSnapshot.child("description").getValue().toString();
                            title = dataSnapshot.child("description").getValue().toString();
                            penaltyHP = (int) dataSnapshot.child("penalty_hp").getValue();
                            stepsRequired = (int) dataSnapshot.child("steps_required").getValue();
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    }
            );
            dialogConsequence.setVisibility(View.GONE);
            affirmativeButton.setText("Attack");
            negativeButton.setText("Run");

        } else {
            mFirebaseEventRef = new Firebase(Constants.FIREBASE_URL_EVENTS + "/inspect/");
            mFirebaseEventRef.child(Integer.toString(eventNumber)).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            description = dataSnapshot.child("description").getValue().toString();
                            outcomeA = dataSnapshot.child("description").getValue().toString();
                            outcomeB = dataSnapshot.child("description").getValue().toString();
                            title = dataSnapshot.child("description").getValue().toString();
                            penaltyHP = (int) dataSnapshot.child("penalty_hp").getValue();
                            stepsRequired = (int) dataSnapshot.child("steps_required").getValue();
                            getItemOnFlee = (boolean) dataSnapshot.child("get_item_on_flee").getValue();
                            getItemOnInspect = (boolean) dataSnapshot.child("get_item_on_flee").getValue();
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    }
            );
            affirmativeButton.setText("Inspect");
            negativeButton.setText("Ignore");
            if (stepsRequired > 0) {
                dialogConsequence.setText("Inspecting will add " + Integer.toString(stepsRequired) + " to your daily goal.");
            } else {
                dialogConsequence.setVisibility(View.GONE);
            }
        }



        if (getItemOnInspect || getItemOnFlee) {
            int categoryRandomizer = (int) Math.floor(Math.random() * 3 + 1);
            int itemRandomizer = (int) Math.floor(Math.random() * 10 + 1);

            switch (categoryRandomizer) {
                case 1:
                    mFirebaseItemRef = new Firebase(Constants.FIREBASE_URL_ITEMS + "/food/");
                    effectsHealth = false;
                    break;
                case 2:
                    mFirebaseItemRef = new Firebase(Constants.FIREBASE_URL_ITEMS + "/medicine/");
                    effectsHealth = true;
                    break;
                case 3:
                    mFirebaseItemRef = new Firebase(Constants.FIREBASE_URL_ITEMS + "/weapons/");
                    weapon = new Weapon("name", "description", 0);
                    break;
            }

            mFirebaseItemRef.child(Integer.toString(itemRandomizer)).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (weapon != null) {
                                String weaponName = dataSnapshot.child("name").getValue().toString();
                                String weaponDescription = dataSnapshot.child("description").getValue().toString();
                                int hitPoints = (int) dataSnapshot.child("hit_points").getValue();
                                weapon = new Weapon(weaponName, weaponDescription, hitPoints);
                            } else {
                                String itemName = dataSnapshot.child("name").getValue().toString();
                                String itemDescription = dataSnapshot.child("description").getValue().toString();
                                int healthPoints = (int) dataSnapshot.child("health_points").getValue();
                                item = new Item(itemName, itemDescription, healthPoints, effectsHealth);
                            }

                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    }
            );
        }

        dialogDescription = (TextView) view.findViewById(R.id.dialogDescription);
        dialogConsequence = (TextView) view.findViewById(R.id.dialogConsequence);
        dialogTitle = (TextView) view.findViewById(R.id.dialogTitle);
        res = getResources();


        dialogTitle.setText(title);
        dialogDescription.setText(description);


        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.affirmativeButton:
                affirmativeClick(dialogChooser);
                break;
            case R.id.negativeButton:
                negativeClick(dialogChooser);
                break;
            case R.id.closeButton:
                dismiss();
                break;
        }
    }

    private void affirmativeClick(int dialogNumber) {
        configureResultLayout();

        dialogDescription.setText(outcomeA);

        if(attackOrInspect == 0) {
            int characterHealth = mCurrentCharacter.getHealth();
            int remainderHealth = 0;
            Weapon weapon = null;

            for (int i = 0; i < mCurrentCharacter.getInventory().size(); i++) {
                    Object currentItem = mCurrentCharacter.getInventory().get(i);
                    if(currentItem instanceof Weapon) {
                        weapon = (Weapon) currentItem;
                    }
                }

            if (weapon == null) {
                remainderHealth = characterHealth- penaltyHP;
            } else {
                weapon.useWeapon(penaltyHP, mCurrentCharacter);
            }

            Firebase firebaseUserRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + mPlayerId + "/character");
            firebaseUserRef.setValue(mCurrentCharacter);

        }


        //Consequence for Inspect events
        if(attackOrInspect == 1) {
            if(getItemOnInspect) {
                if (weapon != null) {
                    dialogConsequence.setText("FOUND " + weapon.getName());
                    addItemToInventory(weapon);
                } else {
                    dialogConsequence.setText("FOUND " + item.getName());
                    addItemToInventory(item);
                }
            } else {
                dialogConsequence.setText("NO ITEMS FOUND");
            }
            updateSteps(stepsRequired);
        }

    }

    private void negativeClick(int dialogNumber) {
        configureResultLayout();

        dialogDescription.setText(outcomeB);

        if(attackOrInspect == 0) {
            dialogConsequence.setText(Integer.toString(stepsRequired) + " STEPS ADDED TO DAILY GOAL");
            updateSteps(stepsRequired);
        } else {
            if(getItemOnFlee) {
                if (weapon != null) {
                    dialogConsequence.setText("FOUND " + weapon.getName());
                    addItemToInventory(weapon);

                } else {
                    dialogConsequence.setText("FOUND " + item.getName());
                    addItemToInventory(item);

                }
            }
        }

    }

    private void updateSteps(final int stepsRequired) {
        final Firebase mFirebaseStepUpdate = new Firebase(Constants.FIREBASE_URL_USERS + "/" + mPlayerId + "/dailyGoal");
        mFirebaseStepUpdate.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long dailyGoalLong = (long)  dataSnapshot.getValue();
                int priorDailyGoal = (int) dailyGoalLong;
                int newDailyGoal = priorDailyGoal + stepsRequired;
                mFirebaseStepUpdate.setValue(newDailyGoal);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void configureResultLayout() {
        dialogTitle.setVisibility(View.GONE);
        affirmativeButton.setVisibility(View.GONE);
        negativeButton.setVisibility(View.GONE);
        closeButton.setVisibility(View.VISIBLE);
        dialogConsequence.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)dialogDescription.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        dialogDescription.setLayoutParams(params);
    }

    private void addItemToInventory(final Weapon weapon) {
        final Firebase mFirebaseInventoryUpdate = new Firebase(Constants.FIREBASE_URL_USERS + "/" + mPlayerId + "/character/inventory/");

        mFirebaseInventoryUpdate.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int itemAmount = (int) dataSnapshot.getChildrenCount();
                if (itemAmount < 16) {
                    mFirebaseInventoryUpdate.child(weapon.getName()).setValue(weapon);
                    mCurrentCharacter.addToInventory(weapon);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void addItemToInventory(final Item item) {
        final Firebase mFirebaseInventoryUpdate = new Firebase(Constants.FIREBASE_URL_USERS + "/" + mPlayerId + "/character/inventory/");

        mFirebaseInventoryUpdate.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int itemAmount = (int) dataSnapshot.getChildrenCount();
                if (itemAmount < 16) {
                    mFirebaseInventoryUpdate.child(item.getName()).setValue(item);
                    mCurrentCharacter.addToInventory(item);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
}
