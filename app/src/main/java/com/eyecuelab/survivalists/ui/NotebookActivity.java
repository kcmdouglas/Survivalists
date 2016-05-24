package com.eyecuelab.survivalists.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.eyecuelab.survivalists.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NotebookActivity extends AppCompatActivity implements View.OnClickListener {
    @Bind(R.id.tabCampaignButton) Button campaignButton;
    @Bind(R.id.mapTabButton) Button mapButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//
        //set content view AFTER ABOVE sequence (to avoid crash)
        setContentView(R.layout.activity_notebook);

        ButterKnife.bind(this);
        campaignButton.setOnClickListener(this);
        mapButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tabCampaignButton:
                Toast.makeText(NotebookActivity.this, "Campaign Button!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.mapTabButton:
                Toast.makeText(NotebookActivity.this, "Map Button!", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
