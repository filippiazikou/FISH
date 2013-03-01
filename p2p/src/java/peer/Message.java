/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package peer;

import java.io.Serializable;
import java.util.LinkedList;

/**
 *
 * @author filippia zikou, georgios paralikidis
 * 
 * Type of messages
 * 1. CONNECT - HOST - PORT
 * 2. SEARCH - FILENAME - SENDER - TTL
 * 3. SEARCHRES - searchres
 */
public class Message implements Serializable{
    private String type;
    private String filename;
    private String host;
    private int port;
    private String sender;
    private int ttl;
    private LinkedList<PeerInfo> searchres;

    /**
     * Message constructor to create a Message object to be transmitted over the network
     * @param type type of message: CONNECT, SEARCH, SEARCHRES
     * @param filename name of file to search for.
     * @param host host of sender
     * @param port port of sender
     * @param sender username of sender
     * @param ttl message time to live
     * @param searchres list of peers that contain the searched file
     */
    public Message(String type, String filename, String host, int port, String sender, int ttl, LinkedList<PeerInfo> searchres) {
        this.type = type;
        this.filename = filename;
        this.host = host;
        this.port = port;
        this.sender = sender;
        this.ttl = ttl;
        this.searchres = searchres;
    }

    /**
     * Get type of Message object
     * @return the type of message
     */
    public String getType() {
        return type;
    }

    /**
     * Get filename of Message object
     * @return the filename of message
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Get host name from Message object
     * @return the host that sent the message
     */
    public String getHost() {
        return host;
    }

    /**
     * Get port number from Message object
     * @return the port from which the message was sent
     */
    public int getPort() {
        return port;
    }

    /**
     * Get sender from Message object
     * @return the sender of message
     */
    public String getSender() {
        return sender;
    }

    /**
     * Get TTL value of Message object
     * @return current time to live of the message
     */
    public int getTtl() {
        return ttl;
    }

    /**
     * Get the list of peers that have the search file available
     * @return a list with peers that own the file searched
     */
    public LinkedList<PeerInfo> getSearchres() {
        return searchres;
    }
}
