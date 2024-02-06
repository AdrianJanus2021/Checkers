package com.example.checkers;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;


public class Client extends Application{
    public ServerInterface si;
    public String playerColor;
    public GridPane boardGrid=new GridPane();
    public GridPane playerGrid=new GridPane();
    public GridPane score=new GridPane();
    public TextField ip=new TextField();
    List<int[]> possibleMoves=new ArrayList<>();
    Circle movingPiece=null;
    Thread thread;
    Label yourTurn;
    Label disconnect;
    @Override
    public void start(Stage stage) throws IOException {
        Platform.setImplicitExit(false);
        GridPane gridPane =new GridPane();
        gridPane.setVgap(10);

        Button connectButton = new Button("Game Connection");
        Label pieceLabel = new Label("Choose your piece Color:");
        Circle choosePiece = new Circle(20);
        ip.setText("localhost");

        choosePiece.setFill(Color.rgb(195, 9, 19));
        choosePiece.setStroke(Color.DARKRED);
        playerColor="red";

        gridPane.add(pieceLabel, 0, 0);
        gridPane.add(choosePiece, 0, 1);
        gridPane.add(ip,0,2);
        gridPane.add(connectButton, 0, 3);


        EventHandler<MouseEvent> handler = e -> {
            Circle c = (Circle) e.getSource();
            if (c.getFill().equals(Color.rgb(195, 9, 19))) {
                c.setFill(Color.rgb(14, 17, 17));
                c.setStroke(Color.GRAY);
                playerColor="black";
            } else {
                c.setFill(Color.rgb(195, 9, 19));
                c.setStroke(Color.DARKRED);
                playerColor="red";
            }

        };
        choosePiece.addEventHandler(MouseEvent.MOUSE_PRESSED, handler);


        connectButton.setOnAction(e -> {
            System.out.println("Player connecting to server");
            try {
                Registry reg = LocateRegistry.getRegistry(ip.getText(),1079);
                si = (ServerInterface) reg.lookup("ServerInterface");
                thread=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Runnable updater = new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if(si.isMoveMade()||si.getTrigger()){
                                        System.out.println(si.getTrigger());
                                        drawPieces();
                                        drawScore();
                                        if(si.getDisconnect())disconnect.setVisible(true);
                                        try {
                                            Thread.sleep(200);
                                            si.setMoveMade(false);
                                            si.endTrigger();
                                        } catch (InterruptedException ex) {
                                        }
                                    }
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        while (true) {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException ex) {
                            }
                            Platform.runLater(updater);
                        }
                    }
                });

                thread.setDaemon(true);
                thread.start();

