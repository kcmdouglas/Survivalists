package com.eyecuelab.survivalists.ui;

import android.content.Intent;
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

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by eyecuelab on 6/10/16.
 */
public class JoinGameErrorDialog extends DialogFragment implements View.OnClickListener {

    @Bind(R.id.joinGameErrorTitle)
    TextView joinGameErrorTitle;
    @Bind(R.id.joinGameErrorCloseButton)
    Button joinGameErrorCloseButton;
    @Bind(R.id.joinGameErrorDescription) TextView joinGameErrorDescription;

    public JoinGameErrorDialog(){}

    public static DialogFragment newInstance() {
        JoinGameErrorDialog frag = new JoinGameErrorDialog();
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomFragment);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_join_game_error, container, false);
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        joinGameErrorTitle.setText("Whoops!");
        joinGameErrorDescription.setText("You have to be part of a campaign to use this part of the app! You could create a new campaign, or join a friend's!");

        joinGameErrorCloseButton.setOnClickListener(this);
        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.joinGameErrorCloseButton:
                dismiss();
                Intent intent = new Intent(getActivity(), TitleActivity.class);
                startActivity(intent);
                break;
        }

    }

}
