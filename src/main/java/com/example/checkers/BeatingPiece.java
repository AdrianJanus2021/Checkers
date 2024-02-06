package com.example.checkers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BeatingPiece implements Serializable {
    int x;
    int y;
    List<int[]> beatenPieces;

    public BeatingPiece(int x, int y, List<int[]> beatenPieces) {
        this.x = x;
        this.y = y;
        this.beatenPieces = beatenPieces;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public List<int[]> getBeatenPieces() {
        List<int[]> list=new ArrayList<>();
        for(int i=0;i<beatenPieces.size();i++){
            list.add(new int[]{beatenPieces.get(i)[0],beatenPieces.get(i)[1]});
        }
        return list;
    }

}
