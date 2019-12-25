package com.example.sudoku;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import pt.isec.ans.sudokulibrary.Sudoku;

public class SudokuView extends View {

    public static final int BOARD_SIZE = 9;
    private boolean inAnotationsMode;
    Paint paintMainLines, paintSubLines, paintMainNumbers, paintSmallNumbers;

    int [][] board = new int[BOARD_SIZE][BOARD_SIZE];
    int [][] boardComp = new int[BOARD_SIZE][BOARD_SIZE];
    private int [][][]anotations = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
    private int selectedCelLin = -1;
    private int selectedCelCol = -1;
    private int points = 0;
    private int errors = 0;
    private int hints = 3;

    private TextView tvErrors, tvPoints;
    private Button bt1, bt2, bt3, bt4, bt5, bt6, bt7, bt8, bt9;

    public SudokuView(Context context, Button bt1, Button bt2, Button bt3, Button bt4, Button bt5,
                      Button bt6, Button bt7, Button bt8, Button bt9, TextView tvErrors,
                      TextView tvPoints) {
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
        createPaints();
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
                        errors++;
                        tvErrors.setText(""+errors);

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

                            points++;
                            tvPoints.setText(""+points);

                        }

                        //Se tiver ganho
                        if (result()){

                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle(R.string.finished);
                            builder.setMessage(R.string.WinMessage)
                                    .setPositiveButton(R.string.thanks, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Intent intent = new Intent(getContext(), MainActivity.class);
                                            getContext().startActivity(intent);
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();

                        }

                    }
                    else
                        anotations[value - 1][selectedCelLin][selectedCelCol] = value;
                }
            }

        invalidateButtons();
        invalidate();

    }

    public int[][] getBoardComp() {
        return boardComp;
    }

    public void setBoardComp(int[][] boardComp) {
        this.boardComp = boardComp;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
        this.tvErrors.setText(""+errors);
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
                    points--;

                this.tvPoints.setText("" + points);

                board[selectedCelLin][selectedCelCol] = 0;
                boardComp[selectedCelLin][selectedCelCol] = 0;
                invalidate();

            }

    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
        this.tvPoints.setText(""+points);
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
}
