package com.eyecuelab.survivalists.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eyecuelab.survivalists.R;
import com.eyecuelab.survivalists.models.SafeHouse;

import org.parceler.Parcels;

import butterknife.ButterKnife;

/**
 * Created by eyecuelab on 6/9/16.
 */
public class CampaignEndFragment extends DialogFragment implements View.OnClickListener{
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;

    public CampaignEndFragment(){}

    public static DialogFragment newInstance() {
        CampaignEndFragment frag = new CampaignEndFragment();
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomFragment);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mEditor = mSharedPreferences.edit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_safehouse_dialog, container, false);
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);


        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.safehouseCloseButton:
                dismiss();
                break;
        }


    }
}
