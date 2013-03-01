/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package peer;

import peer.file.SharedFile;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Description: The Peer as a server
 * @version 1.0 (Beta)
 * @since 05 December
 * @author filippia zikou, georgios paralykidis
 */
public class PeerListen extends Thread implements Serializable {

    Peer mypeer;
    ServerSocket socket;
    String myusername;

    /**
     * Starts a server socket listening to the specified port number
     * @param hostToListen Host to listen at
     * @param portToListen Port to listen at
     * @param mypeer Instance of peer as a client
     */
    public PeerListen(String hostToListen, int portToListen, Peer mypeer) {
        this.mypeer = mypeer;
        this.myusername = hostToListen + ":" + portToListen;
        /*Listen For Connection*/
        try {
            //create an IP address and the server's socket to this address and port 2222
            InetAddress addr = InetAddress.getByName(hostToListen);
            this.socket = new ServerSocket(portToListen, 1000, addr);
            System.out.println("I listen to: " + hostToListen + ":" + portToListen);

        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }

    }

    /**
     * Running loop of the peer listening thread
     */
    @Override
    public void run() {
        try {
            while (true) {
                Socket peersocket = this.socket.accept();
                ChooseAction(peersocket);
                //peersocket.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ec) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ec);
        }
    }

    /**
     * Accepts a Message object that describes the command to be executed
     * @param s Socket from which to receive the command
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    private void ChooseAction(Socket s) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(s.getInputStream());
        Message msg = (Message) in.readObject();
        if (msg.getType().equals("CONNECT")) {
            mypeer.addNeighbor(msg.getHost(), msg.getPort());
        } else if (msg.getType().equals("SEARCH")) {
            LinkedList<PeerInfo> returnList = new LinkedList<PeerInfo>();
            /*search my files*/
            if (mypeer.searchMyFiles(msg.getFilename()) == true) {
                returnList.add(new PeerInfo(this.socket.getLocalPort(), String.valueOf(this.socket.getInetAddress())));
            }
            /*send request to neighbors except sender if ttl>0*/
            if (msg.getTtl() > 0) {
                for (int i = 0; i < mypeer.neighbours.size(); i++) {
                    if (!mypeer.neighbours.get(i).getUsername().equals(msg.getSender())) {
                        Socket socketToConnect = new Socket(mypeer.neighbours.get(i).getHost(), mypeer.neighbours.get(i).getPort());
                        ObjectOutputStream out2 = new ObjectOutputStream(socketToConnect.getOutputStream());
                        /*Send the request*/
                        Message msg2 = new Message("SEARCH", msg.getFilename(), null, 0, this.myusername, msg.getTtl() - 1, null);
                        out2.writeObject(msg2);
                        out2.flush();

                        /*Receive the response*/
                        ObjectInputStream in2 = new ObjectInputStream(socketToConnect.getInputStream());
                        Message msgres = (Message) in2.readObject();
                        
                        if (msgres.getType().equals("SEARCHRES")) {
                            for (int j = 0; j < msgres.getSearchres().size(); j++) {
                                returnList.add(msgres.getSearchres().get(j));
                            }
                        }
                        out2.close();
                        in2.close();
                        socketToConnect.close();
                    }
                }
            }
            /*send list to sender*/
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            /*Send the request*/
            Message msgres = new Message("SEARCHRES", msg.getFilename(), null, 0, this.myusername, 0, returnList);
            out.writeObject(msgres);
            out.flush();
            out.close();
        } else if (msg.getType().equals("FILEREQ")) {
            String fileName = mypeer.getSharedFolder() + msg.getFilename();
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            File myFile = new File(fileName);
            byte[] mybytearray = new byte[(int) myFile.length()];

            //Start a fileInputStream to read bytes from the requested file.
            //System.out.println("sdfd"+myFile);
            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);

            //Read as many bytes as the size of the file to be sent.
            bis.read(mybytearray, 0, mybytearray.length);
            out.write(mybytearray, 0, mybytearray.length);
            out.flush();
            out.close();
        }
        in.close();
    }

    /**
     * Close server socket
     */
    public void exit() throws IOException {
        this.socket.close();
        System.exit(1);
    }

    /**
     * Retrieves username
     * @return String with username in the format host:port
     */
    public String getUsername() {
        return this.myusername;
    }
}
