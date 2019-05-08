import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.awt.*;
import javax.swing.*;

public class BattleShipGui extends JPanel implements MouseListener
{
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
   private JButton[][] left, right;//left is yourSide and right is opponents
   private Socket chatSocket;
   private PrintWriter chatPWrite;
   
   public BattleShipGui(String nickname, InetAddress IP)
   {
      setLayout(new FlowLayout(FlowLayout.CENTER));
      setBackground(new Color(0, 0, 255));
      
      nick = nickname;
      
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
      left = new JButton[SIZE][SIZE];
      right = new JButton[SIZE][SIZE];
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
            left[i][j] = new JButton("" + row + col);
            right[i][j] = new JButton(""+row+col);
            left[i][j].setMargin(new Insets(0, 0, 0, 0));
            left[i][j].setPreferredSize(new Dimension(35, 35));
            right[i][j].setMargin(new Insets(0, 0, 0, 0));
            right[i][j].setPreferredSize(new Dimension(35, 35));
            //uncomment once added actionListeners
            left[i][j].addMouseListener(this);
            right[i][j].addMouseListener(this);
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
   
   //Required methods for implementation of MouseListener
      
   public void mousePressed(MouseEvent me) {}
   public void mouseExited(MouseEvent me) {}
   public void mouseReleased(MouseEvent me) {}
   public void mouseEntered(MouseEvent me) {}
   public void mouseClicked(MouseEvent me) {
	   if (me.getButton() == MouseEvent.BUTTON1) {
		   System.out.println("This will eventually move the ship horizontally");     
	   }
      
	   if (me.getButton() == MouseEvent.BUTTON3) {
		   System.out.println("This will eventually move the ship vertically");
	   }
   }
   
   /**
    * Starts the program.
    * 
    * @param args Command-line args, expects nick and IP as args
    */
   public static void main(String[] args)
   {
      JFrame frame = new JFrame();
      BattleShipGui ob = null;
      
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
   
   public class ChatThread extends Thread {
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
}