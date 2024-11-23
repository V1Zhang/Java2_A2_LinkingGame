package org.example.demo;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class GameServer {

    private static final int PORT = 12345; // 服务器端口
    private static final Map<Integer, GameSession> activeGames = new ConcurrentHashMap<>();
    private static final Map<Integer, Queue<PlayerHandler>> waitingQueues = new ConcurrentHashMap<>(); // 每种棋盘大小的等待队列
    private static int gameIdCounter = 1;

    public static void main(String[] args) {
        ExecutorService executor = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("游戏服务器启动...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("新玩家连接。");
                PlayerHandler player = new PlayerHandler(clientSocket);
                executor.execute(player);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

    // 管理单个玩家的连接和通信
    static class PlayerHandler implements Runnable {
        private final Socket socket;
        private final UUID playerId;
        private String username;
        private int score;
        private PrintWriter out;
        private BufferedReader in;
        private GameSession session;
        private static final AccountController accountController = new AccountController();
        public PlayerHandler(Socket socket) {
            this.socket = socket;
            this.playerId = UUID.randomUUID();
        }
        public String getUsername() {
            return username;
        }
        @Override
        public void run() {
            try {
                setupStreams();
                listenForMessages();
            } catch (IOException e) {
                //
            } finally {
                closeConnection();
            }
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            PlayerHandler that = (PlayerHandler) obj;
            return Objects.equals(playerId, that.playerId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(playerId);
        }
        private void setupStreams() throws IOException {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("欢迎来到游戏！");
        }
        private void handleLoginOrRegister(String message) {
            System.out.println(message);
            if (message.startsWith("REGISTER ")) {
                String[] parts = message.split(" ");
                String username = parts[1];
                String password = parts[2];
                accountController.handleRegister(username, password);
                out.println("注册成功！请登录！");
            } else if (message.startsWith("LOGIN ")) {
                String[] parts = message.split(" ");
                if (parts.length == 3) {
                    String username = parts[1];
                    String password = parts[2];
                    if (accountController.accountManager.loginUser(username, password)) {
                        this.username = username;
                        out.println("SUCCESS_LOGIN 登录成功！欢迎 " + username);
                    } else {
                        out.println("FAIL_LOGIN 登录失败，用户名或密码错误！");
                    }
                } else {
                    out.println("登录格式错误，请使用：LOGIN <用户名> <密码>");
                }
            }else {
                out.println("请先登录或注册！");
            }
        }
        private void handleClientDisconnection() {
            try {
                // 如果玩家正在游戏中
                if (session != null) {
//                    GameSession currentSession = session;
                    // 保存游戏进度
//                    saveGameState(currentSession);
                    session.waitingToReconnect(this);

                    // 通知游戏会话，玩家断开连接
                    // session.endGame(this);
                    session.gameActive=false;

                } else {
                    // 玩家在等待队列中
                    removeFromWaitingQueue();
                }
                accountController.accountManager.logoutUser(username);
                closeConnection(); // 关闭连接
                System.out.println("玩家 " + username + " 已断开连接。");


            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        private void listenForMessages() throws IOException {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(message);
                if (message.startsWith("REGISTER ")) {
                    // 处理注册请求
                    handleLoginOrRegister(message);
                }else if (message.startsWith("LOGIN ")) {
                    // 处理登录请求
                    handleLoginOrRegister(message);
                }else if (message.startsWith("RECORD ")) {
                    String[] parts = message.split(" ");
                    String opponent = parts[1];
                    String status = parts[2];
                    int newScore = Integer.parseInt(parts[3]);
                    User user = AccountManager.getUserByUsername(username);
                    user.setHighestScore(newScore);
                    LocalDateTime now = LocalDateTime.now();
                    String formattedDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    user.addGameHistory(formattedDateTime + " 对手: " + opponent + " | 结果: " + status + " | 得分: " + newScore);
                    accountController.accountManager.saveUser(username);
                }else if (message.startsWith("LOGOUT ")) {
                    // 处理登出请求
                    accountController.accountManager.logoutUser(username);
                } else if (message.startsWith("SELECT_OPPONENT ")) {
                    String[] waitingUserInfo = message.substring("SELECT_OPPONENT ".length()).split(",");
                    String username = waitingUserInfo[1].split(":")[1];
                    System.out.println(username);
                    PlayerHandler foundPlayer = null;
                    int boardSize = 0;
                    for (Map.Entry<Integer, Queue<PlayerHandler>> entry : waitingQueues.entrySet()) {
                        boardSize = entry.getKey();
                        Queue<PlayerHandler> queue = entry.getValue();  // 获取当前队列
                        // 遍历队列，找到与用户名匹配的 PlayerHandler
                        for (PlayerHandler player : queue) {
                            System.out.println(player.username);
                            if (player.username.equals(username)) {
                                foundPlayer = player;  // 找到玩家
                                break;
                            }
                        }
                    }
                    PlayerHandler player1 = foundPlayer;
                    PlayerHandler player2 = this;
                    startGameWith(player1, player2, boardSize);
                }else if (message.equals("VIEW_PLAYERS")){
                    if (waitingQueues.isEmpty()) {
                        out.println("当前没有等待的玩家。");
                        return;
                    }
                    StringBuilder playerList = new StringBuilder();
                    for (Map.Entry<Integer, Queue<PlayerHandler>> entry : waitingQueues.entrySet()) {
                        Integer boardSize = entry.getKey();
                        Queue<PlayerHandler> queue = entry.getValue();  // 当前棋盘大小下的等待队列
                        for (PlayerHandler player : queue) {
                            playerList.append("Board Size:").append(boardSize).append(",Username:").append(player.username).append(";");
                        }
                    }
                    // 将等待队列中的玩家信息发送回客户端
                    out.println("WAITING_QUEUE " + playerList);
                }else if (message.startsWith("BOARD_SIZE ")) {
                    String[] parts = message.split(" ");
                    int size = Integer.parseInt(parts[1]);
                    waitOrCreateGame(size);
                } else if (message.startsWith("DISCONNECT ")){
                    handleClientDisconnection();
                } else if (message.startsWith("RECONNECT ")) {
                    String[] parts = message.split(" ");
                    String username = parts[1];
                    this.username = username;
                    // 检查是否有未完成的游戏会话
                    GameSession session = findSessionByUsername(username);
                    if (session != null) {
                        session.gameActive=true;
                        session.replacePlayerHandler(this); // 替换 PlayerHandler
                        PlayerHandler opponent = (session.getPlayer1().equals(this)) ? session.getPlayer2() : session.getPlayer1();
                        if (opponent != null && opponent.socket != null && !opponent.socket.isClosed()) {
                            session.startGame();
                            out.println("OLD_GAME "+this.score);
                        }
                    } else {
                        out.println("NEW_GAME");
                    }
                }
                else if (session != null) {
                    session.processMessage(this, message);
                }

            }
        }
        private GameSession findSessionByUsername(String username) {
            for (GameSession session : activeGames.values()) {
                if (session.getPlayer1().getUsername().equals(username) ||
                        session.getPlayer2().getUsername().equals(username)) {
                    return session;
                }
            }
            return null;
        }

        private void waitOrCreateGame(int boardSize) {
            waitingQueues.putIfAbsent(boardSize, new LinkedList<>()); // 确保有队列存在
            Queue<PlayerHandler> queue = waitingQueues.get(boardSize);

            synchronized (queue) {
                if (queue.isEmpty()) {
                    queue.add(this);
                    out.println("等待另一位玩家...");
                } else {
                    PlayerHandler player1 = queue.poll();
                    PlayerHandler player2 = this;
                    startGameWith(player1, player2, boardSize);
                }
            }
        }

        private void startGameWith(PlayerHandler player1, PlayerHandler player2, int boardSize) {
            int[][] sharedBoard = Game.SetupBoard(boardSize, boardSize); // 生成一次共享棋盘
            GameSession session = new GameSession(gameIdCounter++, player1, player2, sharedBoard);

            player1.session = session;
            player2.session = session;
            activeGames.put(session.getGameId(), session);

            session.startGame();
        }

        void closeConnection() {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (!socket.isClosed()) {
                    socket.close();
                }
                removeFromWaitingQueue(); // 从等待队列中移除
                session = null; // 清空游戏会话引用
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void removeFromWaitingQueue() {
            for (Queue<PlayerHandler> queue : waitingQueues.values()) {
                synchronized (queue) {
                    queue.remove(this);
                }
            }
        }



        public void sendMessage(String message) {
            out.println(message);
        }


    }

    // 管理一个游戏会话
    static class GameSession {
        private final int gameId;
        private PlayerHandler player1;
        private PlayerHandler player2;
        private final int[][] board;
        private PlayerHandler currentPlayer;
        private boolean gameActive = true;

        public GameSession(int gameId, PlayerHandler player1, PlayerHandler player2, int[][] sharedBoard) {
            this.gameId = gameId;
            this.player1 = player1;
            this.player2 = player2;
            this.currentPlayer = player1;
            this.board = sharedBoard; // 使用生成的共享棋盘
        }

        public int getGameId() {
            return gameId;
        }
        public PlayerHandler getPlayer1() {
            return player1;
        }
        public PlayerHandler getPlayer2() {
            return player2;
        }
        public int[][]  getSharedBoard() {
            return board;
        }

        public void startGame() {
            player1.sendMessage("MATCHED " + player2.username + " " + (board.length - 2));
            player2.sendMessage("MATCHED " + player1.username + " " + (board.length - 2));
            player1.sendMessage("游戏开始！你是玩家 1。");
            player2.sendMessage("游戏开始！你是玩家 2。");
            sendBoardToPlayers();
            currentPlayer.sendMessage("YOUR_TURN");
        }

        private void sendBoardToPlayers() {
            StringBuilder boardString = new StringBuilder("BOARD "+Arrays.deepToString(board));

            player1.sendMessage(boardString.toString());
            player2.sendMessage(boardString.toString());
        }

        public void processMessage(PlayerHandler sender, String message) {
            System.out.println(message);
            if (!gameActive) return;
            if (message.startsWith("LINK_SUCCESS")) {
                String[] parts = message.split(" ");
                int row1 = Integer.parseInt(parts[1]);
                int col1 = Integer.parseInt(parts[2]);
                int row2 = Integer.parseInt(parts[3]);
                int col2 = Integer.parseInt(parts[4]);
                this.currentPlayer.score = Integer.parseInt(parts[5]);
                board[row1][col1] = 0;
                board[row2][col2] = 0;
                sendBoardToPlayers(); // 将更新后的棋盘发送给双方
                checkGameStatus();
            }
            if(gameActive) {
                switchPlayer();
            }
        }


        private void switchPlayer() {
            currentPlayer.sendMessage("NOT_YOUR_TURN");
            currentPlayer = (currentPlayer.equals(player1)) ? player2 : player1;
            currentPlayer.sendMessage("YOUR_TURN");
        }

        private void checkGameStatus() {
            boolean allCleared = Arrays.stream(board)
                    .flatMapToInt(Arrays::stream)
                    .allMatch(value -> value == 0);
            if (allCleared) {
                endGame(null);
            }
        }
        private void addToWaitingQueue(PlayerHandler player, int boardSize) {
            waitingQueues.putIfAbsent(boardSize, new LinkedList<>());
            Queue<PlayerHandler> queue = waitingQueues.get(boardSize);

            synchronized (queue) {
                if (queue.isEmpty()) {
                    queue.add(player);
                    player.sendMessage("对手已断开连接，您已回到等待队列。");
                } else {
                    PlayerHandler matchedPlayer = queue.poll();
                    if (player.session == null && matchedPlayer.session == null) {
                        player.startGameWith(player, matchedPlayer, boardSize);
                    }
                }
            }
        }

        public void waitingToReconnect(PlayerHandler disconnectedPlayer){
            // 设置一定的超时时间允许重连
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!gameActive) {
                        PlayerHandler waitingPlayer = (currentPlayer.equals(player1)) ? player2 : player1;
                        // 如果未在超时时间内重连，则真正结束游戏
                        waitingPlayer.sendMessage("GAME_WIN opponent: "+disconnectedPlayer.username);
                        activeGames.remove(gameId);
                        endGame(disconnectedPlayer);
                    }
                }
            }, 30000); // 设置 30 秒超时时间
        }


        public void endGame(PlayerHandler disconnectedPlayer) {
            activeGames.remove(gameId);
            System.out.println("会话 " + gameId + " 结束。");
            System.out.println("游戏已结束");

//            if (gameActive) {
                if (disconnectedPlayer != null) {
                    // 情况 1：一方断开连接
                    PlayerHandler opponent = (disconnectedPlayer == player1) ? player2 : player1;
                    opponent.sendMessage("OPPONENT_DISCONNECTED opponent: "+opponent);
                    opponent.sendMessage("对手断开连接，游戏结束。");
                    int boardSize = board.length;
                    addToWaitingQueue(opponent, boardSize);

//                    disconnectedPlayer.closeConnection();
                    // 将玩家重新放到等待队列里
                    // 如果有可以匹配的 开始新局
                    // 如果没有匹配的 玩家的页面重新回到等待页面

                } else {
                    // 情况 2：游戏正常结束，处理胜负
                    PlayerHandler winner = (currentPlayer.equals(player1)) ? player1 : player2;
                    PlayerHandler loser = (currentPlayer.equals(player1)) ? player2 : player1;

                    // 向获胜者和失败者发送不同的消息
                    winner.sendMessage("GAME_WIN opponent: "+loser.username); // 获胜玩家接收 "获胜" 消息
                    loser.sendMessage("GAME_LOSE opponent: "+winner.username); // 失败玩家接收 "失败" 消息
                }

                // 设置游戏为非活动状态
                gameActive = false;

//            }
        }

        public void replacePlayerHandler(PlayerHandler newPlayer) {
            if (player1.getUsername().equals(newPlayer.getUsername())) {
                newPlayer.score = player1.score;
                player1 = newPlayer;
            } else if (player2.getUsername().equals(newPlayer.getUsername())) {
                newPlayer.score = player2.score;
                player2 = newPlayer;
            }
            newPlayer.session = this; // 更新新 PlayerHandler 的会话引用
        }



    }
}
