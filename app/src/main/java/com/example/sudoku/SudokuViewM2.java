package com.example.sudoku;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.sudoku.player.Player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pt.isec.ans.sudokulibrary.Sudoku;

import static android.content.Context.MODE_PRIVATE;

public class SudokuViewM2 extends View {

    public static final int BOARD_SIZE = 9;

    private Player[] players = new Player[2];
    private CountDownTimer countDownTimer;
    private int playerIndex = 0;

    private boolean inAnotationsMode;
    Paint paintMainLines, paintSubLines, paintMainNumbers, paintSmallNumbers;

    int [][] board = new int[BOARD_SIZE][BOARD_SIZE];
    int [][] boardComp = new int[BOARD_SIZE][BOARD_SIZE];
    private int [][][]anotations = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
    private int selectedCelLin = -1;
    private int selectedCelCol = -1;
    private int points = 0;
    private int hints = 3;

    private TextView tvErrors, tvPoints, tvPlayer, tvTimer;
    private Button bt1, bt2, bt3, bt4, bt5, bt6, bt7, bt8, bt9;

    public SudokuViewM2(Context context, Button bt1, Button bt2, Button bt3, Button bt4, Button bt5,
                      Button bt6, Button bt7, Button bt8, Button bt9, TextView tvErrors,
                      TextView tvPoints, TextView tvPlayer, TextView tvTimer) {
        super(context);
        inAnotationsMode = false;

        this.bt1 = bt1;
        this.bt2 = bt2;
        this.bt3 = bt3;
        this.bt4 = bt4;
        this.bt5 = bt5;
        this.bt6 = bt6;
        this.bt7 = bt7;
        this.bt8 = bt8;
        this.bt9 = bt9;

        this.tvErrors = tvErrors;
        this.tvPoints = tvPoints;
        this.tvPlayer = tvPlayer;
        this.tvTimer = tvTimer;

        startInfo();
        createPaints();
    }

    private void startInfo(){

        //Prepare the players
        String name1 = getResources().getString(R.string.player1);
        String name2 = getResources().getString(R.string.player2);

        players[0] = new Player(name1,0,0,30000);
        players[1] = new Player(name2,0,0,30000);

        tvPlayer.setText(players[playerIndex].getName());
        startTimer();

    }

    private void resetTimes(){

        players[0].setTime(30000);
        players[1].setTime(30000);

    }

