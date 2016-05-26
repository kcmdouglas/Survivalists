package com.eyecuelab.survivalists.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.eyecuelab.survivalists.R;
import com.eyecuelab.survivalists.adapters.InventoryAdapter;
import com.eyecuelab.survivalists.models.Item;
import com.eyecuelab.survivalists.models.Weapon;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NotebookActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "NotebookActivity";
    @Bind(R.id.tabCampaignButton) Button campaignButton;
    @Bind(R.id.mapTabButton) Button mapButton;
    @Bind(R.id.rightInteractionBUtton) Button rightInteractionButton;
    @Bind(R.id.leftInteractionButton) Button leftInteractionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //set content view AFTER ABOVE sequence (to avoid crash)
        setContentView(R.layout.activity_notebook);

        //TODO: Remove these fake objects for testing:
        ArrayList<Weapon> weapons = new ArrayList<>();
        weapons.add(new Weapon("Axe!", "This is an axe!", 5));
        ArrayList<Item> items = new ArrayList<>();
        items.add(new Item("Axe!", "This is an axe!", 5, true, R.drawable.axe_inventory));
        items.add(new Item("Health Pack", "This is a health pack!", 5, true, R.drawable.firstaid_inventory));
        items.add(new Item("Flare", "This is a flare!", 5, true, R.drawable.flare_inventory));
        items.add(new Item("Steak", "This is a steak!", 5, true, R.drawable.steak_inventory));
        items.add(new Item("Axe!", "This is an axe!", 5, true, R.drawable.axe_inventory));
        items.add(new Item("Health Pack", "This is a health pack!", 5, true, R.drawable.firstaid_inventory));
        items.add(new Item("Flare", "This is a flare!", 5, true, R.drawable.flare_inventory));
        items.add(new Item("Steak", "This is a steak!", 5, true, R.drawable.steak_inventory));
        items.add(new Item("Axe!", "This is an axe!", 5, true, R.drawable.axe_inventory));
        items.add(new Item("Health Pack", "This is a health pack!", 5, true, R.drawable.firstaid_inventory));
        items.add(new Item("Flare", "This is a flare!", 5, true, R.drawable.flare_inventory));
        items.add(new Item("Steak", "This is a steak!", 5, true, R.drawable.steak_inventory));
        items.add(new Item("Axe!", "This is an axe!", 5, true, R.drawable.axe_inventory));
        items.add(new Item("Health Pack", "This is a health pack!", 5, true, R.drawable.firstaid_inventory));
        items.add(new Item("Flare", "This is a flare!", 5, true, R.drawable.flare_inventory));
        items.add(new Item("Steak", "This is a steak!", 5, true, R.drawable.steak_inventory));

        try {
            GridView inventoryGridView = (GridView) findViewById(R.id.backpackGridView);
            //TODO: Figure out why android studio thinks this catch is required (and isn't happy)
            inventoryGridView.setAdapter(new InventoryAdapter(NotebookActivity.this, items, weapons, R.layout.row_grid));
            inventoryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(NotebookActivity.this, "" + position, Toast.LENGTH_SHORT).show();
                }
            });
            //This stops the grid from being scrolled.
            inventoryGridView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_MOVE) {
                        return true;
                    }
                    return false;
                }
            });

        } catch (NullPointerException nullPointer) {
            Log.e(TAG, nullPointer.getMessage());
        }

        ButterKnife.bind(this);
        campaignButton.setOnClickListener(this);
        mapButton.setOnClickListener(this);
        leftInteractionButton.setOnClickListener(this);
        rightInteractionButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tabCampaignButton:
                Intent intent = new Intent(NotebookActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.mapTabButton:
                Toast.makeText(NotebookActivity.this, "Map Button!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.rightInteractionBUtton:
                Toast.makeText(this, "Are you encouraged?", Toast.LENGTH_SHORT).show();
                break;
            case R.id.leftInteractionButton:
                Toast.makeText(this, "Item given!", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
