package com.example.checkers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;

public class Player implements Serializable {
    public String color;

    public Player(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

}