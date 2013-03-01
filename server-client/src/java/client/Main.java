package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    static String shared_file_path, server_address;
    static int server_port;

    public static void main(String[] args) throws IOException {

        //Checking the clients arguments. If not given any, then use default values.
        //File path that the shared files exist: 
        if (args.length > 1) {
            shared_file_path = args[0];
            //IP Address of the server:
            server_address = args[1];
            //Port on which the server listens:
            server_port = Integer.parseInt(args[2]);
        } else {
            // Use the default value: "C:/Shared Folder".
            shared_file_path = "C:/Shared Folder";
            //Use the default value: "localhost".
            server_address = "localhost";
            //Use the default port: 4444.
            server_port = 4444;
        }
        //Asking the user to optionally provide a username.
        String clientName = "";
        boolean flag = false;
        while (clientName.length() < 6 && !clientName.equals("0")) {
            if (flag) {
                System.out.println("Invalid username entered. Please try again.");
            }
            flag = true;
            BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Give a username to connect to server. (You can connect anonymously by entering 0)");
            System.out.print("Username must have at least 6 letters:");
            clientName = consoleIn.readLine();
        }
        new Client(shared_file_path, server_address, server_port, clientName);
    }
}