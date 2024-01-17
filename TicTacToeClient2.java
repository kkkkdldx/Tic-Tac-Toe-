package com.example.proj4_java;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static com.example.proj4_java.TicTacToeMain.*;

public class TicTacToeClient2 extends Application {
    private boolean myTurn = false;
    private char otherToken = ' ';

    private char myToken = ' ';
    private Cell[][] cell = new Cell[3][3];
    private Label Title_label = new Label();
    private Label Status_label = new Label();
    private int Selected_row ;
    private int Column_Selected;
    private DataOutputStream toServer;//send data to the server.

    private DataInputStream fromServer;//read data from the server
    private boolean continue_Game = true;
    private boolean waiting = true;
    private String host = "localhost";




    @Override







    public void start(Stage stage){
        GridPane pane = new GridPane();
        for(int i=0; i<3; i++)
            for(int j=0; j<3; j++)
                pane.add(cell[i][j] = new Cell(i, j), j, i);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(Title_label);
        borderPane.setCenter(pane);
        borderPane.setBottom(Status_label);

        Scene scene = new Scene(borderPane, 320, 350);
        stage.setTitle("Tic-Tac-Toe-Client");
        stage.setScene(scene);
        stage.show();

        connectToServer();
    }




    private void connectToServer(){


        //Establish a socket connection to a server at a specified host and port
        try{
            Socket socket = new Socket(host, 8000);
            toServer = new DataOutputStream(socket.getOutputStream());
            fromServer = new DataInputStream(socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }







//handles communication between Player 1 and Player 2

        new Thread(() -> {
            try{
                int player = fromServer.readInt();


                   if(player == PLAYER2){
                    myToken = '0';//Player 2 is 'O'
                    otherToken = 'X';
                    Platform.runLater(() -> {
                        Title_label.setText("Player 2 ='0'");
                        Status_label.setText("Waiting for player 1 to make a move");
                    });
                }



               else if(player == PLAYER1){
                    myToken = 'X'; //Player 1 is 'X'
                    otherToken = '0';
                    Platform.runLater(() -> {
                        Title_label.setText("Player 1 =  'X'");
                        Status_label.setText("Waiting for Player 2 .....");
                    });
                    fromServer.readInt();
                    Platform.runLater(() ->
                            Status_label.setText("Player 2 has joined"));
                    myTurn = true;
                }






                while(continue_Game){


                    if (player == PLAYER2)
                    {
                        getInfo_FromServer();
                        WaitforclientResponse();
                        sendMove();
                    }
                    else if(player == PLAYER1){
                        WaitforclientResponse();
                        sendMove();
                        getInfo_FromServer();


                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // current thread wait until the waiting condition becomes false.
    private void WaitforclientResponse() throws InterruptedException{
        while(waiting){
            Thread.sleep(100);
        }//loops until wait is set to false
        waiting = true;
    }





    private void get_move() throws IOException{
        int row = fromServer.readInt();
        int column = fromServer.readInt();
        Platform.runLater(() -> cell[row][column].setToken(otherToken));
    }

    private void sendMove() throws IOException{
        toServer.writeInt(Selected_row );
        toServer.writeInt(Column_Selected);
    }





    //handles different game statuses received from the server
    private void getInfo_FromServer() throws IOException{
        int status = fromServer.readInt(); //Reads status of the game from the input stream (fromServer)..

         if(status == PLAYER2_WON){// If player 2 wins
            continue_Game = false;// game is over.
            if(myToken == '0'){//updates the UI to indicate that Player 2 has won
                Platform.runLater(() -> Status_label.setText("you won!"));
                get_move();
            }

            else if(myToken == 'X'){ //indicate that the local player (Player 2) has won.
                Platform.runLater(() -> Status_label.setText("Player 2 won :("));
            }


        }

       else  if(status == PLAYER1_WON){// If player 1 wins
            continue_Game = false; // game is over.


              if(myToken == '0'){//updates the UI to indicate that Player 1 has won
                 Platform.runLater(() -> Status_label.setText("Player 1 won :("));
                  get_move();
             }

            else if(myToken == 'X'){ //indicate that the local player (Player 1) has won.
                Platform.runLater(() -> Status_label.setText(" You won !"));
            }


        }



        else if (status == DRAW) { // game is over with no winner
            continue_Game = false;
            Platform.runLater(() -> Status_label.setText("Game is over, Result = Draw!"));
            if(myToken == '0'){
                get_move();
            }
        }



        else{//If none of the above conditions are met game keeps going
             get_move();
            Platform.runLater(() -> Status_label.setText("My turn"));
            myTurn = true;
        }
    }







    public class Cell extends Pane {
        private int row;
        private int col;
        private char token = ' ';
        public Cell(int row, int col){ //constructor initializes the rows and column
            this.row = row;
            this.col = col;
            this.setPrefSize(2000, 2000);
            setStyle("-fx-border-color: black");
            this.setOnMouseClicked(e -> MouseClick_ErrorHandel());
        }


        public void setToken(char c){ //Sets the token of the cell to the specified character
            token = c;
            repaint();
        }

        public char getToken(){//Returns the current token associated with the cell
            return token;
        }


        //ensures that a player can only make a move if it's their turn and if the clicked cell is empty.
        private void MouseClick_ErrorHandel(){
            if(token == ' ' && myTurn){
                setToken(myToken);
                myTurn = false;
                Selected_row  = row;
                Column_Selected = col;
                Status_label.setText("Waiting for the other player to move");
                waiting = false;
            }
        }



        protected void repaint(){ //updates the visual representation of the cell  ('X' or '0')



            //The ellipse is added to the list of children of the Pane, updating the visual representation of the cell
             if(token == '0'){
                Ellipse ellipse = new Ellipse(this.getWidth() / 2, this.getHeight() / 2,
                        this.getWidth() / 2 - 10, this.getHeight() / 2 - 10);
                ellipse.centerXProperty().bind(this.widthProperty().divide(2));
                ellipse.centerYProperty().bind(this.heightProperty().divide(2));
                ellipse.radiusXProperty().bind(this.widthProperty().divide(2).subtract(10));
                ellipse.radiusYProperty().bind(this.heightProperty().divide(2).subtract(10));
                ellipse.setStroke(Color.BLACK);
                ellipse.setFill(Color.WHITE);

                getChildren().add(ellipse);
            }



            ////ines are then added to the list of children of the Pane
            else if(token == 'X'){
                Line line1 = new Line(10, 10, this.getWidth() - 10, this.getHeight() - 10);
                line1.endXProperty().bind(this.widthProperty().subtract(10));
                line1.endYProperty().bind(this.heightProperty().subtract(10));

                Line line2 = new Line(10, this.getHeight() - 10, this.getWidth() - 10, 10);
                line2.startYProperty().bind(this.heightProperty().subtract(10));
                line2.endXProperty().bind(this.widthProperty().subtract(10));

                this.getChildren().addAll(line1, line2);
            }



        }


    }
}