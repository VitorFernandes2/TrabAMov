package com.example.sudoku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sudoku.player.Player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import pt.isec.ans.sudokulibrary.Sudoku;

public class M3Activity extends AppCompatActivity {

    private static final int BOARD_SIZE = 9;
    private Button btAnotations;
    private long timeInMs;
    private SudokuViewM3 sudokuView;
    private Button bt1, bt2, bt3, bt4, bt5, bt6, bt7, bt8, bt9, btHint, btApaga, btChangeMode;
    private TextView tvTimer, tvErrors, tvPoints, tvPlayer;
    private int[][] board = new int[BOARD_SIZE][BOARD_SIZE];
    private int[][] boardComp = new int[BOARD_SIZE][BOARD_SIZE];
    private int[][][] anotations = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
    int difficulty;
    boolean server;

    ProgressDialog pd = null;
    ServerSocket serverSocket=null;
    Socket socketGame = null;
    Handler procMsg = null;
    BufferedReader input;
    PrintWriter output;
    private int PORT = 8899;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_m3);

        final FrameLayout flSudoku = findViewById(R.id.flSudokuM2);
        createButtons();

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(this, R.string.noint, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        server = getIntent().getBooleanExtra("isserver",false);

        tvErrors = findViewById(R.id.textView6M2);
        tvPoints = findViewById(R.id.textView5M2);
        tvPlayer = findViewById(R.id.tvPlayerNameM2);
        tvTimer = findViewById(R.id.tvTimerM2);
        sudokuView = new SudokuViewM3(this, bt1, bt2, bt3, bt4, bt5, bt6, bt7,
                bt8, bt9, tvErrors, tvPoints, tvPlayer, tvTimer, server);
        flSudoku.addView(sudokuView);
        createButtonsListener();
        difficulty = getIntent().getIntExtra("difficulty", 4);
        

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

        procMsg = new Handler();
        /*if(server == true){
            server();

        }else{
            clientDlg();
        }*/


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(server == true){
            server();

        }else{
            clientDlg();
        }
    }

    void clientDlg() {
        final EditText edtIP = new EditText(this);
        final android.app.AlertDialog ad = new AlertDialog.Builder(this).setTitle("Sudoku Client")
                .setMessage("Server IP").setView(edtIP)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        client(edtIP.getText().toString(), PORT);
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                }).create();

        edtIP.setMaxLines(1);
        edtIP.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (edtIP.getText().toString().isEmpty())
                    return false;
                client(edtIP.getText().toString(), PORT);
                ad.dismiss();
                return true;
            }
        });
        edtIP.setText("10.0.2.2");

        ad.show();
    }

    void client(final String strIP, final int Port) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("Sudoku", "Connecting to the server  " + strIP);
                    socketGame = new Socket(strIP, Port);
                } catch (Exception e) {
                    socketGame = null;
                }
                if (socketGame == null) {
                    procMsg.post(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                    return;
                }
                commThread.start();
            }
        });
        t.start();
    }

    void server() {
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress()); // get ip
        pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.serverdlg_msg) + "\n(IP: " + ip
                + ")");
        pd.setTitle(R.string.serverdlg_title);
        pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
                if (serverSocket!=null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                    }
                    serverSocket=null;
                }

            }
        });
        pd.show();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(PORT);
                    socketGame = serverSocket.accept();
                    serverSocket.close();
                    serverSocket=null;
                    commThread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    socketGame = null;
                }
                procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                        if (socketGame == null)
                            Log.d("Sudoku", "was finished (M3 - line 234)");
                            finish();
                    }
                });
            }
        });
        t.start();
    }

    Thread commThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                input = new BufferedReader(new InputStreamReader(
                        socketGame.getInputStream()));
                output = new PrintWriter(socketGame.getOutputStream());
                while (!Thread.currentThread().isInterrupted()) {
                    String read = input.readLine();
                    final int move = Integer.parseInt(read);
                    Log.d("Sudoku", "Received: " + move);
                    procMsg.post(new Runnable() {
                        @Override
                        public void run() {
                            moveOtherPlayer(move);
                        }
                    });
                }
            } catch (Exception e) {
                procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        Toast.makeText(getApplicationContext(),
                                R.string.game_finished, Toast.LENGTH_LONG)
                                .show();
                        changetoM1();
                    }
                });
            }
        }
    });

    protected void onPause() {
        super.onPause();
        try {
            commThread.interrupt();
            if (socketGame != null)
                socketGame.close();
            if (output != null)
                output.close();
            if (input != null)
                input.close();
        } catch (Exception e) {
        }
        input = null;
        output = null;
        socketGame = null;
    };

    private void moveOtherPlayer(int mov){

    }

    private void gerar(int level){

        if(server == true) {

            String strJson = Sudoku.generate(level);
            Log.e("Sudoku", "Json: " + strJson);

            try {
                JSONObject json = new JSONObject(strJson);

                if (json.optInt("result", 0) == 1) {

                    JSONArray arrayJson = json.getJSONArray("board");
                    int[][] array = convert(arrayJson);
                    sudokuView.setBoard(array);

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else{

            int[][] array = new int[BOARD_SIZE][BOARD_SIZE];
            sudokuView.setBoard(array);

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


    private void changetoM1(){

        Intent intent = new Intent(M3Activity.this, M1Activity.class);

        intent.putExtra("changeMode", "changeMode");
        intent.putExtra("difficulty", difficulty);

        board = sudokuView.getBoard();
        intent.putExtra("boardLine1", board[0]);
        intent.putExtra("boardLine2", board[1]);
        intent.putExtra("boardLine3", board[2]);
        intent.putExtra("boardLine4", board[3]);
        intent.putExtra("boardLine5", board[4]);
        intent.putExtra("boardLine6", board[5]);
        intent.putExtra("boardLine7", board[6]);
        intent.putExtra("boardLine8", board[7]);
        intent.putExtra("boardLine9", board[8]);

        boardComp = sudokuView.getBoardComp();
        intent.putExtra("boardCompLine1", boardComp[0]);
        intent.putExtra("boardCompLine2", boardComp[1]);
        intent.putExtra("boardCompLine3", boardComp[2]);
        intent.putExtra("boardCompLine4", boardComp[3]);
        intent.putExtra("boardCompLine5", boardComp[4]);
        intent.putExtra("boardCompLine6", boardComp[5]);
        intent.putExtra("boardCompLine7", boardComp[6]);
        intent.putExtra("boardCompLine8", boardComp[7]);
        intent.putExtra("boardCompLine9", boardComp[8]);

        intent.putExtra("hints", sudokuView.getHints());
        intent.putExtra("AnotationsMode", sudokuView.isInAnotationsMode());

        //guardar anotations
        int[][][] map = sudokuView.getAnotations();
        for (int i = 0; i < BOARD_SIZE; i++) {

            intent.putExtra("anotationsV"+i+"L1", map[i][0]);
            intent.putExtra("anotationsV"+i+"L2", map[i][1]);
            intent.putExtra("anotationsV"+i+"L3", map[i][2]);
            intent.putExtra("anotationsV"+i+"L4", map[i][3]);
            intent.putExtra("anotationsV"+i+"L5", map[i][4]);
            intent.putExtra("anotationsV"+i+"L6", map[i][5]);
            intent.putExtra("anotationsV"+i+"L7", map[i][6]);
            intent.putExtra("anotationsV"+i+"L8", map[i][7]);
            intent.putExtra("anotationsV"+i+"L9", map[i][8]);

        }

        intent.putExtra("col", sudokuView.getSelectedCelCol());
        intent.putExtra("row", sudokuView.getSelectedCelLin());

        Player[] players = sudokuView.getPlayers();

        intent.putExtra("points", players[0].getPoint() + players[1].getPoint());
        intent.putExtra("errors", players[0].getErrors() + players[1].getErrors());

        startActivity(intent);

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

        /*btChangeMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Ao clicar mudar de vista

                Intent intent = new Intent(M3Activity.this, M1Activity.class);

                intent.putExtra("changeMode", "changeMode");
                intent.putExtra("difficulty", difficulty);

                board = sudokuView.getBoard();
                intent.putExtra("boardLine1", board[0]);
                intent.putExtra("boardLine2", board[1]);
                intent.putExtra("boardLine3", board[2]);
                intent.putExtra("boardLine4", board[3]);
                intent.putExtra("boardLine5", board[4]);
                intent.putExtra("boardLine6", board[5]);
                intent.putExtra("boardLine7", board[6]);
                intent.putExtra("boardLine8", board[7]);
                intent.putExtra("boardLine9", board[8]);

                boardComp = sudokuView.getBoardComp();
                intent.putExtra("boardCompLine1", boardComp[0]);
                intent.putExtra("boardCompLine2", boardComp[1]);
                intent.putExtra("boardCompLine3", boardComp[2]);
                intent.putExtra("boardCompLine4", boardComp[3]);
                intent.putExtra("boardCompLine5", boardComp[4]);
                intent.putExtra("boardCompLine6", boardComp[5]);
                intent.putExtra("boardCompLine7", boardComp[6]);
                intent.putExtra("boardCompLine8", boardComp[7]);
                intent.putExtra("boardCompLine9", boardComp[8]);

                intent.putExtra("hints", sudokuView.getHints());
                intent.putExtra("AnotationsMode", sudokuView.isInAnotationsMode());

                //guardar anotations
                int[][][] map = sudokuView.getAnotations();
                for (int i = 0; i < BOARD_SIZE; i++) {

                    intent.putExtra("anotationsV"+i+"L1", map[i][0]);
                    intent.putExtra("anotationsV"+i+"L2", map[i][1]);
                    intent.putExtra("anotationsV"+i+"L3", map[i][2]);
                    intent.putExtra("anotationsV"+i+"L4", map[i][3]);
                    intent.putExtra("anotationsV"+i+"L5", map[i][4]);
                    intent.putExtra("anotationsV"+i+"L6", map[i][5]);
                    intent.putExtra("anotationsV"+i+"L7", map[i][6]);
                    intent.putExtra("anotationsV"+i+"L8", map[i][7]);
                    intent.putExtra("anotationsV"+i+"L9", map[i][8]);

                }

                intent.putExtra("col", sudokuView.getSelectedCelCol());
                intent.putExtra("row", sudokuView.getSelectedCelLin());

                Player[] players = sudokuView.getPlayers();

                intent.putExtra("points", players[0].getPoint() + players[1].getPoint());
                intent.putExtra("errors", players[0].getErrors() + players[1].getErrors());

                startActivity(intent);

            }
        });*/

    }

    private void createButtons(){

        bt1 = findViewById(R.id.buttonM2);
        bt2 = findViewById(R.id.button2M2);
        bt3 = findViewById(R.id.button3M2);
        bt4 = findViewById(R.id.button4M2);
        bt5 = findViewById(R.id.button5M2);
        bt6 = findViewById(R.id.button6M2);
        bt7 = findViewById(R.id.button7M2);
        bt8 = findViewById(R.id.button8M2);
        bt9 = findViewById(R.id.button9M2);
        btAnotations = findViewById(R.id.btAnotationsM2);
        btHint = findViewById(R.id.btHintsM2);
        btApaga = findViewById(R.id.btDeleteM2);
        //btChangeMode = findViewById(R.id.btChangeM2ToM1);

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

        Player[] players = sudokuView.getPlayers();

        outState.putInt("playerIndex", sudokuView.getPlayerIndex());

        //Guarda os dados do Player 1
        outState.putString("player1Name", players[0].getName());
        outState.putInt("player1Errors", players[0].getErrors());
        outState.putInt("player1Points", players[0].getPoint());
        outState.putLong("player1Time", players[0].getTime());

        //Guarda os dados do Player 2
        outState.putString("player2Name", players[1].getName());
        outState.putInt("player2Errors", players[1].getErrors());
        outState.putInt("player2Points", players[1].getPoint());
        outState.putLong("player2Time", players[1].getTime());

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

        Player[] players = new Player[2];
        players[0] = new Player(
                savedInstanceState.getString("player1Name"),
                savedInstanceState.getInt("player1Points"),
                savedInstanceState.getInt("player1Errors"),
                savedInstanceState.getLong("player1Time")
        );
        players[1] = new Player(
                savedInstanceState.getString("player2Name"),
                savedInstanceState.getInt("player2Points"),
                savedInstanceState.getInt("player2Errors"),
                savedInstanceState.getLong("player2Time")
        );
        sudokuView.setPlayers(players);

        //Guarda o index do jogador
        sudokuView.setPlayerIndex(savedInstanceState.getInt("playerIndex"));

        //Mete os dados corretos no ecrã
        sudokuView.restartGame();

    }


}
