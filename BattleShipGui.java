import java.awt.event.*;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;

public class BattleShipGui extends JPanel implements MouseListener
{
   private final int SIZE = 9;
   private JPanel yourSide;
   private JPanel oppSide;
   private JPanel outcomePan;
   private JTextArea jtArea;
   private JButton[][] left, right;//left is yourSide and right is opponents
   
   public BattleShipGui()
   {
      setLayout(new FlowLayout(FlowLayout.CENTER));
      yourSide = new JPanel();
      yourSide.setLayout(new GridLayout(SIZE,SIZE));
      yourSide.setBackground(new Color(0, 0, 255));
      oppSide = new JPanel();
      oppSide.setLayout(new GridLayout(SIZE,SIZE));
      oppSide.setBackground(new Color(0, 0, 255));
      left = new JButton[SIZE][SIZE];
      right = new JButton[SIZE][SIZE];
      
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
         
      JPanel gameTest = new JPanel();
      gameTest.setLayout(new BorderLayout());
      gameTest.add(yourSide, BorderLayout.LINE_START);
      gameTest.add(oppSide, BorderLayout.LINE_END);
      gameTest.add(outcomePan, BorderLayout.PAGE_END);
      add(gameTest);
      
      
      //add code for the chat in the center (between yourSide and oppSide)
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
   
   public static void main(String[] args)
   {
      JFrame frame = new JFrame();
      BattleShipGui ob = new BattleShipGui();
      frame.add(ob);
      frame.setVisible(true);
      frame.pack();
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   }
}