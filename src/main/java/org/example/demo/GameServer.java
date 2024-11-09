package org.example.demo;

import java.io.*;
import java.net.*;
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
        private PrintWriter out;
        private BufferedReader in;
        private GameSession session;

        public PlayerHandler(Socket socket) {
            this.socket = socket;
            this.playerId = UUID.randomUUID();
        }

        @Override
        public void run() {
            try {
                setupStreams();
                listenForMessages();
            } catch (IOException e) {
                System.out.println("与玩家的连接错误。");
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

        private void listenForMessages() throws IOException {
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("BOARD_SIZE")) {
                    String[] parts = message.split(" ");
                    int size = Integer.parseInt(parts[1]);
                    waitOrCreateGame(size);
                } else if (session != null) {
                    session.processMessage(this, message);
                }
            }
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

        private void closeConnection() {
            try {
                if (session != null) {
                    session.endGame(this);
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }

    // 管理一个游戏会话
    static class GameSession {
        private final int gameId;
        private final PlayerHandler player1;
        private final PlayerHandler player2;
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

        public void startGame() {
            player1.sendMessage("MATCHED " + player2 + " " + board.length);
            player2.sendMessage("MATCHED " + player1 + " " + board.length);
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
            System.out.println(sender);
            System.out.println(message);
            if (!gameActive) return;
            if (message.startsWith("Link")) {
                String[] parts = message.split(" ");
                int row1 = Integer.parseInt(parts[1]);
                int col1 = Integer.parseInt(parts[2]);
                int row2 = Integer.parseInt(parts[3]);
                int col2 = Integer.parseInt(parts[4]);

                board[row1][col1] = 0;
                board[row2][col2] = 0;
                switchPlayer();
                sendBoardToPlayers(); // 将更新后的棋盘发送给双方
                checkGameStatus();
            }
        }

//        private void handleLink(PlayerHandler sender, String message) {
//
//            if (!sender.equals(currentPlayer)) {
//                sender.sendMessage("不是你的回合！");
//                return;
//            }
//
//            String[] parts = message.split(" ");
//            int row1 = Integer.parseInt(parts[1]);
//            int col1 = Integer.parseInt(parts[2]);
//            int row2 = Integer.parseInt(parts[3]);
//            int col2 = Integer.parseInt(parts[4]);
//
//            if (board[row1][col1] == board[row2][col2] && board[row1][col1] != 0) {
//                board[row1][col1] = 0;
//                board[row2][col2] = 0;
//                sender.sendMessage("MATCH " + row1 + " " + col1 + " " + row2 + " " + col2);
//                System.out.println(currentPlayer);
//                switchPlayer();
//                System.out.println(currentPlayer);
//                sendBoardToPlayers(); // 将更新后的棋盘发送给双方
//                checkGameStatus();
//            } else {
//                sender.sendMessage("NO_MATCH " + row1 + " " + col1 + " " + row2 + " " + col2);
//                switchPlayer();
//                sendBoardToPlayers(); // 将无效匹配后的棋盘发送给双方
//            }
//        }

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
                player1.sendMessage("GAME_WON");
                player2.sendMessage("GAME_WON");
                gameActive = false;
                endGame(null);
            }
        }

        public void endGame(PlayerHandler disconnectedPlayer) {
            if (gameActive && disconnectedPlayer != null) {
                PlayerHandler opponent = (disconnectedPlayer == player1) ? player2 : player1;
                opponent.sendMessage("对手断开连接，游戏结束。");
            }
            gameActive = false;
            activeGames.remove(gameId);
        }
    }
}
