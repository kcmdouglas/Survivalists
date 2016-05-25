package com.eyecuelab.survivalists.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.eyecuelab.survivalists.models.Character;
import com.eyecuelab.survivalists.ui.CharacterDetailFragment;

import java.util.ArrayList;
import java.util.List;

public class CharacterPagerAdapter extends FragmentPagerAdapter {
    private List<Character> mCharacters;

    public CharacterPagerAdapter(FragmentManager fm, ArrayList<Character> characters) {
        super(fm);
        mCharacters = characters;
    }



    @Override
    public Fragment getItem(int position) {
        return CharacterDetailFragment.newInstance(mCharacters.get(position));
    }

    @Override
    public int getCount() {
        return mCharacters.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mCharacters.get(position).getName();
    }
}
