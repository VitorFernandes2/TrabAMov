package com.example.sudoku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SudokuView extends View {

    public static final int BOARD_SIZE = 9;
    Paint paintMainLines, paintSubLines, paintMainNumbers, paintSmallNumbers;

    int [][] board = {
            {0, 1, 0,  2, 0, 3,  0, 4, 0},
            {5, 0, 6,  0, 7, 0,  8, 0, 9},
            {0, 1, 0,  2, 0, 3,  0, 4, 0},

            {5, 0, 6,  0, 7, 0,  8, 0, 9},
            {0, 1, 0,  2, 0, 3,  0, 4, 0},
            {5, 0, 6,  0, 7, 0,  8, 0, 9},

            {0, 1, 0,  2, 0, 3,  0, 4, 0},
            {5, 0, 6,  0, 7, 0,  8, 0, 9},
            {0, 1, 0,  2, 0, 3,  0, 4, 0}
    };
    private int value;

    public SudokuView(Context context) {
        super(context);
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
                    canvas.drawText(""+n, x, y, paintMainNumbers);

                } else {

                    List<Integer> possibilities = Arrays.asList(1,2,3,4,5,6,7,8,9);
                    Collections.shuffle(possibilities);
                    Random rnd = new Random(SystemClock.elapsedRealtime());
                    possibilities = possibilities.subList(0, rnd.nextInt(6) + 1);

                    int x = cellW / 6 + cellW * c;
                    int y = cellH / 6 + cellH * r;

                    for (int p = 0; p <= BOARD_SIZE; p++) {

                        if (possibilities.contains(p)){

                            int xp = x + (p - 1)%3 * cellW / 3;
                            int yp = y + (p - 1)/3 * cellH / 3 + cellH / 9;
                            canvas.drawText(""+p, xp, yp, paintSmallNumbers);

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

            board[cellY][cellX] = value;
            invalidate();

            return true;

        }

        return super.onTouchEvent(event);

    }

    public void setBoard(int[][] board){
        this.board = board;
        invalidate();
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

}
