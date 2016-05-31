package com.eyecuelab.survivalists.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eyecuelab.survivalists.R;
import com.eyecuelab.survivalists.models.SafeHouse;

import org.parceler.Parcels;

import butterknife.ButterKnife;

/**
 * Created by eyecuelab on 5/31/16.
 */
public class MerchantDialogFragment extends android.support.v4.app.DialogFragment implements View.OnClickListener{

    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;

    //empty constructor required for dialog fragments
    public MerchantDialogFragment() {};

    public static android.support.v4.app.DialogFragment newInstance(int number, SafeHouse safehouse) {
        MerchantDialogFragment frag = new MerchantDialogFragment();
        Bundle args = new Bundle();
        args.putInt("number", number);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mEditor = mSharedPreferences.edit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_merchant_dialog, container, false);
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);


        return view;
    }

    @Override
    public void onClick(View v) {

    }
}
