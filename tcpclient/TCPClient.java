package tcpclient;
import java.net.*;
import java.io.*;

public class TCPClient {

	static int BUFFERSIZE = 1024;
	boolean shutdown;
	Integer timeout;
	Integer limit;

	public TCPClient(boolean shutdown, Integer timeout, Integer limit) {
		this.shutdown = shutdown;
		this.timeout = timeout;
		this.limit = limit;
	}

	public byte[] askServer(String hostname, int port, byte[] toServerBytes) throws IOException {

		// Buffers
		ByteArrayOutputStream dataBuffer = new ByteArrayOutputStream();
		byte[] readBuffer = new byte[BUFFERSIZE];

		// Open socket
		Socket clientSocket = new Socket(hostname, port);

		// Output
		clientSocket.getOutputStream().write(toServerBytes);

		// Shutdown, Timer and Limit
		if (this.shutdown) {
			clientSocket.shutdownOutput();
		}
		if (this.timeout != null) {
			clientSocket.setSoTimeout(this.timeout);
		}
		int limit = (this.limit == null) ? 0 : this.limit;

		// Input
		try {
			while (true) {
				int len;
				if (this.limit != null) {
					if (limit <= 0) {
						break;
					}
					len = Integer.min(BUFFERSIZE, limit);
				}
				else {
					len = BUFFERSIZE;
				}
				int fromServerLength = clientSocket.getInputStream().read(readBuffer, 0 , len);
				if (fromServerLength == -1) {
					break;
				}
				limit -= fromServerLength;
				dataBuffer.write(readBuffer, 0, fromServerLength);
			}
		} catch (SocketTimeoutException e) {

			// Close socket and return
			clientSocket.close();
			return dataBuffer.toByteArray();
		}

		// Close socket and return
		clientSocket.close();
		return dataBuffer.toByteArray();
	}
}
