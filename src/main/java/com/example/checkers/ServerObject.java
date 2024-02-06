package com.example.checkers;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerObject implements ServerInterface, Serializable {
    public Player[] players;
    public int[][] emptyBoard;
    public int[][] playerPieces;
    boolean moveMade;
    List<int[]> moves=new ArrayList<>();
    List<int[]>jumps=new ArrayList<>();
    List<BeatingPiece> beatingMoves=new ArrayList<>();
    public int[]score;
    public boolean[]turns;
    public boolean startTrigger;
    public boolean disconnect;
    public ServerObject() throws RemoteException {
        disconnect=false;
        moveMade=false;
        players=new Player[2];
        score=new int[]{0,0};
        turns=new boolean[]{false,false};
        startTrigger=startGame();
        emptyBoard= new int[][]{{0, 1, 0, 1, 0, 1, 0, 1},
                                {1, 0, 1, 0, 1, 0, 1, 0},
                                {0, 1, 0, 1, 0, 1, 0, 1},
                                {1, 0, 1, 0, 1, 0, 1, 0},
                                {0, 1, 0, 1, 0, 1, 0, 1},
                                {1, 0, 1, 0, 1, 0, 1, 0},
                                {0, 1, 0, 1, 0, 1, 0, 1},
                                {1, 0, 1, 0, 1, 0, 1, 0}
                                };
        playerPieces= new int[][]{{0, 2, 0, 2, 0, 2, 0, 2},
                                  {2, 0, 2, 0, 2, 0, 2, 0},
                                  {0, 2, 0, 2, 0, 2, 0, 2},
                                  {0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0},
                                  {1, 0, 1, 0, 1, 0, 1, 0},
                                  {0, 1, 0, 1, 0, 1, 0, 1},
                                  {1, 0, 1, 0, 1, 0, 1, 0}
        };
//        playerPieces= new int[][]{{0, 2, 0, 0, 0, 0, 0, 0},
//                {2, 0, 2, 0, 2, 0, 2, 0},
//                {0, 0, 0, 0, 0, 0, 0, 2},
//                {0, 0, 2, 0, 2, 0, 2, 0},
//                {0, 1, 0, 0, 0, 0, 0, 0},
//                {1, 0, 2, 0, 1, 0, 2, 0},
//                {0, 1, 0, 1, 0, 1, 0, 1},
//                {1, 0, 1, 0, 1, 0, 1, 0}
//        };
//        playerPieces= new int[][]{{0, 0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 1, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 2, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0}
//        };

    }
    @Override
    public Player[] getPlayers() throws RemoteException{
        return players;
    }
    @Override
    public void addPlayer(Player player)throws RemoteException{
        if(players[0]==null){
            players[0]=player;
        }
        else if(players[1]==null){
            players[1]=player;
            setTrigger();
        }
    }
    @Override
    public boolean isGameFull(){
        return players[1]!=null;
    }

    @Override
    public int[][] getBoard() throws RemoteException {
        return emptyBoard;
    }

    @Override
    public int[][] getPieces() throws RemoteException {
        return playerPieces;
    }

    @Override
    public List<int[]> getPossibleMoves(int row, int column) throws RemoteException {
        int initialRow=row;
        int initialColumn=column;
        if(playerPieces[initialRow][initialColumn]==1){//TODO
            jumps.add(new int[]{row,column});
            for(int i=0;i<jumps.size();i++) {
                row=jumps.get(i)[0];
                column=jumps.get(i)[1];
                if (column > 0 && row>0) {
                    if (playerPieces[row][column] == 1) {
                        if (playerPieces[row - 1][column - 1] ==0) {
                            int[] space = new int[]{row - 1, column - 1};
                            if(!movesContains(space)) {
                                moves.add(space);
                            }
                        }
                    }
                    if (playerPieces[row - 1][column - 1] !=0&&playerPieces[row - 1][column - 1] !=1) {
                        if (row - 2 >= 0 && column - 2 >= 0) {
                            if (playerPieces[row - 2][column - 2] ==0) {
                                int[] space = new int[]{row - 2, column - 2};
                                if(!movesContains(space)) {
                                    moves.add(space);
                                    BeatingPiece prevPiece=getBeatingPiece(row,column);
                                    List<int[]> previousJumps=new ArrayList<>();
                                    if(prevPiece!=null) {
                                        previousJumps = prevPiece.getBeatenPieces();
                                    }
                                    previousJumps.add(new int[]{row-1,column-1});
                                    beatingMoves.add(new BeatingPiece(row-2,column-2,previousJumps));
                                    row = row - 2;
                                    column = column - 2;
                                    jumps.add(new int[]{row, column});
                                }
                            }
                        }
                    }
                }
                row=jumps.get(i)[0];
                column=jumps.get(i)[1];
                if (column < playerPieces[0].length - 1&&row>0) {
                    if(playerPieces[row][column] == 1) {
                        if (playerPieces[row - 1][column + 1] ==0) {
                            int[] space = new int[]{row - 1, column + 1};
                            if(!movesContains(space)) {
                                moves.add(space);
                            }
                        }
                    }
                    if (playerPieces[row - 1][column + 1] !=0&&playerPieces[row - 1][column + 1] !=1) {
                        if (row - 2 >= 0 && column + 2 < playerPieces.length) {
                            if (playerPieces[row - 2][column + 2] ==0) {
                                int[] space = new int[]{row - 2, column + 2};
                                if(!movesContains(space)) {
                                    moves.add(space);
                                    BeatingPiece prevPiece=getBeatingPiece(row,column);
                                    List<int[]> previousJumps=new ArrayList<>();
                                    if(prevPiece!=null) {
                                        previousJumps = prevPiece.getBeatenPieces();
                                    }
                                    previousJumps.add(new int[]{row-1,column+1});
                                    beatingMoves.add(new BeatingPiece(row-2,column+2,previousJumps));
                                    row = row - 2;
                                    column = column + 2;
                                    jumps.add(new int[]{row, column});
                                }
                            }
                        }
                    }
                }
            }
            jumps=new ArrayList<>();
        }
        if(playerPieces[initialRow][initialColumn]==2){//TODO
            jumps.add(new int[]{row,column});
            for(int i=0;i<jumps.size();i++) {
                row=jumps.get(i)[0];
                column=jumps.get(i)[1];
                if (column > 0 && row < playerPieces.length - 1) {
                    if (playerPieces[row][column] == 2) {
                        if (playerPieces[row + 1][column - 1] ==0) {
                            int[] space = new int[]{row + 1, column - 1};
                            if(!movesContains(space)) {
                                moves.add(space);
                            }
                        }
                    }
                    if (playerPieces[row + 1][column - 1] !=0&&playerPieces[row + 1][column - 1] !=2) {
                        if (row + 2 < playerPieces.length && column - 2 >= 0) {
                            if (playerPieces[row + 2][column - 2] ==0) {
                                int[] space = new int[]{row + 2, column - 2};
                                if(!movesContains(space)) {
                                    moves.add(space);
                                    BeatingPiece prevPiece=getBeatingPiece(row,column);
                                    List<int[]> previousJumps=new ArrayList<>();
                                    if(prevPiece!=null) {
                                        previousJumps = prevPiece.getBeatenPieces();
                                    }
                                    previousJumps.add(new int[]{row+1,column-1});
                                    beatingMoves.add(new BeatingPiece(row+2,column-2,previousJumps));
                                    row = row + 2;
                                    column = column - 2;
                                    jumps.add(new int[]{row, column});
                                }
                            }
                        }
                    }
                }
                row=jumps.get(i)[0];
                column=jumps.get(i)[1];
                if (column < playerPieces[0].length - 1 && row < playerPieces.length - 1) {
                    if(playerPieces[row][column] == 2) {
                        if (playerPieces[row + 1][column + 1] ==0) {
                            int[] space = new int[]{row + 1, column + 1};
                            if(!movesContains(space)) {
                                moves.add(space);
                            }
                        }
                    }
                    if (playerPieces[row + 1][column + 1] !=0&&playerPieces[row + 1][column + 1] !=2) {
                        if (row + 2 < playerPieces.length && column + 2 < playerPieces.length) {
                            if (playerPieces[row + 2][column + 2] ==0) {
                                int[] space = new int[]{row + 2, column + 2};
                                if(!movesContains(space)) {
                                    moves.add(space);
                                    BeatingPiece prevPiece=getBeatingPiece(row,column);
                                    List<int[]> previousJumps=new ArrayList<>();
                                    if(prevPiece!=null) {
                                        previousJumps = prevPiece.getBeatenPieces();
                                    }
                                    previousJumps.add(new int[]{row+1,column+1});
                                    beatingMoves.add(new BeatingPiece(row+2,column+2,previousJumps));
                                    row = row + 2;
                                    column = column + 2;
                                    jumps.add(new int[]{row, column});
                                }
                            }
                        }
                    }
                }
            }
            jumps=new ArrayList<>();
        }
        if(playerPieces[initialRow][initialColumn]==3){//TODO
            jumps.add(new int[]{row,column});
            for(int i=0;i<jumps.size();i++) {
                row=jumps.get(i)[0];
                column=jumps.get(i)[1];
                if (column > 0 && row>0) {
                    if (playerPieces[row][column] == 3) {
                        if (playerPieces[row - 1][column - 1] ==0) {
                            int[] space = new int[]{row - 1, column - 1};
                            if(!movesContains(space)) {
                                moves.add(space);
                            }
                        }
                    }
                    if (playerPieces[row - 1][column - 1] !=0&&playerPieces[row - 1][column - 1] !=3) {
                        if (row - 2 >= 0 && column - 2 >= 0) {
                            if (playerPieces[row - 2][column - 2] ==0) {
                                int[] space = new int[]{row - 2, column - 2};
                                if(!movesContains(space)) {
                                    moves.add(space);
                                    BeatingPiece prevPiece=getBeatingPiece(row,column);
                                    List<int[]> previousJumps=new ArrayList<>();
                                    if(prevPiece!=null) {
                                        previousJumps = prevPiece.getBeatenPieces();
                                    }
                                    previousJumps.add(new int[]{row-1,column-1});
                                    beatingMoves.add(new BeatingPiece(row-2,column-2,previousJumps));
                                    row = row - 2;
                                    column = column - 2;
                                    jumps.add(new int[]{row, column});
                                }
                            }
                        }
                    }
                }
                row=jumps.get(i)[0];
                column=jumps.get(i)[1];
                if (column < playerPieces[0].length - 1&&row>0) {
                    if(playerPieces[row][column] == 3) {
                        if (playerPieces[row - 1][column + 1] ==0) {
                            int[] space = new int[]{row - 1, column + 1};
                            if(!movesContains(space)) {
                                moves.add(space);
                            }
                        }
                    }
                    if (playerPieces[row - 1][column + 1] !=0&&playerPieces[row - 1][column + 1] !=3) {
                        if (row - 2 >= 0 && column + 2 < playerPieces.length) {
                            if (playerPieces[row - 2][column + 2] ==0) {
                                int[] space = new int[]{row - 2, column + 2};
                                if(!movesContains(space)) {
                                    moves.add(space);
                                    BeatingPiece prevPiece=getBeatingPiece(row,column);
                                    List<int[]> previousJumps=new ArrayList<>();
                                    if(prevPiece!=null) {
                                        previousJumps = prevPiece.getBeatenPieces();
                                    }
                                    previousJumps.add(new int[]{row-1,column+1});
                                    beatingMoves.add(new BeatingPiece(row-2,column+2,previousJumps));
                                    row = row - 2;
                                    column = column + 2;
                                    jumps.add(new int[]{row, column});
                                }
                            }
                        }
                    }
                }
                row=jumps.get(i)[0];
                column=jumps.get(i)[1];
                if (column > 0 && row < playerPieces.length - 1) {
                    if (playerPieces[row][column] == 3) {
                        if (playerPieces[row + 1][column - 1] ==0) {
                            int[] space = new int[]{row + 1, column - 1};
                            if(!movesContains(space)) {
                                moves.add(space);
                            }
                        }
                    }
                    if (playerPieces[row + 1][column - 1] !=0&&playerPieces[row + 1][column - 1] !=3) {
                        if (row + 2 < playerPieces.length && column - 2 >= 0) {
                            if (playerPieces[row + 2][column - 2] ==0) {
                                int[] space = new int[]{row + 2, column - 2};
                                if(!movesContains(space)) {
                                    moves.add(space);
                                    BeatingPiece prevPiece=getBeatingPiece(row,column);
                                    List<int[]> previousJumps=new ArrayList<>();
                                    if(prevPiece!=null) {
                                        previousJumps = prevPiece.getBeatenPieces();
                                    }
                                    previousJumps.add(new int[]{row+1,column-1});
                                    beatingMoves.add(new BeatingPiece(row+2,column-2,previousJumps));
                                    row = row + 2;
                                    column = column - 2;
                                    jumps.add(new int[]{row, column});
                                }
                            }
                        }
                    }
                }
                row=jumps.get(i)[0];
                column=jumps.get(i)[1];
                if (column < playerPieces[0].length - 1 && row < playerPieces.length - 1) {
                    if(playerPieces[row][column] == 3) {
                        if (playerPieces[row + 1][column + 1] ==0) {
                            int[] space = new int[]{row + 1, column + 1};
                            if(!movesContains(space)) {
                                moves.add(space);
                            }
                        }
                    }
                    if (playerPieces[row + 1][column + 1] !=0&&playerPieces[row + 1][column + 1] !=3) {
                        if (row + 2 < playerPieces.length && column + 2 < playerPieces.length) {
                            if (playerPieces[row + 2][column + 2] ==0) {
                                int[] space = new int[]{row + 2, column + 2};
                                if(!movesContains(space)) {
                                    moves.add(space);
                                    BeatingPiece prevPiece=getBeatingPiece(row,column);
                                    List<int[]> previousJumps=new ArrayList<>();
                                    if(prevPiece!=null) {
                                        previousJumps = prevPiece.getBeatenPieces();
                                    }
                                    previousJumps.add(new int[]{row+1,column+1});
                                    beatingMoves.add(new BeatingPiece(row+2,column+2,previousJumps));
                                    row = row + 2;
                                    column = column + 2;
                                    jumps.add(new int[]{row, column});
                                }
                            }
                        }
                    }
                }
            }
            jumps=new ArrayList<>();
        }
        if(playerPieces[initialRow][initialColumn]==4){//TODO
            jumps.add(new int[]{row,column});
            for(int i=0;i<jumps.size();i++) {
                row=jumps.get(i)[0];
                column=jumps.get(i)[1];
                if (column > 0 && row>0) {
                    if (playerPieces[row][column] == 4) {
                        if (playerPieces[row - 1][column - 1] ==0) {
                            int[] space = new int[]{row - 1, column - 1};
                            if(!movesContains(space)) {
                                moves.add(space);
                            }
                        }
                    }
                    if (playerPieces[row - 1][column - 1] !=0&&playerPieces[row - 1][column - 1] !=4) {
                        if (row - 2 >= 0 && column - 2 >= 0) {
                            if (playerPieces[row - 2][column - 2] ==0) {
                                int[] space = new int[]{row - 2, column - 2};
                                if(!movesContains(space)) {
                                    moves.add(space);
                                    BeatingPiece prevPiece=getBeatingPiece(row,column);
                                    List<int[]> previousJumps=new ArrayList<>();
                                    if(prevPiece!=null) {
                                        previousJumps = prevPiece.getBeatenPieces();
                                    }
                                    previousJumps.add(new int[]{row-1,column-1});
                                    beatingMoves.add(new BeatingPiece(row-2,column-2,previousJumps));
                                    row = row - 2;
                                    column = column - 2;
                                    jumps.add(new int[]{row, column});
                                }
                            }
                        }
                    }
                }
                row=jumps.get(i)[0];
                column=jumps.get(i)[1];
                if (column < playerPieces[0].length - 1&&row>0) {
                    if(playerPieces[row][column] == 4) {
                        if (playerPieces[row - 1][column + 1] ==0) {
                            int[] space = new int[]{row - 1, column + 1};
                            if(!movesContains(space)) {
                                moves.add(space);
                            }
                        }
                    }
                    if (playerPieces[row - 1][column + 1] !=0&&playerPieces[row - 1][column + 1] !=4) {
                        if (row - 2 >= 0 && column + 2 < playerPieces.length) {
                            if (playerPieces[row - 2][column + 2] ==0) {
                                int[] space = new int[]{row - 2, column + 2};
                                if(!movesContains(space)) {
                                    moves.add(space);
                                    BeatingPiece prevPiece=getBeatingPiece(row,column);
                                    List<int[]> previousJumps=new ArrayList<>();
                                    if(prevPiece!=null) {
                                        previousJumps = prevPiece.getBeatenPieces();
                                    }
                                    previousJumps.add(new int[]{row-1,column+1});
                                    beatingMoves.add(new BeatingPiece(row-2,column+2,previousJumps));
                                    row = row - 2;
                                    column = column + 2;
                                    jumps.add(new int[]{row, column});
                                }
                            }
                        }
                    }
                }
                row=jumps.get(i)[0];
                column=jumps.get(i)[1];
                if (column > 0 && row < playerPieces.length - 1) {
                    if (playerPieces[row][column] == 4) {
                        if (playerPieces[row + 1][column - 1] ==0) {
                            int[] space = new int[]{row + 1, column - 1};
                            if(!movesContains(space)) {
                                moves.add(space);
                            }
                        }
                    }
                    if (playerPieces[row + 1][column - 1] !=0&&playerPieces[row + 1][column - 1] !=4) {
                        if (row + 2 < playerPieces.length && column - 2 >= 0) {
                            if (playerPieces[row + 2][column - 2] ==0) {
                                int[] space = new int[]{row + 2, column - 2};
                                if(!movesContains(space)) {
                                    moves.add(space);
                                    BeatingPiece prevPiece=getBeatingPiece(row,column);
                                    List<int[]> previousJumps=new ArrayList<>();
                                    if(prevPiece!=null) {
                                        previousJumps = prevPiece.getBeatenPieces();
                                    }
                                    previousJumps.add(new int[]{row+1,column-1});
                                    beatingMoves.add(new BeatingPiece(row+2,column-2,previousJumps));
                                    row = row + 2;
                                    column = column - 2;
                                    jumps.add(new int[]{row, column});
                                }
                            }
                        }
                    }
                }
                row=jumps.get(i)[0];
                column=jumps.get(i)[1];
                if (column < playerPieces[0].length - 1 && row < playerPieces.length - 1) {
                    if(playerPieces[row][column] == 4) {
                        if (playerPieces[row + 1][column + 1] ==0) {
                            int[] space = new int[]{row + 1, column + 1};
                            if(!movesContains(space)) {
                                moves.add(space);
                            }
                        }
                    }
                    if (playerPieces[row + 1][column + 1] !=0&&playerPieces[row + 1][column + 1] !=4) {
                        if (row + 2 < playerPieces.length && column + 2 < playerPieces.length) {
                            if (playerPieces[row + 2][column + 2] ==0) {
                                int[] space = new int[]{row + 2, column + 2};
                                if(!movesContains(space)) {
                                    moves.add(space);
                                    BeatingPiece prevPiece=getBeatingPiece(row,column);
                                    List<int[]> previousJumps=new ArrayList<>();
                                    if(prevPiece!=null) {
                                        previousJumps = prevPiece.getBeatenPieces();
                                    }
                                    previousJumps.add(new int[]{row+1,column+1});
                                    beatingMoves.add(new BeatingPiece(row+2,column+2,previousJumps));
                                    row = row + 2;
                                    column = column + 2;
                                    jumps.add(new int[]{row, column});
                                }
                            }
                        }
                    }
                }
            }
            jumps=new ArrayList<>();
        }
        return moves;
    }

    public boolean movesContains(int[] arr)throws RemoteException{
        for(int[] a:moves){
            if (Arrays.equals(a, arr)){
                return true;
            }
        }
        return false;
    }
    public BeatingPiece getBeatingPiece(int x,int y){
        for(BeatingPiece b:beatingMoves){
            if(b.getX()==x&&b.getY()==y){
                return b;
            }
        }
        return null;
    }

    @Override
    public void changePieceValue(int row, int column, int value) throws RemoteException {
        playerPieces[row][column]=value;
    }

    @Override
    public boolean isMoveMade() throws RemoteException{
        return moveMade;
    }

    @Override
    public void setMoveMade(boolean b) throws RemoteException {
        moveMade=b;
    }

    @Override
    public void emptyMoves() throws RemoteException {
        moves=new ArrayList<>();
    }

    @Override
    public void emptyBeatings() throws RemoteException {
        beatingMoves=new ArrayList<>();
    }

    @Override
    public List<BeatingPiece> getBeatingMoves() throws RemoteException {
        return beatingMoves;
    }

    @Override
    public int[] getScore() throws RemoteException {
        return score;
    }

    @Override
    public void addToScore(int p) throws RemoteException {
        score[p]++;
    }

    @Override
    public boolean[] getTurns() throws RemoteException {
        return turns;
    }

    @Override
    public void setTurns(int i, boolean b) throws RemoteException {
        turns[i]=b;
    }

    @Override
    public boolean startGame() throws RemoteException {
        return players[1] != null;
    }

    @Override
    public boolean getTrigger() throws RemoteException {
        return startTrigger;
    }

    @Override
    public void endTrigger() throws RemoteException {
        startTrigger=false;
    }
    @Override
    public void setTrigger() throws RemoteException {
        System.out.println("trigger");
        startTrigger=true;
    }

    @Override
    public boolean getDisconnect() throws RemoteException {
        return disconnect;
    }

    @Override
    public void setDisconnect(boolean b) throws RemoteException {
        disconnect=b;
    }

    @Override
    public boolean isWin() throws RemoteException {
        return score[0]>=12||score[1]>=12;
    }

    @Override
    public void removePlayer(String color) throws RemoteException {
        if(players[0].getColor().equals(color)){
            players[0]=null;
        }
        if(players[1].getColor().equals(color)){
            players[1]=null;
        }
    }

    @Override
    public void setWin(int i) throws RemoteException {
        if(score[0]!=12&&score[1]!=12) {
            score[i] = 12;
        }
    }


    @Override
    public void hello() throws RemoteException {
        System.out.println("Player joined the game");
    }

    @Override
    public void updateBoard() throws RemoteException {

    }

    @Override
    public void leaveGame(String player) throws RemoteException {

    }

}
