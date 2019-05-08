import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.awt.*;
import javax.swing.*;

/**
 * Acts as the client for a battleship game.
 * 
 * @author Eli Hopkins, Madison Russel, Esteban Cruz, and Krish Mahtani
 * @version 1.0
 */
public class BattleShipGui extends JPanel
{
	// Declare Variables
	private static final long serialVersionUID = 8335086950289855933L;
	private Object lock;
	private static BattleShipGui ob;
	private final int SIZE = 9;
	private String nick;
	private JPanel yourSide;
	private JPanel oppSide;
	private JPanel chatPanel;
	private JPanel outcomePan;
	private GridLayout yourSideLayout;
	private GridLayout oppSideLayout;
	private BoxLayout chatLayout;
	private JTextField jtfChatBox;
	private JPanel jpChatBox;
	private JTextArea jtArea;
	private JTextArea jtChat;
	private JButton jbSend;
	private BattleButton[][] left, right;//left is yourSide and right is opponents
	private Socket chatSocket;
	private Socket gameSocket;
	private PrintWriter chatPWrite;
	private boolean gameStarted;
	private PlayerColor ourColor;
	private Board ourBoard;
	private Board oppBoard;
	private int fAdded;
	private boolean turn;
	private int ships;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	
	/**
	 * Initialize all variables
	 * 
	 * @param nickname The nickname to be used by the player
	 * @param IP The IP address of the server
	 */
	public BattleShipGui(String nickname, InetAddress IP)
	{
	  // Adjust layout and background of the frame
      setLayout(new FlowLayout(FlowLayout.CENTER));
      setBackground(new Color(0, 0, 255));
      
      // Declare miscelaneous variables
      ships = 0;
      ourBoard = null;
      oppBoard = null;
      nick = nickname;
      gameStarted = false;
      turn = false;
      fAdded = 0;
      chatSocket = null;
      gameSocket = null;
      chatPWrite = null;
      lock = new Object();
      
      // Initialize sockets and the print writer
      try {
		chatSocket = new Socket(IP, 16789);
		gameSocket = new Socket(IP, 16790);
		chatPWrite = new PrintWriter(chatSocket.getOutputStream());
      } catch (IOException e) {
		e.printStackTrace();
      }
      
      // Start the chat server
      new ChatThread(chatSocket).start();
      
      // Initialize GUI objects and adjust their properties
      yourSideLayout = new GridLayout(SIZE, SIZE);
      oppSideLayout = new GridLayout(SIZE, SIZE);
      yourSideLayout.setHgap(5);
      yourSideLayout.setVgap(5);
      oppSideLayout.setHgap(5);
      oppSideLayout.setVgap(5);
      yourSide = new JPanel();
      yourSide.setLayout(yourSideLayout);
      yourSide.setBackground(new Color(0, 0, 255));
      oppSide = new JPanel();
      oppSide.setLayout(oppSideLayout);
      oppSide.setBackground(new Color(0, 0, 255));
      left = new BattleButton[SIZE][SIZE];
      right = new BattleButton[SIZE][SIZE];
      chatPanel = new JPanel();
      chatLayout = new BoxLayout(chatPanel, BoxLayout.Y_AXIS);
      chatPanel.setLayout(chatLayout);
      chatPanel.setBackground(new Color(0, 0, 255));
      jpChatBox = new JPanel();
      jpChatBox.setBackground(new Color(0, 0, 255));
      
      // Create both grids
      for (int i = 0; i < SIZE; i++) {
      	for (int j = 0; j < SIZE; j++) {
      		left[i][j] = new BattleButton("" + i + " " + j, i, j, true);
            right[i][j] = new BattleButton("" + i + " " + j, i, j, false);
            left[i][j].setMargin(new Insets(0, 0, 0, 0));
            left[i][j].setPreferredSize(new Dimension(35, 35));
            right[i][j].setMargin(new Insets(0, 0, 0, 0));
            right[i][j].setPreferredSize(new Dimension(35, 35));
            left[i][j].addMouseListener(new BoardAdapter());
            right[i][j].addMouseListener(new BoardAdapter());
            left[i][j].setOpaque(true);
            right[i][j].setOpaque(true);
            yourSide.add(left[i][j]);
            oppSide.add(right[i][j]);
      	}
      }
      
      // Adjust more GUI properties
      jtArea = new JTextArea(5,25);
      jtArea.setForeground(Color.WHITE);
      jtArea.setBackground(new Color(0, 0, 0));
      jtArea.setFont(new Font("TimesRoman", Font.PLAIN, 20));
      jtArea.setLineWrap(true);
      jtArea.setWrapStyleWord(true);
      jtArea.setEditable(false);
      jtArea.setVisible(true);
      outcomePan = new JPanel();
      outcomePan.setLayout(new FlowLayout(FlowLayout.CENTER));
      outcomePan.setBackground(new Color(0, 0, 255));
      outcomePan.add(new JScrollPane(jtArea));
      jtChat = new JTextArea(20, 20);
      jtChat.setLineWrap(true);
      jtChat.setWrapStyleWord(true);
      jtChat.setEditable(false);
      jtChat.setVisible(true);
      jtfChatBox = new JTextField(14);
      jbSend = new JButton("Send");
      
      // Add an action listener for the chat
      jbSend.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			chatPWrite.println(nick + ": " + jtfChatBox.getText());
			chatPWrite.flush();
			jtfChatBox.setText("");
		}
      });
      
      
      chatPanel.add(new JScrollPane(jtChat));
      jpChatBox.add(jtfChatBox);
      jpChatBox.add(jbSend);
      chatPanel.add(jpChatBox);
      
      JPanel gameTest = new JPanel();
      gameTest.setLayout(new BorderLayout());
      ((BorderLayout) gameTest.getLayout()).setHgap(5);
      gameTest.add(yourSide, BorderLayout.WEST);
      gameTest.add(oppSide, BorderLayout.EAST);
      gameTest.add(outcomePan, BorderLayout.SOUTH);
      gameTest.add(chatPanel, BorderLayout.CENTER);
      add(gameTest);
      
      // Start the game
      new GameThread(gameSocket).start();
   }
   
   /**
    * Updates the board after the opponent takes their turn.
    * 
    * @param newBoard The board received from the opponent
    */
   public void updateBoard(Board newBoard) {
	   // Used to ensure that a hit isn't shown as a miss
	   boolean hitOnTurn = false;
	   
	   // Loop for each space and compare with the new board
	   for (int i = 0; i < left.length; i++) {
		   for (int j = 0; j < left[i].length; j++) {
			   // Collect status of the old and new space
			   int newStatus = newBoard.getStatus(i, j);
			   int oldStatus = ourBoard.getStatus(i, j);
			   
			   // If different...
			   if (newStatus != oldStatus) {
				   switch (newStatus) {
				      // A hit was scored
				      case 2:
				    	  hitOnTurn = true;
				    	  left[i][j].setBackground(Color.RED);
				    	  jtArea.setText("Friendlies Hit!");
				    	  try {
				    		  Thread.sleep(1000);
				    	  } catch (InterruptedException e) {
				    		  e.printStackTrace();
				    	  }
				    	  break;
				      // There was only a miss
				      case 3:
				    	  if (!hitOnTurn) {
				    		  jtArea.setText("Friendlies Missed!");
				    	  }
				    	  left[i][j].setBackground(Color.BLACK);
			    		  left[i][j].setForeground(Color.WHITE);
			    		  try {
			    			  Thread.sleep(1000);
			    		  } catch (InterruptedException e) {
			    			  e.printStackTrace();
			    		  }
				    	  break;
				   }
			   }
		   }
	   }
	   // Update board with adjusted colors
	   ourBoard = newBoard;
   }
   
   /**
    * Starts the program.
    * 
    * @param args Command-line args, expects nick and IP as args
    */
   public static void main(String[] args)
   {
	  // Create a new JFrame
      JFrame frame = new JFrame();
      
      // Create an instance of BattleShipGui and add it to the JFrame
      try {
		ob = new BattleShipGui(args[0], InetAddress.getByName(args[1]));
      } catch (UnknownHostException e) {
		e.printStackTrace();
      }
      frame.setTitle("Group 1: Battleship");
      frame.add(ob);
      frame.setVisible(true);
      frame.pack();
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   }
   
   /**
    * This thread handles the game.
    * 
    * @author Eli Hopkins
    * @version 1.0
    */
   protected class GameThread extends Thread {
	   Socket gameSock;
	   
	   /**
	    * Initialize variables.
	    * 
	    * @param gameSock The socket to run the game on
	    */
	   public GameThread(Socket gameSock) {
		   this.gameSock = gameSock;
		   oos = null;
		   ois = null;
		   try {
			   oos = new ObjectOutputStream(gameSock.getOutputStream());
			   ois = new ObjectInputStream(gameSock.getInputStream());
		   } catch (IOException e) {
			   e.printStackTrace();
		   }
	   }
	   
	   /**
	    * Manages the game.
	    */
	   public void run() {
		   try {
			   // Get this player's color, and initialize a board for each player
			   ourColor = (PlayerColor) ois.readObject();
			   
			   ourBoard = new Board(ourColor);
			   
			   oppBoard = new Board(PlayerColor.RED);
			   
			   jtArea.setText("Place Ships");
			   
			   // Game loop til 5 hits
			   while(ourBoard.getHits() < 5 && oppBoard.getHits() < 5) {
				   // Get opponents board and update then start turn
				   oppBoard = (Board) ois.readObject();
				   ob.updateBoard((Board) ois.readObject());
				   gameStarted = true;
				   turn = true;
				   
				   jtArea.setText("Your Turn");
				   
				   // Wait for turn to complete via mouse handlers.
				   synchronized(lock) {
				   	lock.wait();
				   }
				   
				   // Finally write new board
				   oos.writeObject(oppBoard);
				   oos.flush();
				   turn = false;
			   }
			   
			   // Determine if the game is won or lost
			   if (oppBoard.getHits() == 5) {
				   jtArea.setText("You Win!");
			   } else {
				   jtArea.setText("You Lose...");
			   }
		   } catch (ClassNotFoundException | IOException | InterruptedException e) {
			   e.printStackTrace();
		   }
	   }
   }
   
   /**
    * Handles the chat.
    * 
    * @author Esteban Cruz
    *
    */
   protected class ChatThread extends Thread {
       String newmsg;
       Socket sc;
       
       /**
        * Initializes the socket.
        * 
        * @param sc Socket to use
        */
       public ChatThread(Socket sc){
          this.sc = sc;
       }
       
       /**
        * Repeatedly looks for text from chat.
        */
       public void run(){
          try{
             InputStream clientInput = sc.getInputStream();
             BufferedReader bin = new BufferedReader(new InputStreamReader(clientInput));
             
            //Create Reader for input
             while((newmsg = bin.readLine()) != null){
            	 //Read line from server and append to j text area
            	 jtChat.append("\n" + newmsg);
             }
             
             //Close stream
             //clientInput.close();
          } catch(IOException io) {
             System.out.println(io);
          }
       }
   }
   
   /**
    * Determines what should happen when a button is clicked.
    * 
    * @author Eli Hopkins
    *
    */
   protected class BoardAdapter extends MouseAdapter {
	   /**
	    * Runs on mouse click.
	    * 
	    * @param me MouseEvent occurring
	    */
	   public void mouseClicked(MouseEvent me) {
		   // Get x and y of origin
		   int x = ((BattleButton) me.getSource()).getX2();
		   int y = ((BattleButton) me.getSource()).getY2();
		   
		   // Check if this player owns the pressed button
		   if (((BattleButton) me.getSource()).getOwner()) {
			   // If they do then if the game hasn't started, empty spots can be filled with 5 ships
			   if (!gameStarted && ourBoard.getStatus(x, y) == 0 && ships < 5) {
				   ourBoard.addShipPiece(x, y);
				   left[x][y].setBackground(Color.CYAN);
				   System.out.println("Added friendly");
				   ships++;
				   // Once 5 ships are placed, wait
				   if (ships == 5) {
				   	try {
				   			jtArea.setText("Waiting for Opponent");
							oos.writeObject(ourBoard);
							oos.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
				   }
			   } else {
				   System.out.println("Invalid mouse click 1");
			   }
		   } else {
			   // Otherwise check if the game is active and show hit or miss
			   if (turn && oppBoard.getStatus(x, y) != 2) {
				   if (oppBoard.tryHit(x, y)) {
					   right[x][y].setBackground(Color.RED);
					   jtArea.setText("Hit");
					   System.out.println("Hit");
				   } else if (oppBoard.getHits() != 5) {
					   right[x][y].setBackground(Color.BLACK);
					   right[x][y].setForeground(Color.WHITE);
					   jtArea.setText("Miss");
					   System.out.println("No Hit");
					   
					   synchronized(lock) {
						   lock.notifyAll();
					   }
				   }
			   } else {
				   System.out.println("Invalid mouse click 2");
			   }
		   }
		   
		   if (oppBoard.getHits() == 5) {
			   synchronized(lock) {
				   lock.notifyAll();
			   }
		   }
		   
		   yourSide.repaint();
		   yourSide.revalidate();
	   }
   }
   
   /**
    * Adds functionality to JButton.
    * 
    * @author Eli Hopkins
    *
    */
   protected class BattleButton extends JButton {
	   private static final long serialVersionUID = 1826966623551372912L;
	   private int x2;
	   private int y2;
	   
	   // False for opp, True for this
	   private boolean owner;
	   
	   /**
	    * Initializes variables.
	    * 
	    * @param text The button text
	    * @param x The first number in the button text
	    * @param y The second number in the button text
	    * @param owner
	    */
	   public BattleButton(String text, int x, int y, boolean owner) {
		   super(text);
		   
		   this.x2 = x;
		   this.y2 = y;
		   this.owner = owner;
	   }
	   
	   /**
	    * Getter for the x of the button.
	    * 
	    * @return X value in array of button
	    */
	   public int getX2() {
		   return x2;
	   }
	   
	   /**
	    * Getter for the y of the button.
	    * 
	    * @return Y value in array of button
	    */
	   public int getY2() {
		   return y2;
	   }
	   
	   /**
	    * Getter for the button's owner
	    * 
	    * @return Whether this is a left side or right side button
	    */
	   public boolean getOwner() {
		   return owner;
	   }
   }
}