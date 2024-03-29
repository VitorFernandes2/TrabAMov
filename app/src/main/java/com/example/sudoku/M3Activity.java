package com.example.sudoku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.icu.text.IDNA;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
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
    boolean serveraccept = true;

    ProgressDialog pd = null;
    ServerSocket serverSocket=null;
    Socket socketGame = null;
    Handler procMsg = null;
    BufferedReader input;
    PrintWriter output;
    private int PORT = 8899;

    private int[][] boardbackup;

    // 2nd player
    Socket socketGame2 = null;
    BufferedReader input2;
    PrintWriter output2;
    Handler procMsg2 = null;

    // screenrotate backups

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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
        procMsg2 = new Handler();

        Player[] tempa = sudokuView.getPlayers();
        SharedPreferences sharedPref = getSharedPreferences("user_id", MODE_PRIVATE);
        String userId = sharedPref.getString("user_id", "Username");
        tempa[0].setName(userId);
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

        // apaga servidores se por acaso manter informação
        if (serverSocket!=null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
            }
            serverSocket=null;
        }
        if (socketGame!=null) {
            try {
                socketGame.close();
            } catch (IOException e) {
            }
            socketGame=null;
        }
        if (socketGame2!=null) {
            try {
                socketGame2.close();
            } catch (IOException e) {
            }
            socketGame2=null;
        }
        //serveraccept = false;

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
                    int qual = 0;
                    serverSocket = new ServerSocket(PORT);
                    while(serveraccept) {
                        if(qual == 0){

                            socketGame = serverSocket.accept();
                            commThread.start();
                            qual = 1;
                            pd.dismiss();
                            serverSocket.close();
                            serverSocket = null;
                        }else{
                            serverSocket = new ServerSocket(PORT);
                            socketGame2 = serverSocket.accept();
                            serverSocket.close();
                            serverSocket = null;
                            Log.d("INFO", "entrou outro jogador");
                            commThread2.start();
                            serveraccept = false;
                        }

                    }
                    /*serverSocket = new ServerSocket(PORT);
                    socketGame = serverSocket.accept();
                    serverSocket.close();
                    serverSocket=null;
                    commThread.start();*/
                } catch (Exception e) {
                    e.printStackTrace();
                    socketGame = null;
                }
                procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        //pd.dismiss();
                        Toast.makeText(getApplicationContext()
                                , "A 2nd player has entered", Toast.LENGTH_SHORT).show();
                        if (socketGame == null) {
                            Log.d("Sudoku", "was finished (M3 - line 234)");
                            pd.dismiss();
                            finish();
                        }
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
                if(server == false){
                    clientstartermsg(output);
                }
                sudokuView.setprintwriter(output);

                while (!Thread.currentThread().isInterrupted()) {
                    Log.d("ServerINFO", "antes do readline");
                    String read = input.readLine();
                    Log.d("ServerINFO", "depois do readline" + read);
                    final JSONObject jo = new JSONObject(read);
                    Log.d("ServerINFO", "Received: " + jo);
                    procMsg.post(new Runnable() {
                        @Override
                        public void run() {
                            precessreceiveinfo(jo,1);
                        }
                    });

                }
            } catch (Exception e) {
                Log.d("ServerINFO", "exeption: " + e.toString());
                procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        Toast.makeText(getApplicationContext(),
                                R.string.game_finished, Toast.LENGTH_LONG)
                                .show();
                        if(sudokuView.result() == false)
                            changetoM1();
                    }
                });
            }
        }
    });

    Thread commThread2 = new Thread(new Runnable() {
        @Override
        public void run() { // exclusivamente utilizador pelo servidor
            try {
                input2 = new BufferedReader(new InputStreamReader(
                        socketGame2.getInputStream()));
                output2 = new PrintWriter(socketGame2.getOutputStream());
                sudokuView.setprintwriter2(output2);

                while (!Thread.currentThread().isInterrupted()) {
                    Log.d("ServerINFO", "antes do readline 2");
                    String read = input2.readLine();
                    Log.d("ServerINFO", "depois do readline 2" + read);
                    final JSONObject jo = new JSONObject(read);
                    Log.d("ServerINFO", "Received 2: " + jo);
                    procMsg2.post(new Runnable() {
                        @Override
                        public void run() {
                            precessreceiveinfo(jo,2);
                        }
                    });

                }
            } catch (Exception e) {
                Log.d("ServerINFO", "exeption: " + e.toString());
                procMsg2.post(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        Toast.makeText(getApplicationContext(),
                                R.string.game_finished, Toast.LENGTH_LONG)
                                .show();
                        if(sudokuView.result() == false)
                            changetoM1();
                    }
                });
            }
        }
    });


    private void clientstartermsg(PrintWriter output){

        JSONObject praenv = new JSONObject();
        SharedPreferences sharedPref = getSharedPreferences("user_id", MODE_PRIVATE);
        String userId = sharedPref.getString("user_id", "Username");

        try {
            praenv.put("acao",1);
            praenv.put("extra", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("sudoku", "Sending begening client msg");
        output.println(praenv.toString());
        output.flush();

        //sudokuView.setprintwriter(output);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("ServerINFO", "Server: on pause lançado");
        try {
            commThread.interrupt();
            commThread2.interrupt();
            if (socketGame != null)
                socketGame.close();
            if (socketGame2 != null)
                socketGame2.close();
            if (output != null)
                output.close();
            if (output2 != null)
                output2.close();
            if (input != null)
                input.close();
            if (input2 != null)
                input2.close();
            if(serverSocket != null)
                serverSocket.close();
        } catch (Exception e) {
        }
        input = null;
        output = null;
        socketGame = null;
        socketGame2 = null;
        input2 = null;
        output2 = null;
        serveraccept = false;
        serverSocket = null;
    };

    private void precessreceiveinfo(JSONObject mov,int pessoa){

        /*
        Layout do json a enviar/receber:
        int acao -> define o tipo de ação
        int val -> define um valor a meter
        int poscol -> define a posicao coluna
        int poslin -> define a posica linha
        string extra -> informação extra
        */

        int acao = 0;
        try {
            acao = mov.getInt("acao");
            //val = mov.getInt("val");
            //posx = mov.getInt("posx");
            //posy = mov.getInt("posy");
            //extra = mov.getString("extra");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(acao == 1){ // mensagem inical de login de user (server)
            if(server == true) { // confere se é servidor

                // define name
                String name = "playerclient";
                try {
                    name = mov.getString("extra");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Player[] cop = sudokuView.getPlayers();
                if(pessoa == 1)
                    cop[1].setName(name);
                else
                    cop[2].setName(name);

                Log.d("Sudoku", "Recebi comando 1" );
                final JSONObject praenv = new JSONObject();
                String out = Integer.toString(boardbackup[0][0]);

                for (int i = 0; i < BOARD_SIZE; i++){

                    for(int j = 0; j< BOARD_SIZE; j++){
                        if(!(i == 0 && j == 0))
                            out += Integer.toString(boardbackup[i][j]);
                    }
                }

                // fix de timer (Restart )
                if(pessoa == 1) {
                    //sudokuView.CancelCountDownTimer();
                    sudokuView.setPlayerIndex(0);
                    sudokuView.resetTimes();
                    sudokuView.startTimer();
                }
                // -----

                try {
                    praenv.put("acao",3);
                    praenv.put("extra", out);
                    praenv.put("nome" , sudokuView.getPlayers()[sudokuView.getPlayerIndex()].getName());
                    praenv.put("time",sudokuView.getPlayers()[sudokuView.getPlayerIndex()].getTime());
                    praenv.put("atpoint" , sudokuView.getPlayers()[sudokuView.getPlayerIndex()].getPoint());
                    praenv.put("aterror",sudokuView.getPlayers()[sudokuView.getPlayerIndex()].getErrors());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(pessoa == 1) { // se for o client 1
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.d("sudokuINFO", "Server: sending table info to client");
                                output.println(praenv.toString());
                                output.flush();
                            } catch (Exception e) {
                                Log.d("sudokuINFO", "Server: sending table info to client exept");
                            }
                        }
                    });
                    t.start();
                } else { // se for o client 2
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.d("sudokuINFO", "Server: sending table info to client");
                                output2.println(praenv.toString());
                                output2.flush();
                            } catch (Exception e) {
                                Log.d("sudokuINFO", "Server: sending table info to client exept");
                            }
                        }
                    });
                    t.start();
                }

            }
        }

        if(acao == 3){ // recebe o tabuleiro no inicio (client)

            Log.d("Sudoku", "Recebi comando 3" );
            int[][] array = null;
            String nameat = "player"; long timeat = 0; int point = 0,error=0;
            try {

                String extra2 = mov.getString("extra");
                array = convertStringArray(extra2);
                nameat = mov.getString("nome");
                timeat = mov.getLong("time");
                point = mov.getInt("atpoint");
                error = mov.getInt("aterror");

            } catch (JSONException e) {
                e.printStackTrace();
            }
            //int[][] array = convertStringArray(extra);

            sudokuView.setBoard(array,nameat,timeat,point,error);

        }

        // ------------ metodos para landscape -------------
        if(acao == 31){ // recebe pedido de client de toda a informação atual (server)

            sudokuView.sendclientcurrentinfo(pessoa);

        }
        // -------------------------

        if(acao == 4){ // recebe informação de um valor a alterar (client)

            int val = -1,poscol = -1,poslin = -1;
            try {

                val = mov.getInt("val");
                poscol = mov.getInt("poscol");
                poslin = mov.getInt("poslin");
                //nome = mov.getString("extra");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            sudokuView.setValue(val,poscol,poslin);
        }

        if(acao == 6){ // recebe update de user atual (client)

            int point = -1,errors = -1;String nome = null;boolean turn = false;
            try {

                nome = mov.getString("extra");
                point = mov.getInt("poscol");
                errors = mov.getInt("poslin");
                turn = mov.getBoolean("turn");
                //nome = mov.getString("extra");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            sudokuView.setTurn(turn);
            sudokuView.startTimersocket(nome,point,errors,-1);
        }

        if(acao == 8){ // recebe pedido de valor do client (server)

            int value = -1,poscol = -1,poslin = -1;
            try {

                value = mov.getInt("val");
                poscol = mov.getInt("poscol");
                poslin = mov.getInt("poslin");

            } catch (JSONException e) {
                e.printStackTrace();
            }
            boolean val = false;
            if(sudokuView.calculateview(value,poscol,poslin)){
                val = true;
                sudokuView.setValue(value,poscol,poslin);
            }else{
                int errosplay = sudokuView.getPlayers()[sudokuView.getPlayerIndex()].getErrors();
                sudokuView.getPlayers()[sudokuView.getPlayerIndex()].setErrors(errosplay +1);
                envsigerrado(pessoa);
            }

            final JSONObject praenv = new JSONObject();
            try {
                praenv.put("acao",9);
                praenv.put("val", value);
                praenv.put("poscol", poscol);
                praenv.put("poslin", poslin);
                praenv.put("result", val);
                praenv.put("points", sudokuView.getPlayers()[sudokuView.getPlayerIndex()].getPoint());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(pessoa == 1) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("sudokuINFO", "Server: sending val possviel to client");
                            output.println(praenv.toString());
                            output.flush();
                        } catch (Exception e) {
                            Log.d("sudokuINFO", "Server: sending val possviel to client exept");
                        }
                    }
                });
                t.start();

                if(val == true){

                    final JSONObject enviftrue = new JSONObject();

                    try {
                        enviftrue.put("acao",4);
                        enviftrue.put("val", value);
                        enviftrue.put("poscol", poscol);
                        enviftrue.put("poslin", poslin);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(output2 != null) {
                        Thread t5 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Log.d("ViewINFO", "Serverview: sending new val to client 2 if correct");
                                    output2.println(enviftrue.toString());
                                    output2.flush();
                                } catch (Exception e) {
                                    Log.d("ViewINFO", "Serverview: sending new val to client 2 if correct exept");
                                }
                            }
                        });
                        t5.start();
                    } else {
                        Log.d("ViewINFO", "não ha copia de printwriter (line 642)");
                    }
                }

            }else{
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("sudokuINFO", "Server: sending val possviel to client");
                            output2.println(praenv.toString());
                            output2.flush();
                        } catch (Exception e) {
                            Log.d("sudokuINFO", "Server: sending val possviel to client exept");
                        }
                    }
                });
                t.start();

                if(val == true){

                    final JSONObject enviftrue = new JSONObject();

                    try {
                        enviftrue.put("acao",4);
                        enviftrue.put("val", value);
                        enviftrue.put("poscol", poscol);
                        enviftrue.put("poslin", poslin);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(output != null) {
                        Thread t5 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Log.d("ViewINFO", "Serverview: sending new val to client 2 if correct");
                                    output.println(enviftrue.toString());
                                    output.flush();
                                } catch (Exception e) {
                                    Log.d("ViewINFO", "Serverview: sending new val to client 2 if correct exept");
                                }
                            }
                        });
                        t5.start();
                    } else {
                        Log.d("ViewINFO", "não ha copia de printwriter (line 642)");
                    }
                }

            }
            if (sudokuView.result()) {

                String[] resultados = sudokuView.getVecedor();

                final JSONObject praenve = new JSONObject();
                try {
                    praenve.put("acao", 10);
                    //praenve.put("venced", sudokuView.getPlayers()[sudokuView.getPlayerIndex()].getName());
                    //praenve.put("points", sudokuView.getPlayers()[sudokuView.getPlayerIndex()].getPoint());
                    praenve.put("venced", resultados[0]);
                    praenve.put("points", Integer.parseInt(resultados[1]));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Thread t2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("sudokuINFO", "Server: sending vencedor to client");
                            output.println(praenve.toString());
                            output.flush();
                        } catch (Exception e) {
                            Log.d("sudokuINFO", "Server: sending vencedor to client exept");
                        }
                    }
                });
                t2.start();

                if(output2 != null){
                    Thread t3 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.d("sudokuINFO", "Server: sending vencedor to client 2");
                                output2.println(praenve.toString());
                                output2.flush();
                            } catch (Exception e) {
                                Log.d("sudokuINFO", "Server: sending vencedor to client 2 exept");
                            }
                        }
                    });
                    t3.start();
                }

                /*if (socketGame != null) {
                    try {
                        socketGame.close();
                        if (socketGame2 != null)
                            socketGame2.close();
                        serveraccept = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }*/
                sudokuView.winnerserver(resultados[0], Integer.parseInt(resultados[1]));
            }

        }

        if(acao == 9){ // recebe resposta de servidor sobre valor escolhido (client)

            int value = -1,poscol = -1,poslin = -1,points = 0; boolean resultado = false;
            try {

                value = mov.getInt("val");
                poscol = mov.getInt("poscol");
                poslin = mov.getInt("poslin");
                resultado = mov.getBoolean("result");
                points = mov.getInt("points");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            sudokuView.respotvalueserv(value,poscol,poslin,resultado,points);
            //finish();
        }

        if(acao == 10){ // servidor avisou que um elemento venceu o jogo (client)

            /*
            praenve.put("acao",10);
            praenve.put("venced", sudokuView.getPlayers()[sudokuView.getPlayerIndex()].getName());
            praenve.put("points", sudokuView.getPlayers()[sudokuView.getPlayerIndex()].getPoint());*/

            String venc = "player"; int point = 0;
            try {

                venc = mov.getString("venced");
                point = mov.getInt("points");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            sudokuView.winnerserver(venc,point);

        }

        if(acao == 11){ // servidor avisou para incrementar o valor dos errados o jogo (client)

            sudokuView.inccorrenterror();

        }

    }

    private void envsigerrado(int pessoa) {

        final JSONObject praenve = new JSONObject();
        try {
            praenve.put("acao", 11);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(pessoa != 1) {
            Thread t2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d("sudokuINFO", "Server: sending vencedor to client");
                        output.println(praenve.toString());
                        output.flush();
                    } catch (Exception e) {
                        Log.d("sudokuINFO", "Server: sending vencedor to client exept");
                    }
                }
            });
            t2.start();
        }

        if(pessoa != 2) {
            if (output2 != null) {
                Thread t3 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("sudokuINFO", "Server: sending vencedor to client 2");
                            output2.println(praenve.toString());
                            output2.flush();
                        } catch (Exception e) {
                            Log.d("sudokuINFO", "Server: sending vencedor to client 2 exept");
                        }
                    }
                });
                t3.start();
            }
        }

        sudokuView.inccorrenterror();
    }

    private  int[][] convertStringArray(String val){

        String[] vals = val.split("(?!^)");

        int[][] out = new int[BOARD_SIZE][BOARD_SIZE];

        for (int i = 0; i < BOARD_SIZE; i++){

            for(int j = 0; j< BOARD_SIZE; j++){

                int pos = (i*BOARD_SIZE)+j;
                out[i][j] = Integer.parseInt(vals[pos]);
            }

        }

        return out;

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
                    boardbackup = array; // backup da border original
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

    }

    public void updateview(){
        sudokuView.forceupdate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
            // guardar informação pre nova view
            Player[] playersback = null ;int indexback= -1;int boardback[][] = null;int boardcompback[][] = null; int anotback[][][] = null;
            boolean turnback = false;
            if(server == true){
                playersback = copyplayers();
                indexback = sudokuView.getPlayerIndex();
                boardback = copyboard(1);
                boardcompback = copyboard(2);
                anotback = copyanotations();
                sudokuView.CancelCountDownTimer();
                turnback = sudokuView.getmyturn();
            }
            // ----

            setContentView(R.layout.activity_m3_land);
            //tvErrors = findViewById(R.id.textView6M2);
            tvErrors = findViewById(R.id.textView6M2);
            tvPoints = findViewById(R.id.textView5M2);
            tvPlayer = findViewById(R.id.tvPlayerNameM2);
            tvTimer = findViewById(R.id.tvTimerM2);

            SudokuViewM3 su = new SudokuViewM3(this, bt1, bt2, bt3, bt4, bt5, bt6, bt7,
                    bt8, bt9, tvErrors, tvPoints, tvPlayer, tvTimer, server,1);
            createButtons();
            final FrameLayout flSudoku = findViewById(R.id.flSudokuM2);
            flSudoku.removeAllViews();
            flSudoku.addView(su);
            sudokuView = su;
            createButtonsListener();
            sudokuView.setprintwriter(output);
            //pede/obtem info
            if(server == false){
                final JSONObject praenv = new JSONObject();
                try {
                    praenv.put("acao",31);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(server == false) { // se for o client 1
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.d("sudokuINFO", "Server: sending table info to client");
                                output.println(praenv.toString());
                                output.flush();
                            } catch (Exception e) {
                                Log.d("sudokuINFO", "Server: sending table info to client exept");
                            }
                        }
                    });
                    t.start();
                }
            } else{

                if(boardback != null && boardcompback!= null && indexback != -1 && playersback != null && anotback != null) {
                    sudokuView.setBoard(boardback);
                    sudokuView.setBoardComp(boardcompback);
                    sudokuView.setPlayerIndex(indexback);
                    sudokuView.setPlayers(playersback);
                    sudokuView.setAnotations(anotback);
                    sudokuView.setmyturn(turnback);
                }
                sudokuView.setprintwriter2(output2);
                sudokuView.startTimer();

            }
            // -----
            updateview();

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();

            // guardar informação pre nova view
            Player[] playersback = null ;int indexback= -1;int boardback[][] = null;int boardcompback[][] = null;int anotback[][][] = null;
            boolean turnback = false;
            if(server == true){
                playersback = copyplayers();
                indexback = sudokuView.getPlayerIndex();
                boardback = copyboard(1);
                boardcompback = copyboard(2);
                anotback = copyanotations();
                sudokuView.CancelCountDownTimer();
                turnback = sudokuView.getmyturn();
            }
            // ----

            setContentView(R.layout.activity_m3);
            //tvErrors = findViewById(R.id.textView6M2);
            tvErrors = findViewById(R.id.textView6M2);
            tvPoints = findViewById(R.id.textView5M2);
            tvPlayer = findViewById(R.id.tvPlayerNameM2);
            tvTimer = findViewById(R.id.tvTimerM2);

            SudokuViewM3 su = new SudokuViewM3(this, bt1, bt2, bt3, bt4, bt5, bt6, bt7,
                    bt8, bt9, tvErrors, tvPoints, tvPlayer, tvTimer, server,1);
            createButtons();
            final FrameLayout flSudoku = findViewById(R.id.flSudokuM2);
            flSudoku.removeAllViews();
            flSudoku.addView(su);
            sudokuView = su;
            createButtonsListener();
            sudokuView.setprintwriter(output);
            //pede/obtem info
            if(server == false){
                final JSONObject praenv = new JSONObject();
                try {
                    praenv.put("acao",31);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(server == false) { // se for o client 1
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.d("sudokuINFO", "Server: sending table info to client");
                                output.println(praenv.toString());
                                output.flush();
                            } catch (Exception e) {
                                Log.d("sudokuINFO", "Server: sending table info to client exept");
                            }
                        }
                    });
                    t.start();
                }
            } else{

                if(boardback != null && boardcompback!= null && indexback != -1 && playersback != null && anotback != null) {
                    sudokuView.setBoard(boardback);
                    sudokuView.setBoardComp(boardcompback);
                    sudokuView.setPlayerIndex(indexback);
                    sudokuView.setPlayers(playersback);
                    sudokuView.setAnotations(anotback);
                    sudokuView.setmyturn(turnback);
                }
                sudokuView.setprintwriter2(output2);
                sudokuView.startTimer();

            }
            // -----
            updateview();
        }

    }

    private int[][] copyboard(int type) {

        int[][] getoriginalboard = sudokuView.getBoard();
        int[][] getoriginalboardcomp = sudokuView.getBoardComp();

        int[][] out = new int[BOARD_SIZE][BOARD_SIZE];

        for (int i = 0; i < BOARD_SIZE; i++){

            for (int j = 0; j < BOARD_SIZE ; j++){

                if(type == 1){
                    out[i][j] = getoriginalboard[i][j];
                }else{
                    out[i][j] = getoriginalboardcomp[i][j];
                }

            }

        }

        return out;
    }

    private int[][][] copyanotations() {

        int[][][] getoriginalanot= sudokuView.getAnotations();

        int[][][] out = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];

        for (int i = 0; i < BOARD_SIZE; i++){

            for (int j = 0; j < BOARD_SIZE ; j++){

                for (int k = 0; k < BOARD_SIZE ; k++ ){
                    out[i][j][k] = getoriginalanot[i][j][k];
                }

            }

        }

        return out;
    }

    private Player[] copyplayers() {

        Player[] playersback = new Player[3];
        Player[] playersback2 = sudokuView.getPlayers();

        for(int i = 0; i < 3; i++){
            playersback[i] = new Player(playersback2[i].getName(),playersback2[i].getPoint(),playersback2[i].getErrors(),playersback2[i].getTime());
        }

        return playersback;

    }

}
