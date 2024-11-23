package org.example.demo;

import java.util.List;
import java.util.stream.Collectors;

public class AccountController {
    AccountManager accountManager = new AccountManager();

    public void handleRegister(String username, String password) {
        if (accountManager.registerUser(username, password)) {
            System.out.println("注册成功！");
        } else {
            System.out.println("用户名已存在！");
        }
    }

    public void handleLogin(String username, String password) {
        if (accountManager.loginUser(username, password)) {
            System.out.println("登录成功！");
        } else {
            System.out.println("用户名或密码错误！");
        }
    }

    public List<String> getUserRankings() {
        List<User> users = accountManager.getAllUsersWithScores();  // 获取所有用户和分数
        return users.stream()
                .sorted((u1, u2) -> Integer.compare(u2.getHighestScoreScore(), u1.getHighestScoreScore()))  // 按最高分数降序排列
                .map(user -> user.getUsername() + ": " + user.getHighestScoreScore() + " " + user.isLoggedIn())  // 转换为 "用户名: 分数" 的格式
                .collect(Collectors.toList());
    }


}