    public void startTimer() {

        countDownTimer = new CountDownTimer(players[playerIndex].getTime(), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                players[playerIndex].setTime(millisUntilFinished);
                updateTimer();
            }

            @Override
            public void onFinish() {

                //Quando o tempo acabar troca de jogador
                if (playerIndex == 0)
                    playerIndex = 1;
                else
                    playerIndex = 0;

                //Sempre que começa uma ronda nova faz reset aos tempos
                resetTimes();

                tvPlayer.setText(players[playerIndex].getName());
                tvPoints.setText("" + players[playerIndex].getPoint());
                tvErrors.setText("" + players[playerIndex].getErrors());
                startTimer();

            }

        }.start();

    }

    private void updateTimer() {

        int minutes = (int) players[playerIndex].getTime() / 60000;
        int seconds = (int) players[playerIndex].getTime() % 60000 / 1000;

        String strTimeLeft = "" + minutes + ":";
        if (seconds < 10) strTimeLeft += "0";
        strTimeLeft += "" + seconds;

        tvTimer.setText(strTimeLeft);

    }

    private void createPaints() {

        paintMainLines = new Paint(Paint.DITHER_FLAG);
        paintMainLines.setStyle(Paint.Style.FILL_AND_STROKE);
        paintMainLines.setColor(Color.BLACK);
        paintMainLines.setStrokeWidth(8);

        paintSubLines = new Paint(paintMainLines);
        paintSubLines.setStrokeWidth(3);

        paintMainNumbers = new Paint(paintSubLines);
        paintMainNumbers.setColor(Color.rgb(0,0,128));
        paintMainNumbers.setTextSize(32);
        paintMainNumbers.setTextAlign(Paint.Align.CENTER);

        paintSmallNumbers = new Paint(paintMainNumbers);
        paintSmallNumbers.setTextSize(12);
        paintSmallNumbers.setStrokeWidth(2);
        paintSmallNumbers.setColor(Color.rgb(0x40,0x80,0xa0));

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth(), cellW = w / BOARD_SIZE;
        int h = getWidth(), cellH = h / BOARD_SIZE;

        for (int i = 0; i <= BOARD_SIZE; i++) {

            canvas.drawLine(0, i * cellH,w, i * cellH, i % 3 == 0 ? paintMainLines : paintSubLines);
            canvas.drawLine(i * cellW, 0,i * cellW, h, i % 3 == 0 ? paintMainLines : paintSubLines);

        }

        if (board == null)
            return;

        paintMainNumbers.setTextSize(cellH / 2);
        paintSmallNumbers.setTextSize(cellH / 4);

        for (int r = 0; r < BOARD_SIZE; r++) {

            for (int c = 0; c < BOARD_SIZE; c++) {

                int n = board[r][c];
                if (n != 0){

                    int x = cellW / 2 + cellW * c;
                    int y = cellH / 2 + cellH * r + cellH / 6;

                    //Se este valor estiver errado
                    if (!Resolve(n, c, r)){

                        paintMainNumbers.setColor(Color.RED);
                        canvas.drawText(""+n, x, y, paintMainNumbers);
                        paintMainNumbers.setColor(Color.rgb(0,0,128));
                        players[playerIndex].setErrors(players[playerIndex].getErrors() + 1);
                        tvErrors.setText(""+players[playerIndex].getErrors());

                        board[r][c] = 0;
                        this.postInvalidateDelayed(3000);

                    }else{

                        if (boardComp[r][c] == 0)
                            paintMainNumbers.setColor(Color.GRAY);

                        canvas.drawText(""+n, x, y, paintMainNumbers);
                        paintMainNumbers.setColor(Color.rgb(0,0,128));
                    }



                } else {

                    int x = cellW / 6 + cellW * c;
                    int y = cellH / 6 + cellH * r;

                    for (int p = 0; p < BOARD_SIZE; p++) {

                        int n2 = anotations[p][r][c];

                        if (n2 != 0){

                            int xp = x + (p)%3 * cellW / 3;
                            int yp = y + (p)/3 * cellH / 3 + cellH / 9;
                            canvas.drawText("" + n2, xp, yp, paintSmallNumbers);

                        }

                    }

                    if (!inAnotationsMode){
                        if (selectedCelCol == c && selectedCelLin == r){

                            Paint paint = new Paint();
                            paint.setColor(Color.parseColor("#b2b2b2"));
                            canvas.drawRect(cellW * c + 2, cellH * r + 2, x + cellW - 20, y + cellH - 20, paint);

                        }
                    }else{
                        if (selectedCelCol == c && selectedCelLin == r){

                            Paint paint = new Paint();
                            paint.setColor(Color.parseColor("#cccccc85"));
                            canvas.drawRect(cellW * c + 2, cellH * r + 2, x + cellW - 20, y + cellH - 20, paint);

                        }
                    }

                }

            }

        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN)
            return true;

        if (event.getAction() == MotionEvent.ACTION_UP){

            int px = (int) event.getX();
            int py = (int) event.getY();
            int w = getWidth(), cellW = w / BOARD_SIZE;
            int h = getWidth(), cellH = h / BOARD_SIZE;
            int cellX = px / cellW;
            int cellY = py / cellH;

            selectedCelLin = cellY;
            selectedCelCol = cellX;
            //Assim que clicar numa posição invalida os botões com os valores já inseridos
            invalidateButtons();
            invalidate();

            return true;

        }

        return super.onTouchEvent(event);

    }

    public void setBoard(int[][] board){
        this.board = board;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                this.boardComp[i][j] = this.board[i][j];
            }
        }
        invalidate();
    }

    public void setInAnotationsMode() {
        this.inAnotationsMode = !this.inAnotationsMode;
        invalidateButtons();
        invalidate();
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

    private JSONArray convert(int [][]array){

        JSONArray arrayJson = new JSONArray();

        try{

            for (int r = 0; r < 9; r++) {

                JSONArray jsonRow = new JSONArray();

                for (int c = 0; c < 9; c++) {

                    jsonRow.put(array[r][c]);

                }

                arrayJson.put(jsonRow);

            }

        }catch (Exception e){

        }

        return arrayJson;

    }

    private boolean Resolve(int value, int column, int row){

        try {

            JSONObject json = new JSONObject();
            json.put("board", convert(boardComp));
            String strJson = Sudoku.solve(json.toString(), 1500);
            json = new JSONObject(strJson);

            //Se existir esse valor
            if (json.optInt("result",0) == 1){

                JSONArray arrayJson = json.getJSONArray("board");
                int [][] array = convert(arrayJson);

                //O valor está errado
                if (array[row][column] != value){

                    Log.e("Sudoku", "O valor está errado!");
                    return false;

                }
                else{
                    Log.e("Sudoku", "O valor está certo!");
                    return true;
                }

            }

        }catch (Exception e){

        }

        return true;

    }

    public int[][] getBoard() {
        return board;
    }

    public int getSelectedCelLin() {
        return selectedCelLin;
    }

    public int getSelectedCelCol() {
        return selectedCelCol;
    }

    public boolean isInAnotationsMode() {
        return inAnotationsMode;
    }

    //Verifica se ganhou
    public boolean result(){

        try {

            JSONObject json = new JSONObject();
            json.put("board", convert(boardComp));
            String strJson = Sudoku.solve(json.toString(), 1500);
            json = new JSONObject(strJson);

            //Se existir esse valor
            if (json.optInt("result",0) == 1){

                JSONArray arrayJson = json.getJSONArray("board");
                int [][] array = convert(arrayJson);

                for (int i = 0; i < BOARD_SIZE; i++)
                    for (int j = 0; j < BOARD_SIZE; j++)
                        if (board[i][j] != array[i][j])
                            return false;

            }

        }catch (Exception e){

        }

        return true;

    }

    private void invalidateButtons(){

        if (boardComp[selectedCelLin][selectedCelCol] != 0){

            bt1.setVisibility(View.INVISIBLE);
            bt2.setVisibility(View.INVISIBLE);
            bt3.setVisibility(View.INVISIBLE);
            bt4.setVisibility(View.INVISIBLE);
            bt5.setVisibility(View.INVISIBLE);
            bt6.setVisibility(View.INVISIBLE);
            bt7.setVisibility(View.INVISIBLE);
            bt8.setVisibility(View.INVISIBLE);
            bt9.setVisibility(View.INVISIBLE);

        }else{
            bt1.setVisibility(View.VISIBLE);
            bt2.setVisibility(View.VISIBLE);
            bt3.setVisibility(View.VISIBLE);
            bt4.setVisibility(View.VISIBLE);
            bt5.setVisibility(View.VISIBLE);
            bt6.setVisibility(View.VISIBLE);
            bt7.setVisibility(View.VISIBLE);
            bt8.setVisibility(View.VISIBLE);
            bt9.setVisibility(View.VISIBLE);
        }

    }

    public void setValue(int value){

        if (selectedCelCol != -1 && selectedCelLin != -1)
            if (value != 0){
                if (boardComp[selectedCelLin][selectedCelCol] == 0){
                    if (!inAnotationsMode){

                        board[selectedCelLin][selectedCelCol] = value;

                        //Se estiver correto
                        if (Resolve(value, selectedCelCol, selectedCelLin)){

                            players[playerIndex].setPoint(players[playerIndex].getPoint() + 1);
                            tvPoints.setText(""+players[playerIndex].getPoint());

                            players[playerIndex].setTime(players[playerIndex].getTime() + 20000);

                            //Se o valor estiver correto bloqueia-se a célula
                            boardComp[selectedCelLin][selectedCelCol] = value;

                        }

                        //Se tiver ganho
                        if (result()){

                            // calcula o vencedor
                            String nome1 = players[0].getName();
                            int point1 = players[0].getPoint();
                            String nome2 = players[1].getName();
                            int point2 = players[1].getPoint();
                            String output; int vencedor;
                            if(point1 > point2){
                                String[] partsmsg = getContext().getString(R.string.WinMessageM2).split(":");
                                output = partsmsg[0] + nome1 + partsmsg[1];
                                vencedor = 1;
                            }else if (point1 < point2){
                                String[] partsmsg = getContext().getString(R.string.WinMessageM2).split(":");
                                output = partsmsg[0] + nome2 + partsmsg[1];
                                vencedor = 2;
                            }else{ // se forem iguais
                                String userid = tvPlayer.getText().toString();
                                if(userid.equals(nome1)){
                                    String[] partsmsg = getContext().getString(R.string.WinMessageM2).split(":");
                                    output = partsmsg[0] + nome1 + partsmsg[1];
                                    vencedor = 1;
                                } else {
                                    String[] partsmsg = getContext().getString(R.string.WinMessageM2).split(":");
                                    output = partsmsg[0] + nome2 + partsmsg[1];
                                    vencedor = 2;
                                }
                            }

                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle(R.string.finished);
                            builder.setMessage(output)
                                    .setPositiveButton(R.string.thanks, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Intent intent = new Intent(getContext(), MainActivity.class);
                                            getContext().startActivity(intent);
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();

                            // adicionar no sharedpreferences o resultado e organiza
                            updatehistory(vencedor);
                        }

                    }
                    else
                        anotations[value - 1][selectedCelLin][selectedCelCol] = value;
                }
            }

        invalidateButtons();
        invalidate();

    }

    private void updatehistory(int vencedor){

        String points,userid;
        if(vencedor == 1){
            points = Integer.toString(players[0].getPoint());
            userid = players[0].getName();
        }else{
            points = Integer.toString(players[1].getPoint());
            userid = players[1].getName();
        }

        JSONObject obj = new JSONObject();

        String outdesc = getContext().getString(R.string.resultsM2);
        String[] parts2 = outdesc.split(":");
        outdesc = parts2[0] + points + parts2[1];

        try {
            obj.put("nome",userid);
            obj.put("tipo","2P");
            obj.put("desc", outdesc);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        addrestult(obj);

        //prefs.edit().putString("result1",obj.toString()).apply();

    }

    private void addrestult(JSONObject obj){

        SharedPreferences prefs = getContext().getSharedPreferences("results", Context.MODE_PRIVATE);

        if(prefs.contains("result1")){

            if(prefs.contains("result2")){

                if(prefs.contains("result3")){
                    prefs.edit().putString("result3",prefs.getString("result2","null")).apply();
                    prefs.edit().putString("result2",prefs.getString("result1","null")).apply();
                    prefs.edit().putString("result1",obj.toString()).apply();
                    return;
                }else{
                    prefs.edit().putString("result3",prefs.getString("result2","null")).apply();
                    prefs.edit().putString("result2",prefs.getString("result1","null")).apply();
                    prefs.edit().putString("result1",obj.toString()).apply();
                    return;
                }

            }else {
                prefs.edit().putString("result2",prefs.getString("result1","null")).apply();
                prefs.edit().putString("result1",obj.toString()).apply();
                return;
            }

        }else{
            prefs.edit().putString("result1",obj.toString()).apply();
            return;
        }

    }

    public int[][] getBoardComp() {
        return boardComp;
    }

    public void setBoardComp(int[][] boardComp) {
        this.boardComp = boardComp;
    }

    private int getValue(int row, int col){

        try {

            JSONObject json = new JSONObject();
            json.put("board", convert(boardComp));
            String strJson = Sudoku.solve(json.toString(), 1500);
            json = new JSONObject(strJson);

            //Se existir esse valor
            if (json.optInt("result",0) == 1){

                JSONArray arrayJson = json.getJSONArray("board");
                int [][] array = convert(arrayJson);
                return array[row][col];

            }

        }catch (Exception e){

        }

        return -1;

    }

    public void getHint(){

        if (selectedCelLin != -1 && selectedCelCol != -1 && hints > 0){

            if (board[selectedCelLin][selectedCelCol] == 0){

                int value = getValue(selectedCelLin, selectedCelCol);
                if (value != -1){
                    board[selectedCelLin][selectedCelCol] = value;
                    boardComp[selectedCelLin][selectedCelCol] = value;
                    hints--;
                    invalidateButtons();
                    invalidate();
                }

            }

        }

    }

    public void deleteCellValue(){

        //Se o valor não for de origem ou uma hint
        if (boardComp[selectedCelLin][selectedCelCol] == 0)
            //Se uma célula estiver seleccionada
            if (selectedCelCol != -1 && selectedCelLin != -1){

                //Se a célula estiver correta decrementa o valor
                if (Resolve(board[selectedCelLin][selectedCelCol], selectedCelCol, selectedCelLin))
                    players[playerIndex].setPoint(players[playerIndex].getPoint() - 1);

                this.tvPoints.setText("" + players[playerIndex].getPoint());

                board[selectedCelLin][selectedCelCol] = 0;
                boardComp[selectedCelLin][selectedCelCol] = 0;
                invalidate();

            }

    }

    public int getHints() {
        return hints;
    }

    public void setHints(int hints) {
        this.hints = hints;
    }

    public int[][][] getAnotations() {
        return anotations;
    }

    public void setAnotations(int[][][] anotations) {
        this.anotations = anotations;
    }

    public void setAnotationsMode(boolean inAnotationsMode) {
        this.inAnotationsMode = inAnotationsMode;
    }

    public void setSelectedCelLin(int selectedCelLin) {
        this.selectedCelLin = selectedCelLin;
    }

    public void setSelectedCelCol(int selectedCelCol) {
        this.selectedCelCol = selectedCelCol;
    }

    public int getPlayerIndex() {
        return playerIndex;
    }

    public void setPlayerIndex(int playerIndex) {
        this.playerIndex = playerIndex;
    }

    public Player[] getPlayers() {
        return players;
    }

    public void setPlayers(Player[] players) {
        this.players = players;
    }

    //Este método é aplicado para facilitar a reinicialização depois de mudar de orientação
    public void restartGame(){

        countDownTimer.cancel();

        //Restart dos nomes
        tvPlayer.setText(players[playerIndex].getName());
        tvErrors.setText("" + players[playerIndex].getErrors());
        tvPoints.setText("" + players[playerIndex].getPoint());

        //Restart dos Timers
        startTimer();

    }

}
