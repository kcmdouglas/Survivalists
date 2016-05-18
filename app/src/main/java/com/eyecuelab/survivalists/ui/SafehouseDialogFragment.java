package com.eyecuelab.survivalists.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eyecuelab.survivalists.R;
import com.eyecuelab.survivalists.models.SafeHouse;

import org.parceler.Parcels;
import org.w3c.dom.Text;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by eyecuelab on 5/13/16.
 */
public class SafehouseDialogFragment extends android.support.v4.app.DialogFragment implements View.OnClickListener{

    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;
    SafeHouse mSafeHouse;
    @Bind(R.id.safehouseTitle) TextView safehouseTitle;
    @Bind(R.id.safehouseDescription) TextView safehouseDescription;
    @Bind(R.id.safehouseCloseButton) Button safehouseCloseButton;

    //Empty constructor required for DialogFragments
    public SafehouseDialogFragment(){}

    public static android.support.v4.app.DialogFragment newInstance(int number, SafeHouse safehouse) {
        SafehouseDialogFragment frag = new SafehouseDialogFragment();
        Bundle args = new Bundle();
        args.putInt("number", number);
        args.putParcelable("safehouse", Parcels.wrap(safehouse));
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSafeHouse = Parcels.unwrap(getArguments().getParcelable("safehouse"));
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mEditor = mSharedPreferences.edit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_safehouse_dialog, container, false);
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        safehouseTitle.setText(mSafeHouse.getHouseName());
        safehouseDescription.setText(mSafeHouse.getDescription());

        safehouseCloseButton.setOnClickListener(this);

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
