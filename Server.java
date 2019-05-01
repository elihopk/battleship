import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Runs a Battleship game between two clients.
 * 
 * @author Eli Hopkins
 * @version 1.0
 */
public class Server {
	private ServerSocket serv;
	private int conns;
	private Board redBoard;
	private Board blueBoard;
	private int turns;
	private ReentrantLock rwLock;
	private ObjectOutputStream redOut;
	private ObjectOutputStream blueOut;
	
	
	public static void main(String[] args) {
		new Server();
	}
	
	public Server() {
		System.out.println("Starting server...");
		
		turns = -1;
		conns = 0;
		
		rwLock = new ReentrantLock();
		
		try {
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
	
	protected class ServThread extends Thread {
		private Socket conn;
		private PlayerColor threadColor;
		private ObjectInputStream ois;
		private ObjectOutputStream oos;
		private Board thisBoard;
		private Board updatedBoard;
		
		public ServThread(Socket conn, PlayerColor threadColor) {
			this.conn = conn;
			this.threadColor = threadColor;
			ois = null;
			oos = null;
			try {
				ois = new ObjectInputStream(this.conn.getInputStream());
				oos = new ObjectOutputStream(this.conn.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (threadColor.equals(PlayerColor.RED)) {
				redOut = oos;
			} else {
				blueOut = oos;
			}
		}
		
		public void run() {
			// If not writing try adding flush
			try {
				oos.writeObject(threadColor);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (threadColor.equals(PlayerColor.BLUE)) {
				try {
					blueBoard = (Board) ois.readObject();
					rwLock.lock();
					redOut.writeObject(blueBoard);
					rwLock.unlock();
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
				thisBoard = blueBoard;
			} else {
				try {
					redBoard = (Board) ois.readObject();
					rwLock.lock();
					blueOut.writeObject(blueBoard);
					rwLock.unlock();
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
				thisBoard = redBoard;
			}
			
			// Adjust value for number of hits to win game
			while (thisBoard.getHits() < 17) {
				if (thisBoard.getPlayerColor() == PlayerColor.RED && turns == -1) {
					try {
						oos.writeObject(blueBoard);
						updatedBoard = (Board) ois.readObject();
						if (blueBoard.getHits() < updatedBoard.getHits()) {
							
						}
					} catch (IOException | ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
