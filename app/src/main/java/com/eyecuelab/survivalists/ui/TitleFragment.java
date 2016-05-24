package com.eyecuelab.survivalists.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eyecuelab.survivalists.R;

public class TitleFragment extends Fragment {

    public TitleFragment() {}

    public static TitleFragment newInstance() {
        TitleFragment titleFragment = new TitleFragment();
        Bundle args = new Bundle();
        titleFragment.setArguments(args);
        return titleFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_title, container, false);
    }

}
