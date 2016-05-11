package com.eyecuelab.survivalists.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eyecuelab.survivalists.R;

public class InvitationFragment extends Fragment {


    public InvitationFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invitation, container, false);


    }

}
