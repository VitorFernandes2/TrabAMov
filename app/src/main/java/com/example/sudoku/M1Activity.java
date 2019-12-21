package com.example.sudoku;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class M1Activity extends AppCompatActivity {

    private CountDownTimer countDownTimer;
    private long timeInMs;
    private SudokuView sudokuView;
    private Button bt1;
    private Button bt2;
    private Button bt3;
    private Button bt4;
    private Button bt5;
    private Button bt6;
    private Button bt7;
    private Button bt8;
    private Button bt9;
    private TextView tvTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_m1);

        final FrameLayout flSudoku = findViewById(R.id.flSudokuM1);
        sudokuView = new SudokuView(this);
        flSudoku.addView(sudokuView);

        tvTimer = findViewById(R.id.tvTimer);

        timeInMs = 6000;

        createButtons();
        startTimer();

    }

    private void startTimer() {

        countDownTimer = new CountDownTimer(timeInMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeInMs = millisUntilFinished;
                updateTimer();
            }

            @Override
            public void onFinish() {
                //Quando o tempo acabar
                tvTimer.setText(getString(R.string.finished) + "!");
            }

        }.start();

    }

    private void updateTimer() {

        int minutes = (int) timeInMs / 60000;
        int seconds = (int) timeInMs % 60000 / 1000;

        String strTimeLeft = "" + minutes + ":";
        if (seconds < 10) strTimeLeft += "0";
        strTimeLeft += "" + seconds;

        tvTimer.setText(strTimeLeft);

    }

    private void createButtons(){

        bt1 = findViewById(R.id.button);
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuView.setValue(1);
            }
        });

        bt2 = findViewById(R.id.button2);
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuView.setValue(2);
            }
        });

        bt3 = findViewById(R.id.button3);
        bt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuView.setValue(3);
            }
        });

        bt4 = findViewById(R.id.button4);
        bt4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuView.setValue(4);
            }
        });

        bt5 = findViewById(R.id.button5);
        bt5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuView.setValue(5);
            }
        });

        bt6 = findViewById(R.id.button6);
        bt6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuView.setValue(6);
            }
        });

        bt7 = findViewById(R.id.button7);
        bt7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuView.setValue(7);
            }
        });

        bt8 = findViewById(R.id.button8);
        bt8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuView.setValue(8);
            }
        });

        bt9 = findViewById(R.id.button9);
        bt9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuView.setValue(9);
            }
        });

    }

}
