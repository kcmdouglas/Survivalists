package com.eyecuelab.survivalists.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eyecuelab.survivalists.Constants;
import com.eyecuelab.survivalists.R;
import com.eyecuelab.survivalists.models.Character;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CharacterDetailFragment extends Fragment {
    @Bind(R.id.nameTextView) TextView nameTextView;
    @Bind(R.id.ageTExtView) TextView ageTextView;
    @Bind(R.id.healthTextView) TextView healthTextView;

    private Character mCharacter;
    private String mPlayerID;

    public CharacterDetailFragment() {
        // Required empty public constructor
    }

    public static CharacterDetailFragment newInstance(String playerID) {
        CharacterDetailFragment fragment = new CharacterDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable("playerID", Parcels.wrap(playerID));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlayerID = Parcels.unwrap(getArguments().getParcelable("playerID"));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_character_detail, container, false);
        ButterKnife.bind(this, view);

        Firebase teamCharactersRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + mPlayerID + "/character/");
        teamCharactersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                long ageLong = (long) dataSnapshot.child("age").getValue();
                int age = (int) ageLong;
                String description = dataSnapshot.child("description").getValue().toString();
                long characterIdLong = (long) dataSnapshot.child("characterId").getValue();
                int characterId = (int) characterIdLong;
                long healthLong = (long) dataSnapshot.child("health").getValue();
                int health = (int) healthLong;
                long fullnessLevelLong = (long) dataSnapshot.child("fullnessLevel").getValue();
                int fullnessLevel = (int) fullnessLevelLong;
                String characterUrl = dataSnapshot.child("characterPictureUrl").getValue().toString();
                Character character = new Character(name, description, age, health, fullnessLevel, characterUrl, characterId);
                mCharacter = new Character(character);

                nameTextView.setText("Name: " + mCharacter.getName());
                ageTextView.setText("Age: " + Integer.toString(mCharacter.getAge()));
                healthTextView.setText("Health: " + Integer.toString(mCharacter.getHealth()));
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        return view;
    }

}
