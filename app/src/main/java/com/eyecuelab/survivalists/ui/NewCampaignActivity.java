package com.eyecuelab.survivalists.ui;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.eyecuelab.survivalists.R;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NewCampaignActivity extends AppCompatActivity {
    private int mDifficultyLevel;
    private int mCampaignLength;

    private ListView mInvitePlayersListView;
    private Context mContext;

    @Bind(R.id.difficultySeekBar) SeekBar difficultySeekBar;
    @Bind(R.id.campaignLengthSeekBar) SeekBar lengthSeekBar;
    @Bind(R.id.difficultyDescription) TextView difficultyTextView;
    @Bind(R.id.lengthText) TextView lengthTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove notification and navigation bars
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        //set content view AFTER ABOVE sequence (to avoid crash)
        setContentView(R.layout.activity_new_campaign);

        ButterKnife.bind(this);

        mInvitePlayersListView = (ListView) findViewById(R.id.invitePlayersListView);

        String[] players = new String[] {"This Nose Knows", "Hello", "Testing", "This Nose Knows", "Hello", "Testing", "This Nose Knows", "Hello", "Testing",};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.player_list_item, R.id.playerNameTextView, players);

        mInvitePlayersListView.setAdapter(adapter);
        mInvitePlayersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {}
        });

        difficultySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressTotal = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressTotal = progress;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ArrayList<String> descriptions = new ArrayList<String>();
                descriptions.add("Walk in the park");
                descriptions.add("Walk the line");
                descriptions.add("Walk the talk");
                difficultyTextView.setText(descriptions.get(progressTotal));
                mDifficultyLevel = progressTotal;
            }
        });

        lengthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressTotal = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressTotal = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ArrayList<String> lengths = new ArrayList<String>();
                lengths.add("15");
                lengths.add("30");
                lengths.add("45");
                lengthTextView.setText(lengths.get(progressTotal) + " Days");
                mCampaignLength = progressTotal;
            }
        });
    }
}
