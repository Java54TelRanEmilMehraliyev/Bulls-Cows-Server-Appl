package telran;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BullsCowsServerAppl {
	private static final int PORT = 12345;

	public static void main(String[] args) {
		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			System.out.println("Server is listening on port " + PORT);

			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("New client connected");

				new Thread(new ClientHandler(socket)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}