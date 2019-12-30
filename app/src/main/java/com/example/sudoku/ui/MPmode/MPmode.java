package com.example.sudoku.ui.MPmode;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.sudoku.M1Activity;
import com.example.sudoku.M3Activity;
import com.example.sudoku.R;

public class MPmode extends Fragment {

    private MpmodeViewModel mViewModel;
    private Button btnHost;
    private Button btnJoin;


    public static MPmode newInstance() {
        return new MPmode();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.mpmode_fragment, container, false);

        btnHost = (Button) root.findViewById(R.id.btnhost);
        btnHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Show dialogue
                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.cDificulties);

                // add a list
                String[] dificulties = {getString(R.string.hard), getString(R.string.medium), getString(R.string.easy)};
                builder.setItems(dificulties, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent;
                        int value = 3;
                        switch (which) {
                            case 0: // Hard
                                value = 7;
                                break;
                            case 1: // Medium
                                value = 5;
                                break;
                            case 2: // Easy
                                value = 3;
                                break;
                        }

                        intent = new Intent(getActivity(), M3Activity.class);
                        intent.putExtra("difficulty", value);
                        startActivity(intent);

                    }
                });

                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        btnHost = (Button) root.findViewById(R.id.btnjoin);
        btnHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int value=3;
                Intent intent;

                intent = new Intent(getActivity(), M3Activity.class);
                intent.putExtra("difficulty", value);
                startActivity(intent);

            }
        });


        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MpmodeViewModel.class);
        // TODO: Use the ViewModel
    }

}
