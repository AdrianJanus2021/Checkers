package com.example.checkers;

import java.rmi.*;
import java.util.List;

public interface ServerInterface extends Remote{
    void hello() throws RemoteException;
    public void updateBoard() throws RemoteException;
    public void leaveGame(String player) throws RemoteException;
    public Player[] getPlayers() throws RemoteException;
    public void addPlayer(Player player)throws RemoteException;
    public boolean isGameFull()throws RemoteException;
    public int[][] getBoard() throws RemoteException;
    public int[][] getPieces() throws RemoteException;
    public List<int[]> getPossibleMoves(int row, int column) throws RemoteException;
    public void changePieceValue(int row, int column, int value) throws RemoteException;
    public boolean isMoveMade() throws RemoteException;
    public void setMoveMade(boolean b) throws RemoteException;
    public void emptyMoves() throws RemoteException;
    public void emptyBeatings() throws RemoteException;
    public List<BeatingPiece> getBeatingMoves() throws RemoteException;
    public int[]getScore() throws RemoteException;
    public void addToScore(int p) throws RemoteException;
    public boolean[]getTurns() throws RemoteException;
    public void setTurns(int i,boolean b) throws RemoteException;
    public boolean startGame() throws RemoteException;
    public boolean getTrigger() throws RemoteException;
    public void endTrigger() throws RemoteException;
    public boolean isWin() throws RemoteException;
    public void removePlayer(String color) throws RemoteException;
    public void setWin(int i) throws RemoteException;
    public void setTrigger() throws RemoteException;
    public boolean getDisconnect() throws RemoteException;
    public void setDisconnect(boolean b) throws RemoteException;


}
