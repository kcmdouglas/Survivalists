package com.eyecuelab.survivalists.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eyecuelab.survivalists.R;
import com.eyecuelab.survivalists.models.Item;
import com.eyecuelab.survivalists.models.Weapon;

import java.util.ArrayList;

/**
 * Created by eyecue on 5/25/16.
 */
public class InventoryAdapter extends BaseAdapter {
    private static final String TAG = "InventoryAdapter";

    private Context mContext;
    private ArrayList<Item> mItems;
    private ArrayList<Weapon> mWeapons;
    private int mInventoryItemLayout;

    public InventoryAdapter(Context context, ArrayList<Item> items, ArrayList<Weapon> weapons, int inventoryItemLayout) {
        mContext = context;
        mItems = items;
        mWeapons = weapons;
        mInventoryItemLayout = inventoryItemLayout;
    }

    @Override
    public int getCount() {
        return mItems.size() + mWeapons.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RecordHolder holder = new RecordHolder();

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(mInventoryItemLayout, parent, false);
            holder.txtTitle = (TextView) convertView.findViewById(R.id.item_text);
            holder.imageItem = (ImageView) convertView.findViewById(R.id.item_image);
            convertView.setTag(holder);
        } else {
            holder = (RecordHolder) convertView.getTag();
        }

        try {
            Item item = mItems.get(position);
            holder.txtTitle.setText(item.getName() + "");
            holder.imageItem.setImageResource(item.getImageId());
        } catch (IndexOutOfBoundsException outOfBounds) {
            Log.v(TAG, outOfBounds.getMessage());
        }

        return convertView;
    }

    static class RecordHolder {
        TextView txtTitle;
        ImageView imageItem;
    }
}
