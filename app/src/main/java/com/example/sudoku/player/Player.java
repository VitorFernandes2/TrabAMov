package com.example.sudoku.player;

public class Player {

    private String name;
    private int point;
    private int errors;
    private long time;

    public Player(String name, int point, int errors, long time) {
        this.name = name;
        this.point = point;
        this.errors = errors;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}