                si.hello();
                if(!si.isGameFull()) {
                    if(si.getPlayers()[0]!=null) {
                        if (si.getPlayers()[0].getColor().equals(playerColor)) {
                            if(playerColor.equals("black")){
                                playerColor="red";
                            }
                            else{
                                playerColor="black";
                            }
//                            gridPane.add(sorryLabel, 0, gridPane.getRowCount());
                        }
                    }
                    si.addPlayer(new Player(playerColor));
                    if(si.startGame()){
                        if("black".equals(si.getPlayers()[0].getColor())){
                            si.setTurns(0,true);
                            si.setTurns(1,false);
                        }
                        else{
                            si.setTurns(0,false);
                            si.setTurns(1,true);
                        }
                    }
                    yourTurn=new Label("Your turn");
                    yourTurn.setVisible(false);

                    stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        @Override
                        public void handle(WindowEvent event) {
                            try {
                                if(playerColor.equals("red")){
                                    si.setWin(1);
                                }
                                else {
                                    si.setWin(0);
                                }
                                si.setTrigger();
                                si.setDisconnect(true);
                            } catch (RemoteException ex) {
                                ex.printStackTrace();
                            }
                            thread.interrupt();
                            Platform.exit();
                            System.exit(0);
                        }
                    });

                    gridPane.getChildren().clear();
                    initiateBoard();
                    drawPieces();/////////////////////////
                    drawScore();
                    gridPane.add(boardGrid,1,1);
                    gridPane.add(playerGrid,1,1);
                    gridPane.add(score,3,1);
                    gridPane.add(new Label("Your color: "+playerColor),1,2);
                    gridPane.add(yourTurn,1,3);
                    disconnect=new Label("Player disconnected...");
                    disconnect.setVisible(false);
                    gridPane.add(disconnect,1,4);


                }
                else {
                    Label sorryLabel=new Label("Sorry...Game is already Full...");
                    gridPane.getChildren().clear();
                    gridPane.add(sorryLabel,0,0);
                    stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        @Override
                        public void handle(WindowEvent event) {
                            thread.interrupt();
                            Platform.exit();
                            System.exit(0);
                        }
                    });
                }

                for (Player p: si.getPlayers()){
                    if(p!=null) System.out.println(p.getColor());
                    else System.out.println("null");
                }
                System.out.println();
            } catch (NotBoundException e1) {
                e1.printStackTrace();
            } catch (AccessException accessException) {
                accessException.printStackTrace();
            } catch (RemoteException remoteException) {
                remoteException.printStackTrace();
            }

        });

        Scene scene = new Scene(gridPane,450, 450);
        stage.setTitle("Checkers");
        stage.setScene(scene);
        stage.show();
    }

    public void initiateBoard() throws RemoteException {
        System.out.println("initiating board");
        for(int i=0;i< si.getBoard().length;i++) {
            for (int j = 0; j < si.getBoard()[i].length; j++) {
                Rectangle rec = new Rectangle(40, 40);
                if(si.getBoard()[i][j]==0) {
                    rec.setFill(Color.rgb(172,126,89));
                }
                else {
                    rec.setFill(Color.rgb(91,37,22));
                }
                rec.setStroke(Color.BLACK);
                boardGrid.add(rec,j,i);
            }
        }
    }
    public synchronized void drawPieces() throws RemoteException {
        playerGrid.getChildren().clear();
        if(!si.isWin()) {
            System.out.println("initiating pieces");
            for (int i = 0; i < si.getPieces().length; i++) {
                for (int j = 0; j < si.getPieces()[i].length; j++) {
                    if (si.getPieces()[i][j] != 0) {
                        Circle piece = new Circle(20);
                        if (si.getPieces()[i][j] == 2) {
                            piece.setFill(Color.rgb(195, 9, 19));
                            piece.setStroke(Color.DARKRED);
                        } if (si.getPieces()[i][j] == 1) {
                            piece.setFill(Color.rgb(14, 17, 17));
                            piece.setStroke(Color.GRAY);
                        }
                        if (si.getPieces()[i][j] == 3) {
                            piece.setFill(Color.BLUE);
                            piece.setStroke(Color.GRAY);
                        }
                        if (si.getPieces()[i][j] == 4) {
                            piece.setFill(Color.HOTPINK);
                            piece.setStroke(Color.GRAY);
                        }

                        playerGrid.add(piece, j, i);
                        if (si.startGame()) {
                            if (playerColor.equals(si.getPlayers()[0].getColor())) {
                                yourTurn.setVisible(si.getTurns()[0]);
                            }
                            if (playerColor.equals(si.getPlayers()[1].getColor())) {
                                yourTurn.setVisible(si.getTurns()[1]);
                            }
                            if (correctPlayer(piece)) {
                                EventHandler<MouseEvent> showmoves = e -> {
                                    try {
                                        si.emptyBeatings();
                                    } catch (RemoteException ex) {
                                        ex.printStackTrace();
                                    }
                                    int column = GridPane.getColumnIndex((Node) e.getSource());
                                    int row = GridPane.getRowIndex((Node) e.getSource());
                                    try {
                                        movingPiece = (Circle) e.getSource();
                                        possibleMoves = si.getPossibleMoves(row, column);
                                        List<BeatingPiece> beatingList = si.getBeatingMoves();
                                        for (BeatingPiece b : beatingList) {
                                            System.out.println(b.getX() + " " + b.getY() + ":");
                                            for (int[] a : b.getBeatenPieces()) {
                                                System.out.println(a[0] + " " + a[1]);
                                            }
                                            System.out.println();
                                        }
                                        System.out.println("moves:");
                                        for (int[] abba : possibleMoves) {//TODO
                                            System.out.println(abba[0] + " " + abba[1]);
                                        }
                                        System.out.println();

                                        for (Node node : playerGrid.getChildren()) {
                                            if (!possibleMoves.isEmpty()) {
                                                for (int[] m : possibleMoves) {
                                                    if (GridPane.getColumnIndex(node) == m[1] && GridPane.getRowIndex(node) == m[0]) {
                                                        Rectangle r = (Rectangle) node;
                                                        r.setFill(Color.CYAN);
                                                        break;
                                                    } else if (si.getPieces()[GridPane.getRowIndex(node)][GridPane.getColumnIndex(node)] == 0) {
                                                        Rectangle r = (Rectangle) node;
                                                        if (si.getBoard()[GridPane.getRowIndex(node)][GridPane.getColumnIndex(node)] == 0) {
                                                            r.setFill(Color.rgb(172, 126, 89));
                                                        } else {
                                                            r.setFill(Color.rgb(91, 37, 22));
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (si.getPieces()[GridPane.getRowIndex(node)][GridPane.getColumnIndex(node)] == 0) {
                                                    Rectangle r = (Rectangle) node;
                                                    if (si.getBoard()[GridPane.getRowIndex(node)][GridPane.getColumnIndex(node)] == 0) {
                                                        r.setFill(Color.rgb(172, 126, 89));
                                                    } else {
                                                        r.setFill(Color.rgb(91, 37, 22));
                                                    }

                                                }
                                            }
                                        }
                                        si.emptyMoves();

                                    } catch (RemoteException ex) {
                                        ex.printStackTrace();
                                    }
                                };
                                piece.addEventHandler(MouseEvent.MOUSE_PRESSED, showmoves);
                            } else {
                                EventHandler<MouseEvent> hidemoves = e -> {
                                    movingPiece = null;
                                    possibleMoves = new ArrayList<>();
                                    try {
                                        si.emptyMoves();
                                        si.emptyBeatings();
                                        drawPieces();
                                    } catch (RemoteException ex) {
                                        ex.printStackTrace();
                                    }
                                };
                                piece.addEventHandler(MouseEvent.MOUSE_PRESSED, hidemoves);
                            }
                        }
                    } else {
                        Rectangle piece = new Rectangle(40, 40);
                        if (si.getBoard()[i][j] == 0) {
                            piece.setFill(Color.rgb(172, 126, 89));
                        } else {
                            piece.setFill(Color.rgb(91, 37, 22));
                        }
                        piece.setStroke(Color.BLACK);

                        if (si.startGame()) {
                            EventHandler<MouseEvent> moveTo = e -> {
                                possibleMoves = new ArrayList<>();
                                try {
                                    si.emptyMoves();
                                    Rectangle rec = (Rectangle) e.getSource();
                                    if (rec.getFill().equals(Color.CYAN)) {
                                        int ColumnTo = GridPane.getColumnIndex(rec);
                                        int RowTo = GridPane.getRowIndex(rec);

                                        List<BeatingPiece> beatingList = si.getBeatingMoves();
                                        for (BeatingPiece b : beatingList) {
                                            if (b.getX() == RowTo && b.getY() == ColumnTo) {
                                                for (int[] a : b.getBeatenPieces()) {
                                                    si.changePieceValue(a[0], a[1], 0);
                                                    if (playerColor.equals("red")) {
                                                        si.addToScore(0);
                                                    }
                                                    if (playerColor.equals("black")) {
                                                        si.addToScore(1);
                                                    }
                                                }
                                            }
                                        }

                                        int ColumnFrom = GridPane.getColumnIndex(movingPiece);
                                        int RowFrom = GridPane.getRowIndex(movingPiece);


                                        if(playerColor.equals("black")&&RowTo==0){
                                            si.changePieceValue(RowTo, ColumnTo, 3);
                                        }
                                        else if(playerColor.equals("red")&&RowTo==si.getPieces().length-1){
                                            si.changePieceValue(RowTo, ColumnTo, 4);
                                        }
                                        else {
                                            si.changePieceValue(RowTo, ColumnTo, si.getPieces()[RowFrom][ColumnFrom]);
                                        }
                                        si.changePieceValue(RowFrom, ColumnFrom, 0);

                                        si.setMoveMade(true);

                                        if (playerColor.equals(si.getPlayers()[0].getColor())) {
                                            si.setTurns(0, false);
                                            si.setTurns(1, true);
                                        }
                                        if (playerColor.equals(si.getPlayers()[1].getColor())) {
                                            si.setTurns(0, true);
                                            si.setTurns(1, false);
                                        }
                                    } else {
                                        movingPiece = null;
                                        possibleMoves = new ArrayList<>();
                                        si.emptyMoves();
                                        drawPieces();
                                    }
                                } catch (RemoteException ex) {
                                    ex.printStackTrace();
                                }
                            };
                            piece.addEventHandler(MouseEvent.MOUSE_PRESSED, moveTo);
                        }
                        playerGrid.add(piece, j, i);
                    }
                }
            }
        }
        else {
            playerGrid.getChildren().clear();
            yourTurn.setVisible(false);
            Text win=new Text();
            if((si.getScore()[1]==12 && playerColor.equals("black"))||(si.getScore()[0]==12 && playerColor.equals("red"))) {
                win.setText("You Win!");
                win.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 60));
                win.setFill(Color.CYAN);
            }
            else {
                win.setText("You Lose...");
                win.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 60));
                win.setFill(Color.HOTPINK);
            }
            playerGrid.add(win,0,0);
        }
    }
    public void drawScore(){
        score.getChildren().clear();
        try {
            score.setVgap(10);
            Text player1=new Text(String.valueOf(si.getScore()[0]));
            Text player2=new Text(String.valueOf(si.getScore()[1]));
            player1.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 30));
            player1.setFill(Color.rgb(195, 9, 19));
            player2.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 30));
            player2.setFill(Color.rgb(14, 17, 17));
            score.add(player1,0,0);
            score.add(player2,0,1);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public boolean correctPlayer(Circle piece)throws RemoteException{
        int column=GridPane.getColumnIndex(piece);
        int row=GridPane.getRowIndex(piece);
        if(((si.getPieces()[row][column]==2 ||si.getPieces()[row][column]==4)&&playerColor.equals("red"))||((si.getPieces()[row][column]==1|| si.getPieces()[row][column]==3)&&playerColor.equals("black"))){
            if(playerColor.equals(si.getPlayers()[0].getColor())){
                return si.getTurns()[0];
            }
            if(playerColor.equals(si.getPlayers()[1].getColor())){
                return si.getTurns()[1];
            }

        }
        return false;

    }

    public static void main(String[] args) throws RemoteException {
        launch();
    }

}
