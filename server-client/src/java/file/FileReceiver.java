package file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Description of SharedFile class:<br> This class is used to represent in code
 * level the files that a client in Fish Project can share from a local folder.
 * It contains all the necessary details like name, size, extension etc.
 *
 * @author Georgios Paralykidis
 * @author Filippia Zikou
 *
 * @version 1.00a (Beta)
 *
 * @since Java 1.6 - 03 December 2012.
 *
 * @param IP The IP Address from where the file will be retrieved.
 * @param port The port from where the file will be retrieved.
 * @param fileName The name of the file with the extension.
 * @param size The size of the file in Bytes.
 */
public class FileReceiver extends Thread {

    /**
     * Description of SharedFile class:
     */
    private String IP, fileName;
    private int port, size;
    /*
     * @param IP The IP Address from where the file will be retrieved.
     * @param port The port from where the file will be retrieved.
     * @param fileName The name of the file with the extension.
     * @param size The size of the file in Bytes.
     */

    public FileReceiver(String IP, int port, String fileName, int size) {
        this.IP = IP;
        this.port = port;
        this.size = size;
        this.fileName = fileName;
    }

    @Override
    public void run() {
        //Connect to Server
        ObjectInputStream in = null;
        BufferedOutputStream out = null;

        Socket clientSocket = null;
        try {
            // Try to connect to the other client.
            System.out.println("Connecting...");
            InetAddress addr = InetAddress.getByName("localhost");
            clientSocket = new Socket(addr, port);
            System.out.println("Connected to " + IP + ":" + port);
            try {
                //Initialize Communication Buffers
                in = new ObjectInputStream(clientSocket.getInputStream());
                out = new BufferedOutputStream(clientSocket.getOutputStream());

                // Initiating values:
                long start = System.currentTimeMillis();
                byte[] buffer = new byte[100000];
                int bytesRead = 0, counter = 0;

                //Send the name of the file it will download:
                byte[] toServer = fileName.getBytes();
                out.write(toServer, 0, toServer.length);
                out.flush();

                //Initialise a fileOutputStream to write the received bytes to the local disk.
                FileOutputStream fos = new FileOutputStream("C:/Fish Downloads/" + fileName);

                //Read the file from the BufferedInputStream and write to the local file.
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
                out.close();
                fos.close();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println(e.toString());
                System.exit(1);
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + IP + ":" + port);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + IP + ":" + port);
        }
    }
}