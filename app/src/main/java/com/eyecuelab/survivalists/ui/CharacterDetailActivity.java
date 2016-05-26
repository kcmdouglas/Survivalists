package com.eyecuelab.survivalists.ui;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.eyecuelab.survivalists.R;
import com.eyecuelab.survivalists.adapters.CharacterPagerAdapter;
import com.eyecuelab.survivalists.models.Character;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CharacterDetailActivity extends AppCompatActivity {
    @Bind(R.id.viewPager) ViewPager mViewPager;
    private CharacterPagerAdapter adapterViewPager;
    ArrayList<String> mPlayerIDs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_detail);
        ButterKnife.bind(this);
        mPlayerIDs = Parcels.unwrap(getIntent().getParcelableExtra("playerIDs"));
        adapterViewPager = new CharacterPagerAdapter(getSupportFragmentManager(), mPlayerIDs);
        mViewPager.setAdapter(adapterViewPager);
        mViewPager.setCurrentItem(0);
    }
}
