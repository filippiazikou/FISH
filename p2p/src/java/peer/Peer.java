/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package peer;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import peer.Command.CommandName;
import peer.file.FileFinder;
import peer.file.SharedFile;

/**
 * Description: The Peer as a client
 * @version 1.0 (Beta)
 * @since 05 December
 * @author filippia zikou, georgios paralykidis
 */
public class Peer {

    private String sharedFolder;
    private String downloadsFolder;
    private List<SharedFile> mysharedfiles;
    private List<SharedFile> mydownloadfiles;
    protected LinkedList<PeerInfo> neighbours;
    private PeerListen peerlisten;
    private String username;
    
    /**
     * Peer constructor to create a Peer for a specific host, port, shared folder and downloads folder
     * @param hostToConnect host of a known peer to connect to
     * @param portToConnect peer of a known peer to connect to
     * @param sharedFolder location of the folder to share
     * @param downloadsFolder location of the download files
     */
    public Peer(String hostToConnect, int portToConnect, String sharedFolder, String downloadsFolder) {

        this.username = new String();
        this.peerlisten = null;
        this.sharedFolder = sharedFolder;
        this.downloadsFolder = downloadsFolder;

        this.mysharedfiles = new ArrayList<SharedFile>();
        this.mydownloadfiles = new ArrayList<SharedFile>();
        this.neighbours = new LinkedList<PeerInfo>();
    }

    /**
     * Main function of the peer
     * @param args
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        int portToConnect = 0;
        int portToListen = 0;
        String hostToConnect = null;
        String hostToListen = null;

        Peer mypeer;

        //Read Host and Port of the Peer to Connect
        BufferedReader consoleIn;
        consoleIn = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Host of known peer to connect:");
        hostToConnect = consoleIn.readLine();
        System.out.print("Port of known peer to connect:");

        try {
            portToConnect = Integer.parseInt(consoleIn.readLine());
        } catch (NumberFormatException ne) {
            System.out.println("Port should be number!");
            System.exit(1);
        }

        //My Host And My Port inputs
        System.out.print("My Listening Host:");
        hostToListen = consoleIn.readLine();
        System.out.print("My Listening Port:");
        try {
            portToListen = Integer.parseInt(consoleIn.readLine());
        } catch (NumberFormatException ne) {
            System.out.println("Port is a number!");
            System.exit(1);
        }

        //Choose Folder To Share and Download direction 
        System.out.print("Folder Location to Share:");
        String sharedF = consoleIn.readLine();
        System.out.print("Download Direction:");
        String downloadF = consoleIn.readLine();

        /*Create The peer*/
        mypeer = new Peer(hostToConnect, portToConnect, sharedF, downloadF);

        /*Make peer peerlisten for connections*/
        mypeer.peerlisten = new PeerListen(hostToListen, portToListen, mypeer);
        mypeer.peerlisten.start();
        Thread.sleep(1000);
        if (!hostToConnect.equals("null")) {
            /*Inform Connected Peer*/
            mypeer.InformPeer(hostToConnect, portToConnect, hostToListen, portToListen);
            /*Update my neighbors*/
            mypeer.addNeighbor(hostToConnect, portToConnect);
        }
        /*Set username*/
        mypeer.setUsername(hostToListen, portToListen);

        /*Check if downloads folder exist or create it*/
        mypeer.checkDownloadFolder();

        /*Get Shared Folder Info*/
        mypeer.checkSharedFolder();

