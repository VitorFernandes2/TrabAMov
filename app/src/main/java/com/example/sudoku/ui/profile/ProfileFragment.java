package com.example.sudoku.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import com.example.sudoku.CameraActivity;
import com.example.sudoku.MainActivity;
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

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private Button btnTakePicture;
    private CircleImageView circleImageView;
    private Button btnSaveChanges;
    private EditText editText;
    private Bitmap img;
    static SharedPreferences sharedPref;

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

        img = insertImage();

        if (img != null){
            circleImageView.setImageBitmap(img);
        }

        sharedPref = getActivity().getSharedPreferences("user_id", MODE_PRIVATE);
        String userId = sharedPref.getString("user_id", "Username");
        if (userId == null){

            sharedPref.edit().putString("user_id", "Username").commit();
        }

        editText.setText(userId);

        btnSaveChanges = (Button) root.findViewById(R.id.btnSaveChanges);
        btnSaveChanges.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if (!editText.getText().toString().isEmpty()){
                    sharedPref.edit().putString("user_id", editText.getText().toString()).commit();
                }

            }
        });

        EditText t = (EditText) root.findViewById(R.id.inputNome);
        t.setGravity(Gravity.CENTER);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        img = insertImage();

        if (img != null){
            circleImageView.setImageBitmap(img);
        }
    }

    private Bitmap insertImage(){

        System.gc();
        File sd = Environment.getExternalStorageDirectory();
        File image = new File(sd, "perfil.jpg");
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
        bitmap = Bitmap.createScaledBitmap(bitmap,480,640,true);

        return bitmap;

    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

}