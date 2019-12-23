package com.example.sudoku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.isec.ans.sudokulibrary.Sudoku;

public class M1Activity extends AppCompatActivity {

    private static final int BOARD_SIZE = 9;
    private CountDownTimer countDownTimer;
    private Button btAnotations;
    private long timeInMs;
    private SudokuView sudokuView;
    private Button bt1, bt2, bt3, bt4, bt5, bt6, bt7, bt8, bt9, btHint, btApaga;
    private TextView tvTimer, tvErrors, tvPoints;
    private int[][] board = new int[BOARD_SIZE][BOARD_SIZE];
    private int[][] boardComp = new int[BOARD_SIZE][BOARD_SIZE];
    private int[][][] anotations = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_m1);

        final FrameLayout flSudoku = findViewById(R.id.flSudokuM1);
        createButtons();

        tvErrors = findViewById(R.id.textView6);
        tvPoints = findViewById(R.id.textView5);
        sudokuView = new SudokuView(this, bt1, bt2, bt3, bt4, bt5, bt6, bt7,
                bt8, bt9, tvErrors, tvPoints);
        flSudoku.addView(sudokuView);
        createButtonsListener();


        tvTimer = findViewById(R.id.tvTimer);

        int difficulty = getIntent().getIntExtra("difficulty", 4);

        switch (difficulty){
            case 7:
                timeInMs = 1800000;
                break;
            case 5:
                timeInMs = 900000;
                break;
            default:
                timeInMs = 450000;
                break;
        }

        gerar(difficulty);

        startTimer();

    }

    private void gerar(int level){

        String strJson = Sudoku.generate(level);
        Log.e("Sudoku", "Json: " + strJson);

        try{
            JSONObject json = new JSONObject(strJson);

            if (json.optInt("result",0) == 1){

                JSONArray arrayJson = json.getJSONArray("board");
                int [][] array = convert(arrayJson);
                sudokuView.setBoard(array);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private int[][] convert(JSONArray arrayJson) {

        int[][] array = new int[9][9];

        try{

            for (int r = 0; r < 9; r++) {

                JSONArray jsonRow = arrayJson.getJSONArray(r);
                for (int c = 0; c < 9; c++) {

                    array[r][c] = jsonRow.getInt(c);

                }

            }

        }catch (Exception e){

        }

        return array;

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

                AlertDialog.Builder builder = new AlertDialog.Builder(M1Activity.this);
                builder.setTitle(R.string.finished);
                builder.setMessage(R.string.DefeatMessage)
                        .setPositiveButton(R.string.thanks, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(M1Activity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

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

    private void createButtonsListener(){

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuView.setValue(1);
            }
        });

        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuView.setValue(2);
            }
        });

        bt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuView.setValue(3);
            }
        });

        bt4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuView.setValue(4);
            }
        });

        bt5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuView.setValue(5);
            }
        });

        bt6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuView.setValue(6);
            }
        });

        bt7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuView.setValue(7);
            }
        });

        bt8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuView.setValue(8);
            }
        });

        bt9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuView.setValue(9);
            }
        });

        btAnotations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuView.setInAnotationsMode();
            }
        });

        btHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuView.getHint();
            }
        });

        btApaga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuView.deleteCellValue();
            }
        });

    }

    private void createButtons(){

        bt1 = findViewById(R.id.button);
        bt2 = findViewById(R.id.button2);
        bt3 = findViewById(R.id.button3);
        bt4 = findViewById(R.id.button4);
        bt5 = findViewById(R.id.button5);
        bt6 = findViewById(R.id.button6);
        bt7 = findViewById(R.id.button7);
        bt8 = findViewById(R.id.button8);
        bt9 = findViewById(R.id.button9);
        btAnotations = findViewById(R.id.btAnotations);
        btHint = findViewById(R.id.btHints);
        btApaga = findViewById(R.id.btDelete);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        board = sudokuView.getBoard();
        outState.putIntArray("boardLine1", board[0]);
        outState.putIntArray("boardLine2", board[1]);
        outState.putIntArray("boardLine3", board[2]);
        outState.putIntArray("boardLine4", board[3]);
        outState.putIntArray("boardLine5", board[4]);
        outState.putIntArray("boardLine6", board[5]);
        outState.putIntArray("boardLine7", board[6]);
        outState.putIntArray("boardLine8", board[7]);
        outState.putIntArray("boardLine9", board[8]);

        boardComp = sudokuView.getBoardComp();
        outState.putIntArray("boardCompLine1", boardComp[0]);
        outState.putIntArray("boardCompLine2", boardComp[1]);
        outState.putIntArray("boardCompLine3", boardComp[2]);
        outState.putIntArray("boardCompLine4", boardComp[3]);
        outState.putIntArray("boardCompLine5", boardComp[4]);
        outState.putIntArray("boardCompLine6", boardComp[5]);
        outState.putIntArray("boardCompLine7", boardComp[6]);
        outState.putIntArray("boardCompLine8", boardComp[7]);
        outState.putIntArray("boardCompLine9", boardComp[8]);

        outState.putLong("time", timeInMs);
        outState.putInt("errors", sudokuView.getErrors());
        outState.putInt("points", sudokuView.getPoints());
        outState.putInt("hints", sudokuView.getHints());
        outState.putBoolean("AnotationsMode", sudokuView.isInAnotationsMode());

        //guardar anotations
        int[][][] map = sudokuView.getAnotations();
        for (int i = 0; i < BOARD_SIZE; i++) {

            outState.putIntArray("anotationsV"+i+"L1", map[i][0]);
            outState.putIntArray("anotationsV"+i+"L2", map[i][1]);
            outState.putIntArray("anotationsV"+i+"L3", map[i][2]);
            outState.putIntArray("anotationsV"+i+"L4", map[i][3]);
            outState.putIntArray("anotationsV"+i+"L5", map[i][4]);
            outState.putIntArray("anotationsV"+i+"L6", map[i][5]);
            outState.putIntArray("anotationsV"+i+"L7", map[i][6]);
            outState.putIntArray("anotationsV"+i+"L8", map[i][7]);
            outState.putIntArray("anotationsV"+i+"L9", map[i][8]);

        }

        outState.putInt("col", sudokuView.getSelectedCelCol());
        outState.putInt("row", sudokuView.getSelectedCelLin());

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //Reiniciar a board
        board[0] = savedInstanceState.getIntArray("boardLine1");
        board[1] = savedInstanceState.getIntArray("boardLine2");
        board[2] = savedInstanceState.getIntArray("boardLine3");
        board[3] = savedInstanceState.getIntArray("boardLine4");
        board[4] = savedInstanceState.getIntArray("boardLine5");
        board[5] = savedInstanceState.getIntArray("boardLine6");
        board[6] = savedInstanceState.getIntArray("boardLine7");
        board[7] = savedInstanceState.getIntArray("boardLine8");
        board[8] = savedInstanceState.getIntArray("boardLine9");

        sudokuView.setBoard(board);

        //Reiniciar o comparador
        boardComp[0] = savedInstanceState.getIntArray("boardCompLine1");
        boardComp[1] = savedInstanceState.getIntArray("boardCompLine2");
        boardComp[2] = savedInstanceState.getIntArray("boardCompLine3");
        boardComp[3] = savedInstanceState.getIntArray("boardCompLine4");
        boardComp[4] = savedInstanceState.getIntArray("boardCompLine5");
        boardComp[5] = savedInstanceState.getIntArray("boardCompLine6");
        boardComp[6] = savedInstanceState.getIntArray("boardCompLine7");
        boardComp[7] = savedInstanceState.getIntArray("boardCompLine8");
        boardComp[8] = savedInstanceState.getIntArray("boardCompLine9");

        sudokuView.setBoardComp(boardComp);

        //Reiniciar o timer
        this.timeInMs = savedInstanceState.getLong("time");
        countDownTimer.cancel();
        startTimer();

        //Reiniciar os erros já existentes
        sudokuView.setErrors(savedInstanceState.getInt("errors"));

        //Reiniciar os pontos já existentes
        sudokuView.setPoints(savedInstanceState.getInt("points"));

        //Reinicia as dicas
        sudokuView.setHints(savedInstanceState.getInt("hints"));

        //Reinicia anotationsMode
        sudokuView.setAnotationsMode(savedInstanceState.getBoolean("AnotationsMode"));

        //guardar anotations
        int[][][] map = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {

            map[i][0] = savedInstanceState.getIntArray("anotationsV"+i+"L1");
            map[i][1] = savedInstanceState.getIntArray("anotationsV"+i+"L2");
            map[i][2] = savedInstanceState.getIntArray("anotationsV"+i+"L3");
            map[i][3] = savedInstanceState.getIntArray("anotationsV"+i+"L4");
            map[i][4] = savedInstanceState.getIntArray("anotationsV"+i+"L5");
            map[i][5] = savedInstanceState.getIntArray("anotationsV"+i+"L6");
            map[i][6] = savedInstanceState.getIntArray("anotationsV"+i+"L7");
            map[i][7] = savedInstanceState.getIntArray("anotationsV"+i+"L8");
            map[i][8] = savedInstanceState.getIntArray("anotationsV"+i+"L9");

        }

        sudokuView.setAnotations(map);
        sudokuView.setSelectedCelCol(savedInstanceState.getInt("col"));
        sudokuView.setSelectedCelLin(savedInstanceState.getInt("row"));

    }

}
