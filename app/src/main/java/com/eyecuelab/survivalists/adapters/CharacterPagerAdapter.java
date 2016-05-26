package com.eyecuelab.survivalists.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.eyecuelab.survivalists.models.Character;
import com.eyecuelab.survivalists.ui.CharacterDetailFragment;

import java.util.ArrayList;
import java.util.List;

public class CharacterPagerAdapter extends FragmentPagerAdapter {
    private List<String> mPlayerIDs;

    public CharacterPagerAdapter(FragmentManager fm, ArrayList<String> playerIDs) {
        super(fm);
        mPlayerIDs = playerIDs;
    }



    @Override
    public Fragment getItem(int position) {
        return CharacterDetailFragment.newInstance(mPlayerIDs.get(position));
    }

    @Override
    public int getCount() {
        return mPlayerIDs.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return " ";
    }
}
