package server;

import database.Database;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionHandler extends Thread {

    private Socket clientSocket;
    private Database db;
    private Connection connection;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ConnectionHandler(Socket clientSocket, Database db, Connection connection) {
        this.clientSocket = clientSocket;
        this.db = db;
        this.connection = connection;
        System.out.println("Connection Received from address: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + ".");
    }

    @Override
    public void run() {
        try {
            in = new ObjectInputStream(clientSocket.getInputStream());
            out = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println(e.toString());
            return;
        }
        try {
            try {
                new ClientHandler(clientSocket, db, connection, in, out);
            } catch (IOException ex) {
                Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            out.close();
            in.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}