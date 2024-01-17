package com.example.proj4_java;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;



public class TicTacToeServer extends Application implements TicTacToeMain {
    private int sessionCount = 1;

    @Override
    public void start(Stage stage) {
        // Create a TextArea for logging server activity
        TextArea logArea = new TextArea();

        // Set up the GUI with a ScrollPane containing the log TextArea
        Scene scene = new Scene(new ScrollPane(logArea), 450, 200);
        stage.setTitle("Tic-Tac-Toe-Server");
        stage.setScene(scene);
        stage.show();

        // Start a new thread to handle server operations
        new Thread(() -> {
            try {
                // Set up the server socket to listen on port 8000
                ServerSocket serverSocket = new ServerSocket(8000);
                // Log server start time
                Platform.runLater(() -> logArea.appendText(new Date() + ": Server started at socket 8000\n"));

                while (true) {
                    // Log waiting for players
                    Platform.runLater(() -> logArea.appendText(new Date() + ": Wait for players to join session " + sessionCount + '\n'));

                    // Accept Player 1 connection
                    Socket player1Socket = serverSocket.accept();

                    // Log Player 1 connection
                    Platform.runLater(() -> {
                        logArea.appendText(new Date() + ": Player 1 joined session " + sessionCount + '\n');
                        logArea.appendText("Player 1's IP address " + player1Socket.getInetAddress().getHostAddress() + '\n');
                    });

                    // Inform Player 1 that they are PLAYER1
                    new DataOutputStream(player1Socket.getOutputStream()).writeInt(PLAYER1);

                    // Accept Player 2 connection
                    Socket player2Socket = serverSocket.accept();

                    // Log Player 2 connection
                    Platform.runLater(() -> {
                        logArea.appendText(new Date() + ": Player 2 joined session " + sessionCount + '\n');
                        logArea.appendText("Player 2's IP address " + player2Socket.getInetAddress().getHostAddress() + '\n');
                    });

                    // Inform Player 2 that they are PLAYER2
                    new DataOutputStream(player2Socket.getOutputStream()).writeInt(PLAYER2);

                    // Log starting a new thread for the current session
                    Platform.runLater(() ->
                            logArea.appendText(new Date() + ": Start a thread for session " + sessionCount++ + '\n'));

                    // Start a new thread to handle the current session
                    new Thread(new GameSessionHandler(player1Socket, player2Socket, logArea)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Inner class representing a session
    class GameSessionHandler implements Runnable, TicTacToeMain {
        private Socket player1Socket;
        private Socket player2Socket;
        private char[][] gameBoard = new char[3][3];
        private DataInputStream fromPlayer1;
        private DataOutputStream toPlayer1;
        private DataInputStream fromPlayer2;
        private DataOutputStream toPlayer2;
        private boolean continueToPlay = true;

        // Constructor for GameSessionHandler
        public GameSessionHandler(Socket player1Socket, Socket player2Socket, TextArea logArea) {
            this.player1Socket = player1Socket;
            this.player2Socket = player2Socket;

            // Initialize the game board cells
            for (int i = 0; i < 3; i++)
                for (int j = 0; j < 3; j++)
                    gameBoard[i][j] = ' ';
        }

        // Run method for the thread
        public void run() {
            try {
                // Set up input and output streams for Player 1 and Player 2
                fromPlayer1 = new DataInputStream(player1Socket.getInputStream());
                toPlayer1 = new DataOutputStream(player1Socket.getOutputStream());
                fromPlayer2 = new DataInputStream(player2Socket.getInputStream());
                toPlayer2 = new DataOutputStream(player2Socket.getOutputStream());

                // Inform Player 1 that the game is ready to start
                toPlayer1.writeInt(1);

                while (continueToPlay) {
                    // Receive Player 1's move
                    int row = fromPlayer1.readInt();
                    int column = fromPlayer1.readInt();
                    gameBoard[row][column] = 'X';

                    // Check if Player 1 has won or if it's a draw
                    if (isWinner('X')) {
                        toPlayer1.writeInt(PLAYER1_WON);
                        toPlayer2.writeInt(PLAYER1_WON);
                        sendMove(toPlayer2, row, column);
                        break;
                    } else if (isBoardFull()) {
                        toPlayer1.writeInt(DRAW);
                        toPlayer2.writeInt(DRAW);
                        sendMove(toPlayer2, row, column);
                        break;
                    } else {
                        // Inform Player 2 to continue the game
                        toPlayer2.writeInt(CONTINUE);
                        sendMove(toPlayer2, row, column);
                    }

                    // Receive Player 2's move
                    row = fromPlayer2.readInt();
                    column = fromPlayer2.readInt();
                    gameBoard[row][column] = 'O';

                    // Check if Player 2 has won
                    if (isWinner('O')) {
                        toPlayer1.writeInt(PLAYER2_WON);
                        toPlayer2.writeInt(PLAYER2_WON);
                        sendMove(toPlayer1, row, column);
                        break;
                    } else {
                        // Inform Player 1 to continue the game
                        toPlayer1.writeInt(CONTINUE);
                        sendMove(toPlayer1, row, column);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Send the move to the specified player
        private void sendMove(DataOutputStream out, int row, int column) throws IOException {
            out.writeInt(row);
            out.writeInt(column);
        }

        // Check if the game board is full
        private boolean isBoardFull() {
            for (int i = 0; i < 3; i++)
                for (int j = 0; j < 3; j++)
                    if (gameBoard[i][j] == ' ')
                        return false;
            return true;
        }

        // Check if a player has won
        private boolean isWinner(char token) {
            for (int i = 0; i < 3; i++)
                if ((gameBoard[i][0] == token) && (gameBoard[i][1] == token) && (gameBoard[i][2] == token)) {
                    return true;
                }
            for (int j = 0; j < 3; j++)
                if ((gameBoard[0][j] == token) && (gameBoard[1][j] == token) && (gameBoard[2][j] == token)) {
                    return true;
                }
            if ((gameBoard[0][0] == token) && (gameBoard[1][1] == token) && (gameBoard[2][2] == token)) {
                return true;
            }
            return false;
        }
    }
}



