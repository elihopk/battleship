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

public class BattleShipGui extends JPanel
{
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
   private PrintWriter chatPWrite;
   private boolean gameStarted;
   private PlayerColor ourColor;
   private Board ourBoard;
   private Board oppBoard;
   private int fAdded;
   private boolean turn;
   
   public BattleShipGui(String nickname, InetAddress IP)
   {
      setLayout(new FlowLayout(FlowLayout.CENTER));
      setBackground(new Color(0, 0, 255));
      
      ourBoard = null;
      oppBoard = null;
      nick = nickname;
      gameStarted = false;
      turn = false;
      fAdded = 0;
      chatSocket = null;
      chatPWrite = null;
      
      try {
		chatSocket = new Socket(IP, 16789);
		chatPWrite = new PrintWriter(chatSocket.getOutputStream());
      } catch (IOException e) {
		e.printStackTrace();
      }
      
      new ChatThread(chatSocket).start();
      
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
      
      for (char row = 'A'; row <= 'I'; row++)
      {
         for (int col = 1, i = 0, j = 0; col <= 9 && i < SIZE && j < SIZE; col++, i++, j++)
         {
            left[i][j] = new BattleButton("" + row + col, i, j, true);
            right[i][j] = new BattleButton(""+row+col, i, j, false);
            left[i][j].setMargin(new Insets(0, 0, 0, 0));
            left[i][j].setPreferredSize(new Dimension(35, 35));
            right[i][j].setMargin(new Insets(0, 0, 0, 0));
            right[i][j].setPreferredSize(new Dimension(35, 35));
            left[i][j].addMouseListener(new BoardAdapter());
            right[i][j].addMouseListener(new BoardAdapter());
            yourSide.add(left[i][j]);
            oppSide.add(right[i][j]);
         }
      }
      
      jtArea = new JTextArea(5,25);
      jtArea.setForeground(Color.WHITE);
      jtArea.setBackground(new Color(0, 0, 0));
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
      
      
   }
   
   public void updateBoard(Board newBoard) {
	   boolean hitOnTurn = false;
	   
	   for (int i = 0; i < left.length; i++) {
		   for (int j = 0; j < left[i].length; j++) {
			   int newStatus = newBoard.getStatus(i, j);
			   int oldStatus = ourBoard.getStatus(i, j);
			   
			   if (newStatus != oldStatus) {
				   switch (newStatus) {
				      case 2:
				    	  hitOnTurn = true;
				    	  left[i][j].setBackground(Color.RED);
				    	  jtArea.setText("Friendlies Hit!");
				    	  break;
				      case 3:
				    	  if (!hitOnTurn) {
				    		  left[i][j].setBackground(Color.BLACK);
				    		  left[i][j].setForeground(Color.WHITE);
				    		  jtArea.setText("Friendlies Missed!");
				    	  }
				    	  break;
				   }
			   }
		   }
	   }
	   
	   ourBoard = newBoard;
   }
   
   /**
    * Starts the program.
    * 
    * @param args Command-line args, expects nick and IP as args
    */
   public static void main(String[] args)
   {
      JFrame frame = new JFrame();
      ob = null;
      
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
   
   protected class GameThread extends Thread {
	   Socket gameSock;
	   ObjectOutputStream oos;
	   ObjectInputStream ois;
	   
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
	   
	   public void run() {
		   try {
			   ourColor = (PlayerColor) ois.readObject();
			   
			   ourBoard = new Board(ourColor);
			   
			   while(ourBoard.getHits() < 17 && oppBoard.getHits() < 17) {
				   oppBoard = (Board) ois.readObject();
				   ob.updateBoard((Board) ois.readObject());
				   turn = true;
				   
				   wait();
				   
				   oos.writeObject(oppBoard);
				   turn = false;
			   }
		   } catch (ClassNotFoundException | IOException | InterruptedException e) {
			   e.printStackTrace();
		   }
	   }
   }
   
   protected class ChatThread extends Thread {
       String newmsg;
       Socket sc;
       
       public ChatThread(Socket sc){
          this.sc = sc;
       }
       
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
   
   protected class BoardAdapter extends MouseAdapter {
	   public void mouseClicked(MouseEvent me) {
		   int x = ((BattleButton) me.getSource()).getX();
		   int y = ((BattleButton) me.getSource()).getY();
		   
		   if (((BattleButton) me.getSource()).getOwner()) {
			   if (!gameStarted && ourBoard.getStatus(x, y) == 0) {
				   ourBoard.addShipPiece(x, y);
				   left[x][y].setBackground(Color.CYAN);
				   System.out.println("Added friendly");
			   }
			   System.out.println("Invalid mouse click");
		   } else {
			   if (turn && oppBoard.getStatus(x, y) != 2) {
				   if (oppBoard.tryHit(x, y)) {
					   right[x][y].setBackground(Color.RED);
					   System.out.println("Hit");
				   } else {
					   right[x][y].setBackground(Color.BLACK);
					   right[x][y].setForeground(Color.WHITE);
					   System.out.println("No Hit");
					   
					   notifyAll();
				   }
			   }
			   System.out.println("Invalid mouse click");
		   }
	   }
   }
   
   protected class BattleButton extends JButton {
	   private static final long serialVersionUID = 1826966623551372912L;
	   private int x;
	   private int y;
	   
	   // False for opp, True for this
	   private boolean owner;
	   
	   public BattleButton(String text, int x, int y, boolean owner) {
		   super(text);
		   
		   this.x = x;
		   this.y = y;
	   }
	   
	   public int getX() {
		   return x;
	   }
	   
	   public int getY() {
		   return y;
	   }
	   
	   public boolean getOwner() {
		   return owner;
	   }
   }
}