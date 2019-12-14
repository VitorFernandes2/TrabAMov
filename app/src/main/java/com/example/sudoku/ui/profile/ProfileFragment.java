package com.example.sudoku.ui.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import com.example.sudoku.CameraActivity;
import com.example.sudoku.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private Button btnTakePicture;
    private CircleImageView circleImageView;
    private Button btnSaveChanges;
    private EditText editText;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        btnTakePicture = (Button) root.findViewById(R.id.btn_takepicture);
        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CameraActivity.class);
                startActivity(intent);
            }
        });

        editText = (EditText) root.findViewById(R.id.inputNome);

        circleImageView = (CircleImageView) root.findViewById(R.id.profile_image);

        Bitmap img = insertImage();

        if (img != null){
            circleImageView.setImageBitmap(img);
        }

        btnSaveChanges = (Button) root.findViewById(R.id.btnSaveChanges);
        btnSaveChanges.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

            }
        });

        EditText t = (EditText) root.findViewById(R.id.inputNome);
        t.setGravity(Gravity.CENTER);

        return root;
    }

    private Bitmap insertImage(){

        Bitmap bm = null;
        try {
            File file = new File("/storage/emulated/0/perfil.jpg");
            FileInputStream is = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e) {
            Log.e("Sudoku", "Error getting bitmap", e);
        }
        return bm;

    }

}