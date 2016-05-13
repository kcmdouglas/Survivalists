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
import android.widget.TextView;

import com.eyecuelab.survivalists.R;

import org.w3c.dom.Text;

import butterknife.Bind;

/**
 * Created by eyecuelab on 5/13/16.
 */
public class EventDialogFragment extends android.support.v4.app.DialogFragment {
    @Bind(R.id.affirmativeButton)
    Button affirmativeButton;
    @Bind(R.id.negativeButton) Button negativeButton;
    @Bind(R.id.dialogDescription)
    TextView dialogDescription;

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
        Resources res = getResources();
        String[] DialogOptions = res.getStringArray(R.array.dialogArray);
        int dialogChooser = (int) (Math.random() * (DialogOptions.length) -1);

        //dialogDescription.setText(DialogOptions[1]);

        return view;
    }


}
