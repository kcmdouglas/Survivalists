package com.eyecuelab.survivalists.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eyecuelab.survivalists.Constants;
import com.eyecuelab.survivalists.R;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.w3c.dom.Text;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by eyecuelab on 5/13/16.
 */
public class EventDialogFragment extends android.support.v4.app.DialogFragment implements View.OnClickListener {
    private TextView dialogDescription;
    private TextView dialogConsequence;
    private TextView dialogTitle;
    private Button affirmativeButton;
    private Button negativeButton;
    private Button closeButton;
    private int dialogChooser;
    private String[] dialogOptions;
    private Resources res;
    private Firebase mFirebaseEventRef;
    private String description;
    private String title;
    private String outcomeA;
    private String outcomeB;
    private int penaltyHP;
    private int stepsRequired;
    private boolean getItemOnFlee;
    private boolean getItemOnInspect;


    //Empty constructor required for DialogFragments
    public EventDialogFragment(){}

    public static android.support.v4.app.DialogFragment newInstance(int number) {
        EventDialogFragment frag = new EventDialogFragment();

        Bundle args = new Bundle();
        args.putInt("number", number);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_dialog, container, false);
        super.onViewCreated(view, savedInstanceState);
        int eventNumber = (int) Math.floor(Math.random() * 10 + 1);
        int attackOrInspect = (int) (Math.random() +0.5);

        affirmativeButton = (Button) view.findViewById(R.id.affirmativeButton);
        affirmativeButton.setOnClickListener(this);
        negativeButton = (Button) view.findViewById(R.id.negativeButton);
        negativeButton.setOnClickListener(this);
        closeButton = (Button) view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(this);
        closeButton.setVisibility(View.GONE);
        dialogConsequence.setVisibility(View.GONE);

        if(attackOrInspect == 0) {
            mFirebaseEventRef = new Firebase(Constants.FIREBASE_URL_EVENTS + "/attack/");
            mFirebaseEventRef.child(Integer.toString(eventNumber)).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            description = dataSnapshot.child("description").getValue().toString();
                            outcomeA = dataSnapshot.child("description").getValue().toString();                        String description = dataSnapshot.child("description").getValue().toString();
                            outcomeB = dataSnapshot.child("description").getValue().toString();
                            title = dataSnapshot.child("description").getValue().toString();
                            penaltyHP = (int) dataSnapshot.child("penalty_hp").getValue();
                            stepsRequired = (int) dataSnapshot.child("steps_required").getValue();
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    }
            );
            affirmativeButton.setText("Attack");
            negativeButton.setText("Run");
        } else {
            mFirebaseEventRef = new Firebase(Constants.FIREBASE_URL_EVENTS + "/inspect/");
            mFirebaseEventRef.child(Integer.toString(eventNumber)).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            description = dataSnapshot.child("description").getValue().toString();
                            outcomeA = dataSnapshot.child("description").getValue().toString();                        String description = dataSnapshot.child("description").getValue().toString();
                            outcomeB = dataSnapshot.child("description").getValue().toString();
                            title = dataSnapshot.child("description").getValue().toString();
                            penaltyHP = (int) dataSnapshot.child("penalty_hp").getValue();
                            stepsRequired = (int) dataSnapshot.child("steps_required").getValue();
                            getItemOnFlee = (boolean) dataSnapshot.child("get_item_on_flee").getValue();
                            getItemOnInspect = (boolean) dataSnapshot.child("get_item_on_flee").getValue();
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    }
            );
            affirmativeButton.setText("Inspect");
            negativeButton.setText("Ignore");
        }

        dialogDescription = (TextView) view.findViewById(R.id.dialogDescription);
        dialogConsequence = (TextView) view.findViewById(R.id.dialogConsequence);
        dialogTitle = (TextView) view.findViewById(R.id.dialogTitle);
        res = getResources();


        dialogTitle.setText(title);
        dialogDescription.setText(description);


        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.affirmativeButton:
                affirmativeClick(dialogChooser);
                break;
            case R.id.negativeButton:
                negativeClick(dialogChooser);
                break;
            case R.id.closeButton:
                dismiss();
                break;
        }
    }

    private void affirmativeClick(int dialogNumber) {
        configureResultLayout();
        String[] resultA = res.getStringArray(R.array.resultA);
        String[] consequenceA = res.getStringArray(R.array.resultAConsequence);

        dialogDescription.setText(resultA[dialogNumber]);
        dialogConsequence.setText(consequenceA[dialogNumber]);
    }

    private void negativeClick(int dialogNumber) {
        configureResultLayout();
        String[] resultB = res.getStringArray(R.array.resultB);
        String[] consequenceB = res.getStringArray(R.array.resultBConsequence);

        dialogDescription.setText(resultB[dialogNumber]);
        dialogConsequence.setText(consequenceB[dialogNumber]);
    }

    private void configureResultLayout() {
        dialogTitle.setVisibility(View.GONE);
        affirmativeButton.setVisibility(View.GONE);
        negativeButton.setVisibility(View.GONE);
        closeButton.setVisibility(View.VISIBLE);
        dialogConsequence.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)dialogDescription.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        dialogDescription.setLayoutParams(params);
    }
}
