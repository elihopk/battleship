/**
 * Represents a battleship board and tracks board state.
 * 
 * @author Eli Hopkins
 * @version 1.0
 */
public class Board {
	// Create variables to track board state and owner
	private int[][] board;
	private PlayerColor pcolor;
	private int hits;
	
	/*
	 * Initializes the board.
	 * 
	 * @param pcolor The color of the player who owns this board
	 */
	public Board(PlayerColor pcolor) {
		// Initialize variables
		board = new int[9][9];
		this.pcolor = pcolor;
		hits = 0;
		
		// Initialize the board as empty
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				board[i][j] = 0;
			}
		}
	}
	
	/**
	 * Checks if the target location is filled.
	 * 
	 * @param x X value to check
	 * @param y Y value to check
	 * @return boolean Whether the location is filled
	 */
	public boolean checkHit(int x, int y) {
		return board[x][y] == 2;
	}
	
	public boolean tryHit(int x, int y) {
		if (board[x][y] == 1) {
			hits++;
			board[x][y] = 2;
			return true;
		} else {
			board[x][y] = 3;
		}
		
		return false;
	}
	
	/**
	 * Add a piece to a specific coordinate.
	 * 
	 * @param x X value to fill
	 * @param y Y value to fill
	 */
	public void addShipPiece(int x, int y) {
		// Set coordinate to true
		board[x][y] = 1;
	}
	
	/**
	 * Remove a ship piece once hit.
	 * 
	 * @param x X value to remove
	 * @param y Y value to remove
	 */
	public void removeShipPiece(int x, int y) {
		board[x][y] = 0;
	}
	
	/**
	 * Getter for PlayerColor.
	 * 
	 * @return PlayerColor this board belongs to
	 */
	public PlayerColor getPlayerColor() {
		return pcolor;
	}
	
	public int getStatus(int x, int y) {
		return board[x][y];
	}
	
	/**
	 * Clears the board state.
	 */
	public void clearBoard() {
		// Set each space on the board to false
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				board[i][j] = 0;
			}
		}
	}
	
	/**
	 * Getter for amount of hits the owner has taken.
	 * 
	 * @return int Amount of hits taken
	 */
	public int getHits() {
		return hits;
	}
}
