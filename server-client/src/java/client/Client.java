package client;

import file.SharedFile;
import file.FileReceiver;
import file.FileFinder;
import file.FileDistributor;
import client.Command.CommandName;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client extends Thread {

    String shared_folder, server_address, clientName;
    int server_port;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket serverSocket;
    private ResultList resultList;
    private ServerSocket serverSocket2 = null;
    private boolean flag = false;

    public Client(String shared_folder, String server_address, int server_port, String clientName) throws IOException {
        this.shared_folder = shared_folder;
        this.server_address = server_address;
        this.server_port = server_port;
        this.clientName = clientName;
        run();
    }

    @Override
    public void run() {
        try {
            serverSocket = new Socket(server_address, server_port);
            //Initialize Communication Buffers
            out = new ObjectOutputStream(serverSocket.getOutputStream());
            in = new ObjectInputStream(serverSocket.getInputStream());
            if (clientName.equals("0")) {
                System.out.println("Connected to server anonymously.");
            } else {
                System.out.println("Connected to server as: " + clientName + ".");
            }
        } catch (UnknownHostException e) {
            System.err.println("Could not find host at address: " + server_address + ":" + server_port);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + server_address + ":" + server_port);
        }

        //Send name of the client to server.
        sendCommand("LOGIN%" + clientName);
        BufferedReader consoleIn;
        consoleIn = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print(clientName + "@FISH>");
            try {
                String userInput = consoleIn.readLine();
                try {
                    try {
                        execute(parse(userInput));
                    } catch (SQLException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void execute(Command command) throws IOException, ClassNotFoundException, SQLException {
        //This is the reader that will read the user input
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        //This is where the user's responses are going to be stored:        
        String responseString = "";
        int responseInt;
        int fileSize = 0;
        String fileIP = "localhost", shared;
        String fileName = "";
        SharedFile[] sharedFiles = null;
        if (command == null) {
            return;
        }
        BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));

        switch (command.getCommandName()) {

            case list: // If user wants to see all files avaiable, or search for a specific file: 
                System.out.println("You can define what files you are looking for. Options are:");
                System.out.println("all          - get all available files.");
                System.out.println("name example - Search for files that have a specific name.");
                System.out.println(".txt         - Search for files that have a specific extension.");
                System.out.println("min 100      - Search for files with minimum size (in KBytes).");
                System.out.println("max 500      - Search for files with minimum size (in KBytes).");
                System.out.print("Please enter your choice:");
                responseString = in.readLine();
                responseString = responseString.replace(" ", ""); // We replace spaces given by user.
                responseString = responseString.toUpperCase();
                if (responseString.equals("ALL")) { //In case user wants to get all available files.
                    sendCommand("LIST%ALL");
                } else if (responseString.startsWith(".")) { //In case user wants a specific type of file.
                    sendCommand("LIST%" + responseString);
                } else if (responseString.startsWith("MIN")) { //In case user sets minimum size limit.
                    sendCommand("LISTMIN%" + responseString.replace("MIN", ""));
                } else if (responseString.startsWith("MAX")) { //In case user sets maximum size limit.
                    sendCommand("LISTMAX%" + responseString.replace("MAX", ""));
                } else {
                    sendCommand("LIST%" + responseString); //In case user wants a specific name of a file.
                }
                try {
                    resultList = (ResultList) getResponse();
                } catch (Exception e) {
                }

                //The client lists all the available files.
                System.out.println("============================Files Available============================");
                if (resultList.size() == 0) {
                    System.out.println("There are no files that are currently being shared!");
                }
                for (int i = 0; i < resultList.size(); i++) {
                    System.out.println(i + 1 + ") " + resultList.get(i).getName() + "." + resultList.get(i).getExtension() + " with size " + resultList.get(i).getSize() + " Bytes from user: " + resultList.get(i).getOwner() + ".");
                }
                System.out.println("=======================================================================");
                System.out.print("To select a file please type its number on the list (Press 0 to choose nothing): ");
                try { // User enters the number of the file he wants to get.
                    responseInt = Integer.parseInt(in.readLine());
                } catch (NumberFormatException nfe) { // In case user doesn't insert number.
                    System.out.println("Invalid input. Please type list to see the files again.");
                    break;
                } catch (IOException e) { // In case of an I/O connection error.
                    System.out.println("An error occured. Please try to list files again");
                    break;
                }
                if (responseInt == 0) { // If response is 0 then return to the prrevious menu.
                    break;
                } else if (responseInt > resultList.size()) {
                    System.out.println("The number you have put is not in the list. Please type list to see the files again.");
                    break;

                }
                //The client send a request to the server to ask the IP of the owner of the chosen file.
                sendCommand("GIVEIP%" + Integer.toString(resultList.get(responseInt - 1).getID()));
                fileName = resultList.get(responseInt - 1).getName() + "." + resultList.get(responseInt - 1).getExtension();
                fileSize = ((int) resultList.get(responseInt - 1).getSize());
                //Client receives the IP Address and port of the requested file:
                try {
                    fileIP = (String) getResponse();
                } catch (Exception e) {
                }
                //Connects to owner to get the file.
                new FileReceiver(fileIP, 8888, fileName, fileSize).start();
                break;

            case share:
                sendCommand("SHARE% ");
                if ("GIVE FILES".equals((String) getResponse())) {
                    // At first the client finds the files that exist into the shared folder.
                    sharedFiles = FileFinder.findNow(shared_folder);
                    // Here the client sends all the files that it has into the shared folder to the server.
                    sendCommand(sharedFiles);
                }
                shared = (String) getResponse();
                if (shared.equals("OK")) { // If the sharing has been successful.
                    if (!flag) {
                        openServerSocket();
                        new FileDistributor(serverSocket2, shared_folder).start();
                    }
                    System.out.println("Your files are now being shared!");
                } else if (shared.equals("NOT OK")) { // If something has gone bad.
                    System.out.println("There was a problem putting for files online. Please try again.");
                }
                break;

            case unshare:
                sendCommand("UNSHARE% ");
                shared = (String) getResponse();
                if (shared.equals("OK")) {// If the unsharing has been successful.
                    System.out.println("Your files are not being shared!");
                } else if (shared.equals("NOT OK")) { // If something has gone bad.
                    System.out.println("There was a problem putting for files offline. Please try again.");
                }
                break;

            case changefolder:
                System.out.print("Please give the full path of the new shared folder:");
                shared_folder = in.readLine();
                break;

            case exit:
                sendCommand("LOGOUT% ");
                serverSocket.close();
                System.out.println("Successfully logged out from server! Bye!");
                System.exit(1);

            case help:
                System.out.println("You can choose to execute one of the following operations:");
                System.out.println("list         - List shared files and choose what to download.");
                System.out.println("share        - Share files that you have in your shared folder.");
                System.out.println("unshare      - Stop sharing files that you are sharing now.");
                System.out.println("changefolder - Change your default sharing folder.");
                System.out.println("exit         - Logout from the server.");
                break;

            case currentfolder:
                System.out.println("The folder currently shared on Fish Server is: " + shared_folder);
                break;
        }
    }

    private Command parse(String userInput) {
        if (userInput == null) {
            return null;
        }
        StringTokenizer tokenizer = new StringTokenizer(userInput);
        if (tokenizer.countTokens() == 0) {
            return null;
        }

        CommandName commandName = null;
        String userName = null;
        float amount = 0;
        int userInputTokenNo = 1;

        while (tokenizer.hasMoreTokens()) {
            switch (userInputTokenNo) {
                case 1:
                    try {
                        String commandNameString = tokenizer.nextToken();
                        commandName = CommandName.valueOf(CommandName.class, commandNameString);
                    } catch (IllegalArgumentException commandDoesNotExist) {
                        System.out.println("Illegal command");
                        return null;
                    }
                    break;
                case 2:
                    try {
                        amount = Float.parseFloat(tokenizer.nextToken());
                    } catch (NumberFormatException e) {
                        System.out.println("Illegal amount");
                        return null;
                    }
                    break;
                default:
                    System.out.println("Illegal command");
                    return null;
            }
            userInputTokenNo++;
        }
        return new Command(commandName, amount);
    }

    private void openServerSocket() {
        try {
            InetAddress addr = InetAddress.getByName("localhost");
            serverSocket2 = new ServerSocket(8888, 1000, addr);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        flag = true;
    }

    @SuppressWarnings("empty-statement")
    private Object getResponse() throws ClassNotFoundException {
        Object response = null;
        try {
            while ((response = in.readObject()) == null);
        } catch (IOException ioe) {
            System.err.println("Connection lost! Cannot read data from the server!");
            System.exit(1);
        }
        return response;
    }

    private void sendCommand(Object object) {
        try {
            out.writeObject(object);
            out.flush();
        } catch (IOException ioe) {
            System.err.println("Connection lost! Cannot send data to the server!");
            System.exit(1);
        }
    }
}