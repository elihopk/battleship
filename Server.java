import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * Runs a Battleship game between two clients.
 * 
 * @author Eli Hopkins
 * @version 1.0
 */
public class Server {
	// Declare Variables
	private ServerSocket serv;
	private int conns;
	private Board redBoard;
	private Board blueBoard;
	/**
	 * Start the server.
	 * 
	 * @param args Command-line arguments
	 */
	public static void main(String[] args) {
		Server serv = new Server();
	}
	
	/**
	 * Initializes variables and starts server threads.
	 */
	public Server() {
		System.out.println("Starting server...");
		
		new ChatServer();
		
		conns = 0;
		
		try {
			// Allow 2 clients to connect
			while (conns < 2) {
				serv = new ServerSocket(28888);
			
				Socket conn = serv.accept();
				
				conns++;
				
				if (conns == 1) {
					new ServThread(conn, PlayerColor.BLUE).start();
				} else {
					new ServThread(conn, PlayerColor.RED).start();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Handles each individual connection.
	 * 
	 * @author Eli Hopkins
	 * @version 1.0
	 */
	protected class ServThread extends Thread {
		// Declare Variables
		private Socket conn;
		private PlayerColor threadColor;
		private ObjectInputStream ois;
		private ObjectOutputStream oos;
		private Board thisBoard;
		private int turns;
		
		/**
		 * Initialize variables.
		 * 
		 * @param conn The client connection
		 * @param threadColor The player's color
		 */
		public ServThread(Socket conn, PlayerColor threadColor) {
			// Initialize variables
			this.conn = conn;
			this.threadColor = threadColor;
			ois = null;
			oos = null;
			turns = -1;
			try {
				ois = new ObjectInputStream(this.conn.getInputStream());
				oos = new ObjectOutputStream(this.conn.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void run() {
			// If not writing try adding flush
			// Tell client what player color they are
			try {
				oos.writeObject(threadColor);
				oos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// Check playercolor of this thread
			if (threadColor.equals(PlayerColor.BLUE)) {
				// Get initial board
				try {
					blueBoard = (Board) ois.readObject();
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
				thisBoard = blueBoard;
			} else {
				try {
					redBoard = (Board) ois.readObject();
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
				thisBoard = redBoard;
			}
			
			// Blue player goes first
			if (turns == -1 && threadColor.equals(PlayerColor.RED)) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			// Adjust value for number of hits to win game
			// This is the game loop
			while (redBoard.getHits() < 17 && blueBoard.getHits() < 17) {
				// Each turn send and receive a new board
				if (thisBoard.getPlayerColor().equals(PlayerColor.RED)) {
					try {
						oos.writeObject(blueBoard);
						oos.writeObject(redBoard);
						oos.flush();
						blueBoard = (Board) ois.readObject();
					} catch (IOException | ClassNotFoundException e) {
						e.printStackTrace();
					}
				} else {
					try {
						oos.writeObject(redBoard);
						oos.writeObject(blueBoard);
						oos.flush();
						redBoard = (Board) ois.readObject();
					} catch (IOException | ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
				// Count the turn, notify the other thread, then wait for next turn
				turns++;
				notifyAll();
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Handles the chat server.
	 * 
	 * @author Esteban Cruz
	 * @version 1.0
	 */
	protected class ChatServer {

		// Attributes
		private Vector<PrintWriter> vpw = new Vector<PrintWriter>();
		private ServerSocket sc;
		private final int PORT = 16789;

		public ChatServer() {
			try {
				sc = new ServerSocket(PORT);

				System.out.println("Chat Server is listening on port: " + PORT);

				System.out.println("Server IP Address: " + InetAddress.getLocalHost().getHostAddress());

				while (true) {
					// Accept connection
					Socket accept = sc.accept();

					System.out.println("A client has connected!");

					ChatServerThread cst = new ChatServerThread(accept);
					cst.start();
				}
			} catch (Exception e) {

				System.out.println("Exception error: " + e.getMessage());

			}
		}

		class ChatServerThread extends Thread {

			// Attributes
			private Socket accept;

			public ChatServerThread(Socket _accept) {
				this.accept = _accept;
			}

			public void run() {
				String msg;

				try {
					InputStream in = accept.getInputStream();
					BufferedReader bin = new BufferedReader(new InputStreamReader(in));

					OutputStream out = accept.getOutputStream();
					PrintWriter pout = new PrintWriter(new OutputStreamWriter(out));

					vpw.add(pout);

					while ((msg = bin.readLine()) != null) {
						for (PrintWriter tempvpw: vpw) {
							tempvpw.println(msg);
							tempvpw.flush();
						}
					}

					if (msg == null) {
						vpw.remove(pout);
						bin.close();
						pout.close();
						accept.close();
						System.out.println("A client has disconnected!");
					}
				} catch (Exception e) {

					System.out.println("Exception error: " + e.getMessage());

				}
			}
		} 
	} // End of class ChatServer
}