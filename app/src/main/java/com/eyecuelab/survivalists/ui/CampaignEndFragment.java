package com.eyecuelab.survivalists.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.eyecuelab.survivalists.R;
import com.eyecuelab.survivalists.models.SafeHouse;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by eyecuelab on 6/9/16.
 */
public class CampaignEndFragment extends DialogFragment implements View.OnClickListener{
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;
    int mMatchDuration;
    int mNextSafehouseId;

    @Bind(R.id.campaignEndTitle) TextView campaignEndTitle;
    @Bind(R.id.campaignEndCloseButton) Button campaignEndCloseButton;
    @Bind(R.id.campaignEndDescription) TextView campaignEndDescription;

    public CampaignEndFragment(){}

    public static DialogFragment newInstance(int matchDuration, int nextSafehouseId) {
        CampaignEndFragment frag = new CampaignEndFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("matchDuration", matchDuration);
        bundle.putInt("nextSafehouseId", nextSafehouseId);
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomFragment);
        Bundle bundle = getArguments();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mEditor = mSharedPreferences.edit();
        mMatchDuration = bundle.getInt("matchDuration");
        mNextSafehouseId = bundle.getInt("nextSafehouseId");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_campaign_end, container, false);
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        campaignEndTitle.setText("In the end...");

        if(mMatchDuration == mNextSafehouseId) {
            campaignEndDescription.setText("You've done it! With enduring teamwork, you've all survived! Time for your next excursion.");
        } else if(mMatchDuration == (mNextSafehouseId + 1)) {
            campaignEndDescription.setText("You were so close! Unfortunately, you were one safehouse from safety... Maybe next time?");
        } else if (mMatchDuration > (mNextSafehouseId + 1)) {
            campaignEndDescription.setText("There were plenty of stumbles along the way. But hey, at least you gave it a shot. Keep at it!");
        }

        campaignEndCloseButton.setOnClickListener(this);


        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.campaignEndCloseButton:
                dismiss();
                break;
        }


    }
}
