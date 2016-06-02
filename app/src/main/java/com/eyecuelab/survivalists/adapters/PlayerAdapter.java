package com.eyecuelab.survivalists.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eyecuelab.survivalists.R;
import com.eyecuelab.survivalists.models.Item;
import com.eyecuelab.survivalists.models.User;
import com.eyecuelab.survivalists.models.Weapon;
import com.google.android.gms.common.images.ImageManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by eyecue on 5/31/16.
 */
public class PlayerAdapter extends BaseAdapter {
    private static final String TAG = "PlayerAdapter";

    private Context mContext;
    ArrayList<User> mUsers;
    private int mPlayerListLayout;

    public PlayerAdapter(Context context, ArrayList<User> users, int playerListLayout) {
        mContext = context;
        mUsers = users;
        mPlayerListLayout = playerListLayout;
    }

    @Override
    public int getCount() {
        return mUsers.size();
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
            convertView = inflater.inflate(mPlayerListLayout, parent, false);
            holder.txtTitle = (TextView) convertView.findViewById(R.id.playerNameTextView);
            holder.imageItem = (ImageView) convertView.findViewById(R.id.playerAvatarImage);
            convertView.setTag(holder);
        } else {
            holder = (RecordHolder) convertView.getTag();
        }

        try {
            User user = mUsers.get(position);
            holder.txtTitle.setText(user.getDisplayName());
            Uri imageUri = user.getImageUri();
            ImageManager imageManager = ImageManager.create(mContext);
            imageManager.loadImage(holder.imageItem, imageUri);

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
