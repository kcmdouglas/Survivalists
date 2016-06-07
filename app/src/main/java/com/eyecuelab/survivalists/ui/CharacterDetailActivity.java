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
        mCharacters = Parcels.unwrap(getIntent().getParcelableExtra("characters"));

        if (mCharacters.size() > 0) {
            setPager();
        }
    }

    public void setPager() {
        adapterViewPager = new CharacterPagerAdapter(getSupportFragmentManager(), mCharacters);
        mViewPager.setAdapter(adapterViewPager);
        mViewPager.setCurrentItem(0);
        adapterViewPager.notifyDataSetChanged();
    }
}
