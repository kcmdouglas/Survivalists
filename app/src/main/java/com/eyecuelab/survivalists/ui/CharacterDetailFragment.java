package com.eyecuelab.survivalists.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.eyecuelab.survivalists.Constants;
import com.eyecuelab.survivalists.R;
import com.eyecuelab.survivalists.models.Character;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CharacterDetailFragment extends Fragment implements View.OnClickListener {
    @Bind(R.id.nameTextView) TextView nameTextView;
    @Bind(R.id.ageTextView) TextView ageTextView;
    @Bind(R.id.healthTextView) TextView healthTextView;
    @Bind(R.id.teamDailyGoalTextView) TextView teamDailyGoalTextView;
    @Bind(R.id.teamHealthProgressBar) ProgressBar teamHealthProgressBar;
    @Bind(R.id.teamStepProgressBar) ProgressBar teamStepProgressBar;
    @Bind(R.id.teamEnergyProgressBar) ProgressBar teamEnergyProgressBar;
    @Bind(R.id.teamEnergyTextView) TextView teamEnergyTextView;
    @Bind(R.id.playerStatusTitle) TextView playerStatusTitle;
    @Bind(R.id.backpackContentTitle) TextView backpackContentTitle;
    @Bind(R.id.upperTabButton) Button upperTabButton;
    @Bind(R.id.tabLargeButton) Button tabLargeButton;


    private Character mCharacter;
    private String mPlayerID;
    private int teammateSteps;
    private int teammateGoal;
    private int teammateHealth;
    private int dailySteps;
    private int dailyGoal;

    public CharacterDetailFragment() {
        // Required empty public constructor
    }

    public static CharacterDetailFragment newInstance(Character character) {
        CharacterDetailFragment fragment = new CharacterDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable("character", Parcels.wrap(character));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCharacter = Parcels.unwrap(getArguments().getParcelable("character"));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_character_detail, container, false);
        ButterKnife.bind(this, view);
        upperTabButton.setOnClickListener(this);

        nameTextView.setText("Name: " + mCharacter.getName());
        ageTextView.setText("Age: " + Integer.toString(mCharacter.getAge()));
        healthTextView.setText(mCharacter.getHealth() + "HP");

        Typeface titleTypeface = Typeface.createFromAsset(getContext().getAssets(), "WindowMarkers.ttf");
        Typeface bodyTypeface = Typeface.createFromAsset(getContext().getAssets(), "BebasNeue.ttf");

        playerStatusTitle.setTypeface(titleTypeface);
        backpackContentTitle.setTypeface(titleTypeface);
        nameTextView.setTypeface(bodyTypeface);
        ageTextView.setTypeface(bodyTypeface);
        healthTextView.setTypeface(bodyTypeface);
        upperTabButton.setTypeface(bodyTypeface);
        tabLargeButton.setTypeface(bodyTypeface);

        setupListeners();
        return view;
    }

    private void setupListeners() {
        mPlayerID = mCharacter.getPlayerId();

        Firebase teammateFirebase = new Firebase(Constants.FIREBASE_URL_USERS + "/" + mPlayerID);

        teammateFirebase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCharacter = new Character(dataSnapshot.child("character").getValue(Character.class));
                mCharacter.setPlayerId(mPlayerID);
                dailyGoal = Integer.parseInt(dataSnapshot.child("dailyGoal").getValue().toString());
                dailySteps = Integer.parseInt(dataSnapshot.child("dailySteps").getValue().toString());


                healthTextView.setText("Health: " + Integer.toString(mCharacter.getHealth()));
                updateUi();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
    public void updateUi() {
        teamStepProgressBar.setProgress(dailySteps);
        teamStepProgressBar.setMax(dailyGoal);
        teamDailyGoalTextView.setText(dailySteps + "/" + dailyGoal);
        teamHealthProgressBar.setProgress(mCharacter.getHealth());
        healthTextView.setText(mCharacter.getHealth() + "HP");
        teamEnergyProgressBar.setProgress(mCharacter.getFullnessLevel());
        teamEnergyTextView.setText(mCharacter.getFullnessLevel() + "%");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.upperTabButton:
                Intent backIntent = new Intent(getContext(), MainActivity.class);
                startActivity(backIntent);
                break;
        }
    }
}
