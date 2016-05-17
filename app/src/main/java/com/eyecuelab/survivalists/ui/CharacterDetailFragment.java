package com.eyecuelab.survivalists.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eyecuelab.survivalists.R;
import com.eyecuelab.survivalists.models.Character;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CharacterDetailFragment extends Fragment {
    @Bind(R.id.nameTextView) TextView nameTextView;
    @Bind(R.id.ageTExtView) TextView ageTextView;
    @Bind(R.id.healthTextView) TextView healthTextView;

    private Character mCharacter;

    public CharacterDetailFragment() {
        // Required empty public constructor
    }

    public static CharacterDetailFragment newInstance(Character character) {
        CharacterDetailFragment fragment = new CharacterDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable("character", Parcels.wrap(character));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCharacter = Parcels.unwrap(getArguments().getParcelable("character"));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_character_detail, container, false);
        ButterKnife.bind(this, view);
        nameTextView.setText("Name: " + mCharacter.getName());
        ageTextView.setText("Age: " + Integer.toString(mCharacter.getAge()));
        healthTextView.setText("Health: " + Integer.toString(mCharacter.getHealth()));

        return view;
    }

}
