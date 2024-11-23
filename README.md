# CS209A Assignment 2 Demo

## Environment

**java JDK**: openjdk-22 (Oracle OpenJDK 22.0.2)

**javafx-fxml**: 22.0.1

**javafx-controls**: 22.0.1

**maven**: 3.8.5

## File List

**Application.java**: the main entry point of the demo application

**Game.java**: manages the game logic and controls the game's behavior

**Controller.java**: handles JavaFX UI interactions and events

**board.fxml**: a game board prototype

**resources**: stores pictures for the game board (https://www.iconfont.cn/)

## Logic

- game start: allowing the user to select options and set up the game board

- operations validity: monitoring user actions, validating operations, and updating the board

- game finish: informing the user that the game has ended

## Notes

I suggest that you first complete the single-player mode. If you feel confident, you can directly reconstruct this project to include a two-player mode.

If you encounter any GUI issues while rendering multiple game boards, maybe you can check the $start$ method in the main entry point.

If you have any questions or find any bugs, feel free to contact me 12442018@mail.sustech.edu.cn or QQ:503652093 :)



## 评分标准

|                  |                             要求                             | 分值 |                          [我的实现]                          |
| :--------------: | :----------------------------------------------------------: | :--: | :----------------------------------------------------------: |
| GUI/游戏逻辑部分 |     棋盘图标是否随机、成对出现，是否可以自定义棋盘大小。     |  5   |                                                              |
|                  | 连线是否符合不超过三条直线，游戏过程中选择格子能够显示，连线能够显示，连线成功线和格子消失、连线失败提示。 |  5   | 如果连续两次点击的是同一个格子不算 如果点击的是空白位置不算  |
|                  | 是否有计分板，并及时更新得分。得分规则是否符合逻辑（可自行设计），游戏结束结果的展示。 |  5   |                      根据时间和折数得分                      |
|                  |        是否严格使用javafx，若使用其他框架，GUI部分0分        |      |                                                              |
|   服务端客户端   | 服务器接收到一个用户的双人游戏申请，将其加入等待队列，服务器通知给客户端，客户端能收到信息，界面有明确提示进入等待状态的标识。 |  10  |                                                              |
|                  | 服务器接收到另一个用户的双人游戏申请，将两个玩家成功匹配，通知给两个客户端，两个客户端收到成功匹配信息，并在界面有明确提示进入匹配状态。 |  10  |                                                              |
|                  | 服务器能将随机生成的棋盘同步给两个客户端，客户端界面上能正确呈现。服务器将初始棋盘发给客户端前应先检查棋盘是否存在连线。 |  5   |                                                              |
|                  | 偶数个客户端（4个,6个等）发起双人游戏申请，服务器收到多个客户端游戏申请，两两配对，偶数个客户端显示收到成功匹配信息。 |  5   |                                                              |
|                  | 奇数个客户端（5个,7个等）发起双人游戏申请，服务器收到多个客户端游戏申请，两两配对，偶数个客户端提示收到成功匹配信息，剩余一个显示等待状态。此时再启动一个客户端发起双人游戏申请，能匹配成功进入匹配状态。 |  5   |                                                              |
|                  | 服务器为每对客户端随机生成有效的棋盘并同步给相应的客户端，所有客户端的界面都应该要能正确呈现。服务器可以在一对客户端中随机挑一个作为先手方，也可自行设计。棋盘大小可由管理员设置或者随机挑一个客户端传来的设置（自行进行合理的设计） |  5   | 先发送开始请求的一方是先手方；棋盘大小是只有相同的才能匹配上 |
|                  | 当前客户端点击两个图标，连接有效时，界面应该正确呈现连线、图标、计分等的变化，发消息给服务器，服务器跟踪结果，更新游戏状态（比如得分等），并且处理好与对手客户端的同步操作，比如对手客户端界面是否更新，是否可以开始操作等，将控制权交给对手客户端。 |  10  |                                                              |
|                  | 当前客户端点击两个图标，连接无效时，界面正确反馈无效连接，发消息给服务器，服务器做好同步处理，并将控制权交接给另一客户端。注意：每个玩家只有一次机会，落子无悔。 |  10  |                                                              |
|                  | 两个玩家在各自的回合进行连线操作，直至游戏结束。服务端应正确判断游戏结束，并将结果正确通知到双方客户端。双方客户端的界面正确呈现游戏结果。 |  10  |                                                              |
|     异常处理     |   服务器异常关闭时，客户端们能检测到，并以合理的方式处理。   |  5   |                                                              |
|                  | 一个客户端在游戏进行时异常关闭时，服务器能通知到另外一个客户端，并用合理的方式处理。 |  4   |   对方弹出消息提示对手关闭连接 并且对方直接胜利回到主页面    |
|                  | 客户端在等待匹配的过程中异常关闭，服务器能以合理的方式处理。 |  4   |       该用户会从等待队列出去，重新再开两个会自动匹配上       |
|                  |  当2个客户端都异常关闭，服务器应该解除该会话并记录log信息。  |  2   |                                                              |
|      Bonus       | 账号管理系统，要求用户可以注册和登录账号，并查看自己和他人的游戏历史、是否在线等信息。 |  5   |                                                              |
|                  | 连接服务器后，玩家可以看到当前等待匹配的玩家列表。玩家可以从玩家列表中选择对手开始新游戏。 |  5   |                                                              |
|                  | 玩家在游戏中断后，可以**重新连接到服务器**并从之前的游戏状态和之前的对手继续游戏。 |  5   |                                                              |
