import tcpclient.TCPClient;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ConcHTTPAsk {

    private static class MyRunnable implements Runnable {
        Socket clientSocket;

        public MyRunnable(Socket clientSocket){
            this.clientSocket = clientSocket;
        }

        @Override public void run() {
            HTTPAsk.main(clientSocket);
        }
    }

    public static void main(String[] args) {
        try {
            // Serversocket
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));

            // Main loop
            while (true) {
                //System.out.println("JO");
                Socket clientSocket = serverSocket.accept();
                MyRunnable myRunnable = new MyRunnable(clientSocket);
                Thread thread = new Thread(myRunnable);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }
    }
}