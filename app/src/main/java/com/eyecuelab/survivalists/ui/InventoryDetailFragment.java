package com.eyecuelab.survivalists.ui;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.eyecuelab.survivalists.Constants;
import com.eyecuelab.survivalists.R;
import com.eyecuelab.survivalists.models.Character;
import com.eyecuelab.survivalists.models.InventoryEntity;
import com.eyecuelab.survivalists.models.Item;
import com.eyecuelab.survivalists.models.Weapon;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class InventoryDetailFragment extends DialogFragment implements View.OnClickListener {
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;
    InventoryEntity mItem;
    Character mCharacter;
    String mUserId;

    @Bind(R.id.dialogTitle) TextView dialogTitle;
    @Bind(R.id.closeButton) Button closeButton;
    @Bind(R.id.dialogDescription) TextView dialogDescription;
    @Bind(R.id.negativeButton) Button negativeButton;
    @Bind(R.id.affirmativeButton) Button affirmativeButton;

    public InventoryDetailFragment() {}

    public static DialogFragment newInstance(InventoryEntity item, Character character, String userId) {
        InventoryDetailFragment fragment = new InventoryDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable("item", Parcels.wrap(item));
        args.putParcelable("character", Parcels.wrap(character));
        args.putParcelable("userId", Parcels.wrap(userId));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomFragment);

        mCharacter = Parcels.unwrap(getArguments().getParcelable("character"));
        mUserId = Parcels.unwrap(getArguments().getParcelable("userId"));
        mItem = Parcels.unwrap(getArguments().getParcelable("item"));
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mEditor = mSharedPreferences.edit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_event_dialog, container, false);
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);
        dialogTitle.setText(mItem.getName());
        dialogDescription.setText(mItem.getDescription());

        negativeButton.setVisibility(View.GONE);

        if (mItem.getClass().isInstance(Item.class)) {
            affirmativeButton.setText("Use Item");
        } else {
            affirmativeButton.setVisibility(View.GONE);
        }

        closeButton.setOnClickListener(this);
        negativeButton.setOnClickListener(this);
        affirmativeButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.closeButton:
                dismiss();
                break;
            case R.id.affirmativeButton:
                useItem(v);
        }

    }

    public void useItem(View view) {

        if (mItem.getClass().isInstance(Item.class)) {
            final Item currentItem = (Item) mItem;
            currentItem.useItem(mCharacter);
            final View v = view;

            Firebase userFirebaseRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + mUserId + "/" + "items");
            userFirebaseRef.child(currentItem.getPushId()).removeValue(new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    Toast.makeText(v.getContext(), currentItem.getName() + " Used", Toast.LENGTH_LONG).show();
                    dismiss();
                }
            });
        }
    }
}
