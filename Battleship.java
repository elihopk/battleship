import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;

/** 
 * Battleship GUI & Chat Client program.
 * @author Esteban Cruz, Krish Mahtani, Madison Rusell, Eli Hopkins
 * @version 20190501
 * This program is intended to work by providing a GUI 
 * for players to use in order to play Battleship. 
 * The GUI also provides a Chat area that establishes a connection 
 * to the ChatServer, messages are written and all 
 * client messages are shared with all clients.
 * 
*/

// Creates a class that extends JFrame 
public class Battleship extends JFrame {
   
   // Attributes
   private InetAddress serverIP;
   private String userName;
   private String msg;
   private String newmsg;
   private InputStream clientInput;
   private OutputStream clientOutput;
   private PrintWriter clientPrintWriter;
   private Socket socket;
   private final int PORT = 16789;
   
   private final int SIZE = 9;
   private JPanel yourSide;
   private JPanel jpChatMain;
   private JPanel oppSide;
   
   private JTextArea jtArea;
   private JTextArea jtChatArea;
   private JTextField jtfName;
   private JTextField jtfChat;
   private JButton[][] left, right;//left is yourSide and right is opponents
   
   /**
   *  Parameterized constructor.
   *  @param _serverIP - its an InetAddress representing the 
   *  server's IP.  
   */
   public Battleship(InetAddress _serverIP) {
      
      this.serverIP = _serverIP;
                  
      JMenuBar menuBar = new JMenuBar();
      setJMenuBar(menuBar);
      
      JMenu menu = new JMenu("File");
      menuBar.add(menu);
      
      JMenuItem jmiExit = new JMenuItem("Exit/Quit");
      menu.add(jmiExit);
      
      JMenu jmHelp = new JMenu("Help");
      menuBar.add(jmHelp);
      
      JMenuItem jmiHow = new JMenuItem("How to");
      jmHelp.add(jmiHow);
      
      JMenuItem jmiAbout = new JMenuItem("About");
      jmHelp.add(jmiAbout);
      
      // ****************************** Left board used for ship placement ******************************//
      
      yourSide = new JPanel(new GridLayout(SIZE, SIZE));
      yourSide.setBackground(new Color(0, 0, 255));
      left = new JButton[SIZE][SIZE];
      
      // ************************************************************************************************//
      
      // ********************** Center Chat used for communicating with other users *********************//
      
      jpChatMain = new JPanel(); 
      
      JPanel jp0 = new JPanel();
      jp0.add(new JLabel("Player Chat", JLabel.CENTER));
      
      JPanel jp1 = new JPanel();
      jtChatArea = new JTextArea(25, 25);
      jtChatArea.setEditable(false);
      jtChatArea.setLineWrap(true);
      jtChatArea.setWrapStyleWord(true);
      jtChatArea.setBackground(Color.BLACK);
      jtChatArea.setForeground(Color.WHITE);
      jp1.add(jtChatArea);
      
      jpChatMain.add(jp1); 
      
      // ************************************************************************************************//
      
      // ****************************** Right board used for target selection ***************************//
            
      oppSide = new JPanel(new GridLayout(SIZE, SIZE));
      oppSide.setBackground(new Color(0, 0, 255));
      right = new JButton[SIZE][SIZE];
      
      // ************************************************************************************************//
      
      // ****** South area used for player name, collect chat messages & hit or miss as well as winner communication ******//
            
      JPanel jpSouthButtons = new JPanel(new GridLayout(3,1));
                           
      JPanel jp2 = new JPanel();
      jp2.setBackground(new Color(0, 0, 255));
      jp2.add( new JLabel( "Name:", JLabel.RIGHT ) );
      jtfName = new JTextField(15);
      JButton jbEnter = new JButton("Enter");
      jp2.add(jtfName);
      jp2.add(jbEnter);
         
      JPanel jp3 = new JPanel();
      jp3.setBackground(new Color(0, 0, 255));
      jp3.add( new JLabel( "Message:", JLabel.RIGHT ) );
      jtfChat = new JTextField(20);
      jtfChat.setEnabled(false);
      JButton jbSend = new JButton("Send");
      jbSend.setEnabled(false);
      jp3.add(jtfChat);
      jp3.add(jbSend);
      
      JPanel jp4 = new JPanel();
      jp4.setBackground(new Color(0, 0, 255));
      jtArea = new JTextArea(5,25);
      jtArea.setForeground(Color.WHITE);
      jtArea.setBackground(Color.BLACK);
      jtArea.setLineWrap(true);
      jtArea.setWrapStyleWord(true);
      jtArea.setEditable(false);
      jp4.add(jtArea);
         
      jpSouthButtons.add(jp2);
      jpSouthButtons.add(jp3);
      jpSouthButtons.add(jp4);
      
      // ************************************************************************************************//
      
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
            //left[i][j].addActionListener(this);
            //right[i][j].addActionListener(this);
            yourSide.add(left[i][j]);
            oppSide.add(right[i][j]);
         }
      }
      
      // Place the panels in the desired layout location
      add(jp0, BorderLayout.NORTH);            
      add(yourSide, BorderLayout.WEST);
      add(jpChatMain, BorderLayout.CENTER);
      add(oppSide, BorderLayout.EAST);
      add(jpSouthButtons, BorderLayout.SOUTH);
      
      // Add scrollpane to chat area
      JScrollPane sp = new JScrollPane(jtChatArea);
      add(sp);
      
      pack();
      setTitle("Team#One Battleship");
      setVisible(true);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      // Actionlistener to handle Exit menu click event      
      jmiExit.addActionListener(
         new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               System.exit(0);
            }
         });
      
      // Actionlistener to handle How to menu click event      
      jmiHow.addActionListener(
         new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               JOptionPane.showMessageDialog(null, "How to play: \n1) Place your ships on the left side.\n2) Wait for the server to acknowledge your opponent is ready.\n3) Select a button from the right side to fire at your opponent.\n4) Wait for the server to acknowledge whether is a hit or miss.");
            }
         });
               
      // Actionlistener to handle About menu click event      
      jmiAbout.addActionListener(
         new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               JOptionPane.showMessageDialog(null, "Battleship Chat made by Esteban");
            }
         });
   
      // Actionlistener to handle Enter button click event
      jbEnter.addActionListener(
         new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               userName = jtfName.getText();
               jtfName.setEnabled(false);
               setUserName(userName);
               jtfChat.setEnabled(true);
               jbSend.setEnabled(true);
               System.out.println("Print statement in enter button action listener: " + getUserName());
            }
         });
      
      // Actionlistener to handle textfield keyboard event      
      jtfChat.addActionListener(
         new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               
               sendMessage();
                
            } 
         });

      // Actionlistener to handle Send button click event      
      jbSend.addActionListener(
         new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               
               sendMessage();
                  
            } 
         });
   } // End of constructor
   
   /**
   *  Method to establish chat connection.
   *  
   */
   public void connect() {
      try {
         
         System.out.println("Server IP: " + serverIP);
         System.out.println("Connecting to port: " + PORT);
         
         socket = new Socket(serverIP, PORT);
      
         System.out.println("Connected to the chat server");
      
         ClientThread ct = new ClientThread(socket);
         ct.start();
      
      } catch (Exception e) {
         
         System.out.println("Exception error: " + e.getMessage());
      
      }    
   } // End of Connect Method

   /**
   *  Method to handle chat send messages.
   *  
   */
   public void sendMessage() {
      
      msg = "[" + getUserName() + "]: " + jtfChat.getText();
                              
      try {
                  
                  // Instantiates outputstream in order to write to the server
         clientOutput = socket.getOutputStream();
         clientPrintWriter = new PrintWriter(new OutputStreamWriter(clientOutput));
                  
                  // If the client types bye the communication will end as well as the game
                  // Maybe add another validation in order to confirm the entry [yes] or [no]
                  // If ys then end if no then continue
         if (msg.contains("bye") || msg.contains("Bye")) {
            System.out.println("Client is leaving the chat...");
            try {
                        
               Thread.sleep(1000);
               clientPrintWriter.println(msg);
               clientPrintWriter.flush();
               jtfChat.setText("");
               System.exit(0);
                     
            } catch (Exception e) {
                        
               System.out.println("Exception error: " + e.getMessage());   
                     
            }
         } else {
                     
            clientPrintWriter.println(msg);
            clientPrintWriter.flush();
                     
            jtfChat.setText("");
         }
      } catch (Exception e) {
                  
         System.out.println("Exception error: " + e.getMessage());
               
      }
   } // End of sendMessage
   
   /**
   *  Mutator method.
   *  @param userName - the chat client username
   */
   public void setUserName(String userName) {
      this.userName = userName;
   } 
   
   /**
   *  Accessor method.
   *  @return string
   */
   public String getUserName() {
      return this.userName;
   }

   /**
   *  Main method.
   *  @param args - string array
   */
   public static void main(String[]args) {
      
      if (args.length == 0) {
         System.out.println("Syntax: java BattleShip2 <IP Address>");   
      } else {
         try {
         
            InetAddress serverIP = InetAddress.getByName(args[0]);
                  
            Battleship battleClient = new Battleship(serverIP);
            battleClient.connect();
                     
         } catch (Exception e) {
            System.out.println("Exception error: " + e.getMessage());
         }  
      }
   
   }
   
   /**
   *  Inner class that extends thread.
   *  This class handles multi-threaded messages/connections
   *  as well as updating the textarea with most
   *  recent messages. 
   */
   public class ClientThread extends Thread {
      
      // Attributes   
      private Socket sc;
      
      /**
      *  Class constructor.
      *  @param sc - socket
      */  
      public ClientThread(Socket sc) {
         this.sc = sc;
      }
         
      /**
      *  Run method that handles the
      *  messages per threads, by getting the messages,
      *  reading them and append them to jtextarea .
      *
      */
      public void run() {
         try {
               
            clientInput = sc.getInputStream();
            BufferedReader bin = new BufferedReader(new InputStreamReader(clientInput));
               
            // Create Reader for input
            while ((newmsg = bin.readLine()) != null) {
               
               // Read line from server and append to j text area
               // bin.readLine();
               jtChatArea.append("\n" + newmsg);
            }
               
            if (msg == null) {
               // ackowledges that the server is no longer there
               // Basically lost connection
               // sc.isClosed();
               JOptionPane.showMessageDialog(null, "No more communications are allowed!");
               try {
                  
                  Thread.sleep(2000);
                  System.exit(0);
               
               } catch (Exception e) {
                  
                  System.out.println("Exception error: " + e.getMessage());

               }
            }
            
            if (!(sc.isConnected())) {
               JOptionPane.showMessageDialog(null, "No more communications are allowed!");
               try {
                  Thread.sleep(2000);
                  sc.close();
                  System.exit(0);
               } catch (Exception e) {
                  
                  System.out.println("Exception error: " + e.getMessage());
                     
               }
            
            }
         
         // Close stream
         // clientInput.close();
         } catch (Exception io) {
            
            System.out.println("Exception error: " + io.getMessage());
         
         }
      }
         
   } // end of inner class
   
} // End of class Battleship

