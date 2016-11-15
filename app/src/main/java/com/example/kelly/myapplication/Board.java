package com.example.kelly.myapplication;


import java.util.ArrayList;

public class Board {//棋盘
    private int numRows;
    private int numCols;
    public boolean hasWinner;
    public Cell[][] cells;

    public class Cell {
        public boolean empty;//棋子对应位置所在的状态
        public Board.Turn player;

        public Cell() {
            empty = true;
        }//当前没被占有

        public void setPlayer(Board.Turn player) {
            this.player = player;
            empty = false;//棋子已经被用了
        }
    }

    //site to record win cell.记录赢得棋子
    public class Site {
        int row;
        int col;
    }
    ArrayList<Site> winCellSite;
    ArrayList<ArrayList<Site>> winCellSiteSet;//会有多种情况 一横一竖 放到每一次检测的结果,并单独显示

    //-test
    String testStr;

    public enum Turn {
        FIRST, SECOND
    }

    public Turn turn;

    public Board(int rows, int cols) {
        numRows = rows;
        numCols = cols;
        cells = new Cell[rows][cols];
        winCellSite = new ArrayList<Site>();
        winCellSiteSet = new ArrayList<ArrayList<Site>>();
        testStr = new String();
        reset();
    }

    public void reset() {
        winCellSite.clear();
        winCellSiteSet.clear();
        hasWinner = false;
        turn = Turn.FIRST;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                cells[row][col] = new Cell();
            }
        }
    }

    public int lastAvailableRow(int col) {//控制棋子那一行没有棋子,遍历竖的,看到那里空就返回
        for (int row = numRows - 1; row >= 0; row--) {
            if (cells[row][col].empty) {
                return row;
            }
        }
        return -1;
    }

    public void occupyCell(int row, int col) {
        cells[row][col].setPlayer(turn);
    }

    //Clear the given Cell
    public void clearCell(int row, int col){
        cells[row][col].empty = true;
    }

    public void toggleTurn() {
        if (turn == Turn.FIRST) {
            turn = Turn.SECOND;
        } else {
            turn = Turn.FIRST;
        }
    }

    //后面两个修改需要重新，maybe
    public boolean checkForWin() {//放一次检测一次
        winCellSite.clear();
        winCellSiteSet.clear();

        for (int col = 0; col < numCols; ++col){
            isContiguous(turn, 0, 1, col, 0, 0);//从左到右往下遍历,直着
            isContiguous(turn, 1, 1, col, 0, 0);//对角线方向,行在前,列在后。横着找,斜着向下
            isContiguous(turn, -1, 1, col, 0, 0);//斜着左边
        }

        for (int row = 0; row < numRows; ++row){//147
            isContiguous(turn, 1, 0, 0, row, 0);//横着
            isContiguous(turn, 1, 1, 0, row, 0);//斜向下159 48
            isContiguous(turn, -1, 1, numCols - 1, row, 0);//斜向下 左边的方向 369,最后一列找 357 68
        }
        hasWinner = !winCellSiteSet.isEmpty();//为空返回false
        return !winCellSiteSet.isEmpty();


    }

    private boolean isContiguous(Turn player, int dirX, int dirY, int col, int row, int count) {
        if (col < 0 || col >= numCols || row < 0 || row >= numRows) {
            if (count >= 4){
                winCellSiteSet.add((ArrayList<Site>)winCellSite.clone());//所有赢得状态的集合
                winCellSite.clear();
                return true;
            }
            if (!winCellSite.isEmpty()){//清空状态
                //-test
                testStr = "";
                winCellSite.clear();
            }
            return false;
        }
        Cell cell = cells[row][col];
        if ((!cell.empty) && cell.player == player) {
            Site site = new Site();
            site.row = row;
            site.col = col;
            winCellSite.add(site);//加到这里

            //-test
            Integer Row = row, Col = col;
            testStr += "add:(".toString() + Row.toString() + ",".toString() + Col.toString() + ")\n".toString();

            return isContiguous(player, dirX, dirY, col + dirX, row + dirY, count + 1);
        } else {
            if (count >= 4){
                winCellSiteSet.add((ArrayList<Site>)winCellSite.clone());
                winCellSite.clear();
                return true;
            }
            if (!winCellSite.isEmpty()){
                //-test
                testStr = "";
                winCellSite.clear();
            }
            return isContiguous(player, dirX, dirY, col + dirX, row + dirY, 0);//下一步,从计数零开始
        }
    }
}


