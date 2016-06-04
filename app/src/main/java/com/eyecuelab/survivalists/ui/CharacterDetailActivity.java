package com.eyecuelab.survivalists.ui;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.eyecuelab.survivalists.Constants;
import com.eyecuelab.survivalists.R;
import com.eyecuelab.survivalists.adapters.CharacterPagerAdapter;
import com.eyecuelab.survivalists.models.Character;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CharacterDetailActivity extends AppCompatActivity {
    @Bind(R.id.viewPager) ViewPager mViewPager;
    private CharacterPagerAdapter adapterViewPager;
    ArrayList<String> mPlayerIDs = new ArrayList<>();
    ArrayList<Character> mCharacters = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_detail);
        ButterKnife.bind(this);
        mPlayerIDs = Parcels.unwrap(getIntent().getParcelableExtra("playerIDs"));

        for(String playerId: mPlayerIDs) {
            Firebase teamCharactersRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + playerId + "/character/");
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
                    mCharacters.add(character);

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });

        }

        if (mCharacters.size() > 0) {
            setPager();
        } else {
            try {
                Thread.sleep(1000);
                setPager();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setPager() {
        adapterViewPager = new CharacterPagerAdapter(getSupportFragmentManager(), mCharacters);
        mViewPager.setAdapter(adapterViewPager);
        mViewPager.setCurrentItem(0);
        adapterViewPager.notifyDataSetChanged();
    }
}
