/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package peer;

import java.io.Serializable;

/**
 *
 * @author filippia
 */
public class PeerInfo implements Serializable{
    private int port;
    private String host;

    /**
     * Constructor to create PeerInfo object for a specified host and port
     * @param port
     * @param host 
     */
    public PeerInfo(int port, String host) {
        this.port = port;
        this.host = host;
    }

    /**
     * Get port
     * @return int value indicating the port that this peer has
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Get host
     * @return String with the host name of the peer
     */
    public String getHost() {
        return host;
    }
   
    /**
     * Get username
     * @return String indicating the username in the format host:port
     */
    public String getUsername() {
        return this.host+":"+this.port;
    }
}
