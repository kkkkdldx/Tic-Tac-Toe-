

**Client-Server Architecture:**

 The server (`TicTacToeServer`) handles multiple game sessions concurrently, listening for player connections and managing the game state. On the client side, there are two implementations (`TicTacToeClient` and `TicTacToeClient2`), representing Player 1 and Player 2.

**Object-Oriented Design:**



 The Tic-Tac-Toe grid is represented by the `Cell` class, and each game session is managed by the `GameSessionHandler` class on the server side.


**JavaFX for GUI**


Each cell in the Tic-Tac-Toe grid is represented as a custom `Cell` class, which extends `Pane` and handles mouse clicks for player moves.


**Multithreading** 

Multithreading is used to manage concurrent operations. Each game session runs in a separate thread, allowing the server to handle multiple clients simultaneously without blocking.

**Network Communication**  

Socket programming is utilized for communication between the server and clients. The server listens on a specific port (8000), and clients connect to this port. Data streams (`DataInputStream` and `DataOutputStream`) are used for communication between the server and clients.

**Game Logic**

 The game logic is implemented in the `GameSessionHandler` class on the server side. It manages player moves, checks for a winner, and communicates game status to clients.

