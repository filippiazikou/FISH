package file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileDistributor extends Thread {

    private ServerSocket serverSocket;
    private BufferedInputStream in;
    private ObjectOutputStream out;
    private Socket clientSocket;
    private String shared_folder;
    String fileName = "";

    public FileDistributor(ServerSocket serverSocket, String shared_folder) throws IOException {
        this.serverSocket = serverSocket;
        this.shared_folder = shared_folder;
    }

    @Override
    public void run() {
        while (true) {
            try {
                //New client appears:
                clientSocket = serverSocket.accept();
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new BufferedInputStream(clientSocket.getInputStream());
            } catch (IOException ex) {
                Logger.getLogger(FileDistributor.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Accepted connection from address: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

            byte[] msg = new byte[25];
            try {
                //Read the name of the file to Distribute:
                in.read(msg, 0, msg.length);
                fileName = new String(msg).replaceAll("\\s", "");

                //Create a new file and directory if that does not exist:
                File myFile = new File(shared_folder + "\\" + fileName);

                //Initialize variables:
                long fileSize = myFile.length();
                long completed = 0;
                int step = 100000;

                //Start a fileInputStream to read bytes from the requested file.
                FileInputStream fis = new FileInputStream(myFile);

                // Start sending the requested file.
                System.out.println("Start sending file: " + fileName);
                byte[] buffer = new byte[step];
                while (completed <= fileSize) {
                    fis.read(buffer);
                    out.write(buffer);
                    completed += step;
                }
                out.flush();
                System.out.println("Sending file " + fileName + " completed successfully!");
            } catch (IOException ex) {
                Logger.getLogger(FileDistributor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}