package server;

import database.Database;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import client.ResultList;
import java.net.URL;
import file.SharedFile;

public class ClientHandler {

    private Socket clientSocket;
    private String specialChar = "%";
    private String[] splittedMessage;
    private Database db;
    private Connection conn;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String clientName;
    private ResultList resultList;
    private boolean flag = true;

    public ClientHandler(Socket clientSocket, Database db, Connection conn, ObjectInputStream in, ObjectOutputStream out) throws SQLException, IOException, ClassNotFoundException {
        this.clientSocket = clientSocket;
        this.db = db;
        this.conn = conn;
        this.in = in;
        this.out = out;
        db.prepareStatements(conn);
        ChooseAction();
    }

    @SuppressWarnings("empty-statement")
    public void ChooseAction() throws SQLException, IOException, ClassNotFoundException {
        try {
            String message;

            while (!clientSocket.isClosed()) {
                while ((message = (String) in.readObject()) == null);
                try {
                    executeCommand(message);
                    if (!clientSocket.isClosed()) { // If the client's socket is not closed, then
                        ChooseAction();             // continue executing incoming commands.
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (flag) {
                System.out.println("Connection with client " + clientName + " terminated!");
                flag = false;
            }
        } catch (SocketTimeoutException ste) {
            //In case the connection with the client is timed out.
            executeCommand("LOGOUT%" + clientName);
            System.err.println("Connection with client timed out!");
        } catch (IOException ioe) {
            //In case there is an I/O Error with the clients connection.            
            executeCommand("LOGOUT%" + clientName);
            System.out.println("Connection error!");
        }
    }

    public void executeCommand(String message) throws SQLException, IOException, ClassNotFoundException {
        ResultSet resultSet;
        URL resultIP = null;
        int counter;
        splittedMessage = message.split(specialChar, 2);

        if (splittedMessage[0].equals("LOGIN")) {
            //When a client logs in, then he sends a "LOGIN%<name>" message.
            //Then we add him to the onlineClients.
            db.addOnlineClient.setString(1, splittedMessage[1]);
            db.addOnlineClient.setString(2, clientSocket.getInetAddress().toString());
            db.addOnlineClient.setString(3, Integer.toString(clientSocket.getPort()));
            db.addOnlineClient.executeUpdate();
            clientName = splittedMessage[1];

        } else if (splittedMessage[0].equals("SHARE")) {
            // When a client wants to share a local folder in his computer he sends a "SHARE%"
            // Then the server requests the file list the client wants to share.
            sendCommand("GIVE FILES");
            // Server recieves the file List to be shared:
            SharedFile[] sharedFiles = (SharedFile[]) in.readObject();
            // Before adding the files the user wants to share, we have to remove any files
            // that the user has already been sharing:
            db.removeAllFilesByClient.setString(1, clientName);
            db.removeAllFilesByClient.executeUpdate();
            //And then add all of the files to be shared from scratch: 
            for (int i = 0; i < sharedFiles.length; i++) {
                db.addFile.setString(1, sharedFiles[i].getName()); // Name of the file to be added.
                db.addFile.setString(2, sharedFiles[i].getExtension().toUpperCase()); // Extension of the file to be added.
                db.addFile.setString(3, String.valueOf(sharedFiles[i].getSize())); // Size of the file to be added.
                db.addFile.setString(4, clientName); // Owner of the file to be added.
                db.addFile.executeUpdate();
            }
            sendCommand("OK");
        } else if (splittedMessage[0].equals("UNSHARE")) {
            // When a client wants to share a local folder in his computer he sends a "UNSHARE%"
            db.removeAllFilesByClient.setString(1, clientName);
            db.removeAllFilesByClient.executeUpdate();
            sendCommand("OK");
        } else if (splittedMessage[0].equals("LOGOUT")) {
            try {
                out.close();
                in.close();
                clientSocket.close();
            } catch (Exception e) {
                System.out.println("There was an error when closing connection with client: " + clientName);
            }
            // Firstly we remove the files that the exiting user shared:
            db.removeAllFilesByClient.setString(1, clientName);
            db.removeAllFilesByClient.executeUpdate();
            // If a user logs out, or if there is a connection error, we remove the user for ONLINE_CLIENTS table.
            db.removeOnlineClient.setString(1, clientName);
            db.removeOnlineClient.setString(2, clientSocket.getInetAddress().toString());
            db.removeOnlineClient.setString(3, Integer.toString(clientSocket.getPort()));
            db.removeOnlineClient.executeUpdate();
            // And finally close the ObjectInputStream and ObjectOutputStream and the socket:
        } else if (splittedMessage[0].equals("LIST")) {
            if (splittedMessage[1].equals("ALL")) { // In case user wants to get all available files. 
                resultList = new ResultList(db.getAllFiles.executeQuery());
                sendCommand(resultList);

            } else if (splittedMessage[1].startsWith(".")) {
                db.getFilesByExtention.setString(1, splittedMessage[1].replace(".", ""));
                resultList = new ResultList(db.getFilesByExtention.executeQuery());
                sendCommand(resultList);

            } else { // In case user wants a specific name of a file.
                db.getFilesByName.setString(1, splittedMessage[1]);
                resultList = new ResultList(db.getFilesByName.executeQuery());
                sendCommand(resultList);
            }

        } else if (splittedMessage[0].equals("GIVEIP")) {

            db.getFileByID.setString(1, splittedMessage[1]);
            resultSet = db.getFileByID.executeQuery();
            String resultName = "";
            String IP = "";
            String port = "";
            while (resultSet.next()) {
                resultName = resultSet.getString("OWNER");
            }
            db.getClientByName.setString(1, resultName);
            resultSet = db.getClientByName.executeQuery();
            while (resultSet.next()) {
                IP = resultSet.getString("IP");
                port = resultSet.getString("PORT");
            }
            sendCommand(IP);
            sendCommand(port);
        } else if (splittedMessage[0].equals("LISTMIN")) { // In case user sets minimum size limit.
            db.getFilesByMinimunSize.setString(1, splittedMessage[1]);
            resultList = new ResultList(db.getFilesByMinimunSize.executeQuery());
            sendCommand(resultList);

        } else if (splittedMessage[0].equals("LISTMAX")) { // In case user sets maximum size limit.
            db.getFilesByMaximunSize.setString(1, splittedMessage[1]);
            resultList = new ResultList(db.getFilesByMaximunSize.executeQuery());
            sendCommand(resultList);

        } else { // If server receives an unknown message. 
            System.out.println("Client " + clientName + " has sent and unknown command: " + message);
        }
    }

    private void sendCommand(Object object) {
        try {
            out.writeObject(object);
            out.flush();
        } catch (IOException ioe) {
            System.err.println("Connection lost! Cannot send data to the client!");
            ioe.printStackTrace();
            System.exit(1);
        }
    }
}