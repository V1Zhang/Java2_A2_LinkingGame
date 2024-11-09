package org.example.demo;

import java.util.*;

public class Game {
    private Controller controller;
    // row length
    int row;

    // col length
    int col;

    // board content
    int[][] board;
    private int score = 0;
    public Game(int[][] board, Controller controller){
        this.board = board;
        this.row = board.length;
        this.col = board[0].length;
        this.controller = controller;
    }
    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void increaseScore(int basePoints, long reactionTime, int turns) {
        int reactionBonus = calculateReactionBonus(reactionTime);
        int directnessBonus = calculateDirectnessBonus(turns);
        int totalPoints = basePoints + reactionBonus + directnessBonus;
        score += totalPoints;
        controller.updateScore(score);
    }
    // Calculate bonus points based on reaction time: faster responses get more points
    private int calculateReactionBonus(long reactionTime) {
        if (reactionTime < 1000) {  // Less than 1 second
            return 10;
        } else if (reactionTime < 2000) {  // 1 to 2 seconds
            return 5;
        } else if (reactionTime < 3000) {  // 2 to 3 seconds
            return 2;
        } else {
            return 0;
        }
    }

    // Calculate bonus points based on directness: fewer turns yield more points
    private int calculateDirectnessBonus(int turns) {
        if (turns == 1) {  // Matched in the first turn
            return 10;
        } else if (turns <= 3) {  // Matched within three turns
            return 5;
        } else if (turns <= 5) {  // Matched within five turns
            return 2;
        } else {
            return 0;
        }
    }

    public int getScore() {
        return score;
    }
    // randomly initialize the game board
    public static int[][] SetupBoard(int row, int col) {
        // TODO: randomly initialize board
        int[][] board = new int[row+2][col+2];
        List<Integer> values = new ArrayList<>();
        // 准备成对的元素
        for (int i = 1; i <= (row * col) / 2; i++) {
            values.add(i);
            values.add(i);
        }
        Collections.shuffle(values);
        int index = 0;
        for (int i = 1; i <= row; i++) {
            for (int j = 1; j <= col; j++) {
                board[i][j] = values.get(index++);
            }
        }
        return board;
    }


    // judge the validity of an operation
    public boolean judge(int row1, int col1, int row2, int col2){
        if ((board[row1][col1] != board[row2][col2]) || (row1 == row2 && col1 == col2)) {
            return false;
        }

        // one line
        if (isDirectlyConnected(row1, col1, row2, col2, board)) {
            return true;
        }

        // two lines
        if((row1 != row2) && (col1 != col2)){
            if(board[row1][col2] == 0 && isDirectlyConnected(row1, col1, row1, col2, board)
            && isDirectlyConnected(row1, col2, row2, col2, board))
                return true;
            if(board[row2][col1] == 0 && isDirectlyConnected(row2, col2, row2, col1, board)
            && isDirectlyConnected(row2, col1, row1, col1, board))
                return true;
        }

        // three lines
        if(row1 != row2)
            for (int i = 0; i < board[0].length; i++) {
                if (board[row1][i] == 0 && board[row2][i] == 0 &&
                        isDirectlyConnected(row1, col1, row1, i, board) && isDirectlyConnected(row1, i, row2, i, board)
                        && isDirectlyConnected(row2, col2, row2, i, board)){
                    return true;
                }
            }
        if(col1 != col2)
            for (int j = 0; j < board.length; j++){
                if (board[j][col1] == 0 && board[j][col2] == 0 &&
                        isDirectlyConnected(row1, col1, j, col1, board) && isDirectlyConnected(j, col1, j, col2, board)
                        && isDirectlyConnected(row2, col2, j, col2, board)){
                    return true;
                }
            }

        return false;
    }
    public void deleteGrid(int row, int col) {
        if (row < 0 || row >= board.length || col < 0 || col >= board[0].length) {
            System.out.println("Invalid grid position.");
            return;
        }
        board[row][col] = 0;
    }

    // judge whether
    private boolean isDirectlyConnected(int row1, int col1, int row2, int col2, int[][] board) {
        if (row1 == row2) {
            int minCol = Math.min(col1, col2);
            int maxCol = Math.max(col1, col2);
            for (int col = minCol + 1; col < maxCol; col++) {
                if (board[row1][col] != 0) {
                    return false;
                }
            }
            return true;
        } else if (col1 == col2) {
            int minRow = Math.min(row1, row2);
            int maxRow = Math.max(row1, row2);
            for (int row = minRow + 1; row < maxRow; row++) {
                if (board[row][col1] != 0) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
