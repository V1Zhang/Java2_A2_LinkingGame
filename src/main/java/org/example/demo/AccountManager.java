package org.example.demo;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class AccountManager {
    private static final String USER_FILE = "user_data.txt"; // 用户数据文件


    // 内存中的用户数据缓存
    private static final Map<String, User> users = new ConcurrentHashMap<>();
    public AccountManager() {
        ensureFileExists(USER_FILE);
        // 初始化时加载用户数据
        loadUsers();
    }

    private void ensureFileExists(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
                System.out.println("文件已创建: " + filePath);
            } catch (IOException e) {
                System.err.println("无法创建文件: " + filePath);
                e.printStackTrace();
            }
        }
    }

    // 加载用户数据到内存
    private void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String username = parts[0];
                String password = parts[1];
                int highestScore = Integer.parseInt(parts[2]);
                boolean status = parts[3].equals("online");
                ArrayList<String> history_5 = new ArrayList<>();
                if (parts.length == 5) {
                    String history = parts[4];
                    String[] historyParts = history.split(";");
                    history_5 = new ArrayList<>(Arrays.asList(historyParts));
                }
                users.put(username, new User(username, password, highestScore, status, history_5));
                // 这里需要加载历史记录（目前没有实现历史记录存储方式）

            }
        } catch (IOException e) {
            System.err.println("加载用户数据时出错：" + e.getMessage());
        }
    }

    // 保存所有用户数据到文件
    public void saveUser(String username) {
        try {
            // 读取现有文件的内容
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line); // 将文件内容读取到列表中
                }
            } catch (IOException e) {
                System.err.println("读取用户数据时出错：" + e.getMessage());
                return;
            }

            // 更新指定用户名的用户数据
            boolean updated = false;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] parts = line.split(",");
                if (parts[0].equals(username)) {
                    // 找到匹配的用户名，更新对应的用户数据
                    User user = AccountManager.getUserByUsername(username);
                    String updatedLine = user.getUsername() + "," +
                            user.getPassword() + "," +
                            user.getHighestScoreScore() + "," +
                            (user.isLoggedIn() ? "online" : "offline") + "," +
                            String.join(";", user.getGameHistory());
                    lines.set(i, updatedLine); // 更新列表中的对应行
                    updated = true;
                    break;
                }
            }

            if (!updated) {
                System.err.println("找不到指定的用户：" + username);
                return;
            }

            // 将更新后的内容写回文件
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine(); // 写入新的一行
                }
            } catch (IOException e) {
                System.err.println("保存用户数据时出错：" + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("保存用户数据时出错：" + e.getMessage());
        }
    }


    // 用户注册
    public boolean registerUser(String username, String password) {
        if (users.containsKey(username)) {
            return false; // 用户已存在
        }
        User newUser = new User(username, password, 0, false, new ArrayList<>());
        users.put(username, newUser);
        saveUser(username); // 保存到文件
        return true;
    }

    // 用户登录
    public boolean loginUser(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            user.setLoggedIn(true);
            saveUser(username); // 更新用户状态到文件
            return true;
        }
        return false; // 用户不存在或密码错误
    }

    // 用户登出
    public void logoutUser(String username) {
        if (username == null || !users.containsKey(username)) {
            return; // 防止传递 null 或无效用户名
        }
        User user = users.get(username);
        if (user != null) {
            user.setLoggedIn(false);
            saveUser(username); // 更新用户状态到文件
        }
    }

    public static User getUserByUsername(String username) {
        return users.get(username); // 获取指定用户名的User对象
    }

    public List<User> getAllUsersWithScores() {
        List<User> userList = new ArrayList<>(users.values());
        return userList;
    }




}
