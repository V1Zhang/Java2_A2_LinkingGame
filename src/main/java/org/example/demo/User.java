package org.example.demo;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;
    private boolean isLoggedIn;
    private int highestScore;
    private List<String> gameHistory; // 游戏历史记录

    public User(String username, String password, int highestScoreScore, boolean status, List<String> gameHistory) {
        this.username = username;
        this.password = password;
        this.highestScore = highestScoreScore;
        this.isLoggedIn = status; // 初始状态未登录
        this.gameHistory = gameHistory;
    }

    // Getters 和 Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public int getHighestScoreScore() {
        return highestScore;
    }
    public void setHighestScore(int highestScore) {
        if (highestScore > this.highestScore) {
            this.highestScore = highestScore;
        }
    }
    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }
    public List<String> getGameHistory() {
        return gameHistory;
    }
    public void addGameHistory(String record) {
        if (gameHistory.size() >= 5) {
            gameHistory.remove(0); // 如果超过5条记录，移除最旧的记录
        }
        this.gameHistory.add(record); // 添加新的游戏记录
    }

}