        /*Start Console*/
        mypeer.ChooseAction();

    }


    /**
     * Update Connected Peer neighbors
     * @param hostToConnect host of peer to connect to
     * @param portToConnect port of peer to connect to
     * @param hostToListen host peer is listening
     * @param portToListen port the peer is listening
     * @throws UnknownHostException
     * @throws IOException
     */
    public void InformPeer(String hostToConnect, int portToConnect, String hostToListen, int portToListen) throws UnknownHostException, IOException {
        Socket socketToConnect = new Socket(hostToConnect, portToConnect);
        ObjectOutputStream out = new ObjectOutputStream(socketToConnect.getOutputStream());

        Message msg = new Message("CONNECT", null, hostToListen, portToListen, null, 0, null);
        out.writeObject(msg);
        out.flush();

        out.close();
        socketToConnect.close();
    }

    /** 
     * Generate username from host and port number
     * @param host host to generate username from
     * @param port port to generate username from
     */
    public void setUsername(String host, int port) {
        this.username = host + ":" + port;
    }

    /**
     * Add a peer to the neighbors list
     * @param host host of the new peer to be added to the list
     * @param port port of the new peer to be added to the list
     */
    public synchronized void addNeighbor(String host, int port) {
        this.neighbours.add(new PeerInfo(port, host));
        System.out.println("Added to my neighbors: " + host + ":" + port);
        System.out.print(this.username + "@FISH>");
    }

    /**
     * Read command from user
     * @throws IOException 
     */
    public void ChooseAction() throws IOException {
        while (true) {
            System.out.print(this.username + "@FISH>");
            try {
                BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
                String userInput = consoleIn.readLine();

                execute(parse(userInput));

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(this.username).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Execute command read from user
     * @param command the command to be executed(mydownloadfiles, mysharedfiles, search, help, exit)
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    void execute(Command command) throws IOException, ClassNotFoundException {
        //This is the reader that will read the user input
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        if (command == null) {
            return;
        }
        switch (command.getCommandName()) {
            case mysharedfiles:
                checkSharedFolder();
                printShares();
                break;
            case mydownloadfiles:
                checkDownloadFolder();
                printDownloads();
                break;
            case search:
                String searchname = searchName();
                List<PeerInfo> results = new ArrayList<PeerInfo>();
                results = searchFile(searchname);
                if (results == null || results.isEmpty()) {
                    System.out.println("no results found");
                    break;
                } else {
                    for (int i = 0; i < results.size(); i++) {
                        System.out.println(i + 1 + ". " + results.get(i).getUsername());
                    }
                }
                int responseInt;
                System.out.print("To select a file please choose a peer from the list (Press 0 to choose nothing): ");
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
                } else if (responseInt > results.size()) {
                    System.out.println("The number you have put is not in the list. Please type list to see the files again.");
                    break;
                }
                //this.getFile(this.neighbours.get(responseInt-1), searchname);
                this.getFile(new PeerInfo(results.get(responseInt-1).getPort(), results.get(responseInt-1).getHost()), searchname);
                break;

            case exit:
                this.peerlisten.exit();
                System.out.println("Successfully logged out!");
                System.exit(1);
            case help:
                System.out.println("You can choose to execute one of the following operations:");
                System.out.println("search       - search for a file and download.");
                System.out.println("mydownloadfiles      - display me downloaded files.");
                System.out.println("mysharedfiles      - display me downloaded files.");
                System.out.println("exit         - Logout from system.");
                break;
        }
    }

    /**
     * Parse a string and return a Command object to be executed
     * @param userInput String format of the command to be executed
     * @return Command object describing the command to be executed
     */
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

    /**
     * Updates download list
     */
    private void checkDownloadFolder() {
        FileFinder ff = new FileFinder();
        this.mydownloadfiles = ff.findNow(this.downloadsFolder);
    }

    /**
     * Updates shared list
     */
    private void checkSharedFolder() {
        FileFinder ff = new FileFinder();
        this.mysharedfiles = ff.findNow(this.sharedFolder);
    }

    /**
     * Print shared files
     */
    private void printShares() {
        System.out.println("My Shared Files:");
        for (int i = 0; i < mysharedfiles.size(); i++) {
            System.out.println(i + ". " + mysharedfiles.get(i).getName() + " with size: " + mysharedfiles.get(i).getSize());
        }
    }

    /**
     * Print downloaded files
     */
    private void printDownloads() {
        System.out.println("My Downloaded Files:");
        for (int i = 0; i < mydownloadfiles.size(); i++) {
            System.out.println(i + ". " + mydownloadfiles.get(i).getName() + " with size: " + mydownloadfiles.get(i).getSize());
        }
    }

    /**
     * Read file name to search for from user input
     * @return String containing the user input file name
     * @throws IOException 
     */
    private String searchName() throws IOException {
        BufferedReader consoleIn;
        consoleIn = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Name of Search File:");
        return consoleIn.readLine();
    }

    /**
     * Send request to neighbors to look for a file
     * @param filename String containing the filename to search for
     * @return List with the peers that have this file
     * @throws UnknownHostException
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    private List searchFile(String filename) throws UnknownHostException, IOException, ClassNotFoundException {
        LinkedList<PeerInfo> res = new LinkedList<PeerInfo>();
        /*Ask all neighbors*/
        for (int i = 0; i < this.neighbours.size(); i++) {
            Socket socketToConnect = new Socket(this.neighbours.get(i).getHost(), this.neighbours.get(i).getPort());
            ObjectOutputStream out = new ObjectOutputStream(socketToConnect.getOutputStream());
            int ttl = 5;

            /*Send the request*/
            Message msg = new Message("SEARCH", filename, null, 0, peerlisten.getUsername(), ttl, null);
            out.writeObject(msg);
            out.flush();


            /*Receive the response*/
            ObjectInputStream in = new ObjectInputStream(socketToConnect.getInputStream());
            Message msgres = (Message) in.readObject();
            
            if (msgres.getType().equals("SEARCHRES")) {
                for (int j=0 ; j<msgres.getSearchres().size() ; j++) {
                    res.add(msgres.getSearchres().get(j));
                }
            }
            out.close();
            in.close();
            socketToConnect.close();

        }
        return res;
    }

    /**
     * Search for a specific file into my shared folder
     * @param name String containing the file name to search for
     * @return true if the file is found, false otherwise
     */
    public boolean searchMyFiles(String name) {
        //Update
        this.checkSharedFolder();
        //Search
        for (int i = 0; i < this.mysharedfiles.size(); i++) {
            if (name.equals(this.mysharedfiles.get(i).getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Requests a file from the owning peer and places it in the downloaded folder
     * @param from PeerInfo of the peer to get file from
     * @param filename String with the file name of the file to get from the peer
     * @throws UnknownHostException
     * @throws IOException 
     */
    private void getFile(PeerInfo from, String filename) throws UnknownHostException, IOException {
        /*Send The request*/
        String host[] = from.getHost().split("/");
        Socket socketToConnect = new Socket(host[0], from.getPort());
        ObjectOutputStream out = new ObjectOutputStream(socketToConnect.getOutputStream());
        /*Send the request*/
        Message msg = new Message("FILEREQ", filename, null, 0, null, 0, null);
        out.writeObject(msg);
        out.flush();

        /*Receive the file*/
        //Initialise a fileOutputStream to write the received bytes to the local disk.
        FileOutputStream fos = new FileOutputStream(this.downloadsFolder + filename);
        //Read the file from the BufferedInputStream and write to the local file.
        ObjectInputStream in = new ObjectInputStream(socketToConnect.getInputStream());
        int bytesRead = 0;
        int counter = 0;
        long start = System.currentTimeMillis();
        byte[] buffer = new byte[100000];
        while (bytesRead >= 0) {
            bytesRead = in.read(buffer);
            if (bytesRead >= 0) {
                fos.write(buffer, 0, bytesRead);
                counter += bytesRead;
            }
            if (bytesRead < 1024) {
                fos.flush();
                break;
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("Total Bytes read: " + counter);
        System.out.println("File was successfully downloaded in: " + (end - start) + " Milisecconds.");
        in.close();
        out.close();
        fos.close();

    }

    /**
     * Gets the path of the shared folder
     * @return String containing the path of the shared folder
     */
    public String getSharedFolder() {
        return this.sharedFolder;
    }
}
