import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;

public class GuiTest extends JFrame {
   
   // Attributes
   private InetAddress serverIP;
   private String userName;
   private final int SIZE = 9;
   private final int PORT = 16789;
   private JPanel yourSide;
   private JPanel jpChatMain;
   private JPanel jp1;
   private JPanel oppSide;
   private JPanel outcomePan;
   private JTextArea jtArea;
   private JTextArea jtChatArea;
   private JTextField jtfName;
   private JTextField jtfChat;
   private JButton[][] left, right;//left is yourSide and right is opponents
   
   public GuiTest() {
      
      //this.serverIP = _serverIP;
                  
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
      
      yourSide = new JPanel(new GridLayout(SIZE, SIZE));
      yourSide.setBackground(new Color(0, 0, 255));
      left = new JButton[SIZE][SIZE];
      
      jpChatMain = new JPanel();
      
      JPanel jp0 = new JPanel();
      jp0.add(new JLabel("Player Chat", JLabel.CENTER));
      
      JPanel jp1 = new JPanel();
      jtChatArea = new JTextArea(25, 25);
      jtChatArea.setEditable(false);
      jtChatArea.setBackground(Color.BLACK);
      jtChatArea.setForeground(Color.GREEN);
      jp1.add(jtChatArea);
      
      jpChatMain.add(jp1); 
            
      oppSide = new JPanel(new GridLayout(SIZE, SIZE));
      oppSide.setBackground(new Color(0, 0, 255));
      right = new JButton[SIZE][SIZE];
            
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
      JButton jbSend = new JButton("Send");
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
      
      outcomePan = new JPanel();
      outcomePan.setBackground(new Color(0, 0, 255));
      jtArea = new JTextArea(5,25);
      jtArea.setForeground(Color.WHITE);
      jtArea.setBackground(Color.BLACK);
      jtArea.setLineWrap(true);
      jtArea.setWrapStyleWord(true);
      jtArea.setEditable(false);
      
      outcomePan.add(jtArea);
      
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
      
      add(jp0, BorderLayout.NORTH);            
      add(yourSide, BorderLayout.WEST);
      add(jpChatMain, BorderLayout.CENTER);
      add(oppSide, BorderLayout.EAST);
      add(jpSouthButtons, BorderLayout.SOUTH);
      
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
               String userName2 = jtfName.getText();
               jtfName.setEnabled(false);
               System.out.println(userName2);
            }
         });
      
      // Actionlistener to handle Send button click event      
      jbSend.addActionListener(
         new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               // msg = jtfChat.getText();
            //                   
            //                if (msg.equals("bye")) {
            //                   System.out.println("Client is leaving the chat...");
            //                   try {
            //                      Thread.sleep(1000);
            //                      clientPrintWriter.println(msg);
            //                      clientPrintWriter.flush();
            //                      jtfChat.setText("");
            //                   } catch (Exception e) {
            //                      System.out.println("Exception error: " + e.getMessage());   
            //                   }
            //                      
            //                   System.exit(0);
            //                } else {
            //                
            //                   clientPrintWriter.println(msg);
            //                   clientPrintWriter.flush();
            //                   jtfChat.setText("");
            //                   
            //                }
            }
         });
   } // End of constructor
   
   public static void main(String[]args) {
      new GuiTest();
   }
   
} // End of class GuiTest
