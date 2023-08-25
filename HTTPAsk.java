import tcpclient.TCPClient;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HTTPAsk {
    static int BUFFERSIZE = 1024;

    public static void main(Socket clientSocket) {
        try {

            // Buffers
            ByteArrayOutputStream dataBuffer = new ByteArrayOutputStream();
            byte[] readBuffer = new byte[BUFFERSIZE];

            // Input
            int fromServerLength;
            while ((fromServerLength = clientSocket.getInputStream().read(readBuffer)) != -1) {
                dataBuffer.write(readBuffer, 0, fromServerLength);
                if (dataBuffer.toString().endsWith("\r\n\r\n"))
                    break;
            }

            // Data
            String response = "";
            String[] rows = dataBuffer.toString().split("\n");
            String[] row0 = rows[0].split("[& =?]");
            String hostname = "";
            int port = 0;
            boolean shutdown = false;
            Integer limit = null;
            Integer timeout = null;
            String string = "";

            // Check data
            if (row0[0].equals("GET")
                    && row0[row0.length - 1].subSequence(0, row0[row0.length - 1].length() - 1).equals("HTTP/1.1")) {

                // Check \ask
                if (row0[1].equals("/ask")) {
                    // Iterate through first row after /ask
                    for (int i = 2; i < row0.length; i++) {
                        switch (row0[i]) {
                            case "hostname" -> hostname = row0[i + 1];
                            case "port" -> port = Integer.parseInt(row0[i + 1]);
                            case "shutdown" -> shutdown = true;
                            case "limit" -> limit = Integer.parseInt(row0[i + 1]);
                            case "timeout" -> timeout = Integer.parseInt(row0[i + 1]);
                            case "string" -> string = row0[i + 1];
                        }
                    }

                    // For checking parameters
                    boolean ok = true;

                    // Check port and hostname
                    if (!(port >= 0 && port <= 65536 && hostname.length() > 0)) {
                        ok = false;
                    }
                    // Check limit
                    else if (limit != null) {
                        if (!(limit > 0)) {
                            ok = false;
                        }
                    }
                    // Check timeout
                    else if (timeout != null) {
                        if (!(timeout > 0)) {
                            ok = false;
                        }
                    }

                    // Parameters ok
                    if (ok) {
                        // Set string to default if not specified
                        byte[] stringBytes = new byte[0];
                        if (string.length() > 0) {
                            stringBytes = (string + "\n").getBytes(StandardCharsets.UTF_8);
                        }
                        // Connect to TCPClient and create response
                        try {
                            TCPClient tcpClient = new tcpclient.TCPClient(shutdown, timeout, limit);
                            byte[] serverBytes = tcpClient.askServer(hostname, port, stringBytes);
                            String serverOutput = new String(serverBytes);
                            response = "HTTP/1.1 200 OK\r\n\r\n" + serverOutput;

                        } catch (IOException e) {
                            System.err.println(e);
                            System.exit(1);
                        }
                    }
                    // Parameters not ok
                    else {
                        response = "HTTP/1.1 400 Bad Request\r\n\r\n";
                    }
                }
                // Not Found, no /ask
                else {
                    response = "HTTP/1.1 404 Not Found\r\n\r\n";
                }
            }
            // Bad Request, no GET or HTTP/1.1
            else {
                response = "HTTP/1.1 400 Bad Request\r\n\r\n";
            }

            // Output
            try {
                // System.out.println(response);
                clientSocket.getOutputStream().write(response.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                System.err.println(e);
                System.exit(1);
            }

            // Close socket
            clientSocket.close();

        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }
    }
}
