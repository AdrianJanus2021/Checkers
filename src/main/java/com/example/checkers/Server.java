package com.example.checkers;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    public static void main(String[] args) throws RemoteException {
        ServerInterface ri = new ServerObject();
        try {
            ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(ri, 0);

            Registry reg = LocateRegistry.createRegistry(1079);
            reg.rebind("ServerInterface", stub);
            System.out.println("Server has started");

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
