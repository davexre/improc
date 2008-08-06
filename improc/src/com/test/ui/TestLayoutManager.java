package com.test.ui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.OverlayLayout;
import javax.swing.ScrollPaneConstants;
import javax.swing.ScrollPaneLayout;
import javax.swing.ViewportLayout;

public class TestLayoutManager {

  protected static JFrame f;
  
  public static void testBoxLayout() {
    JPanel pnl = new JPanel();
    BoxLayout lm = new BoxLayout(pnl, BoxLayout.X_AXIS);
    pnl.setLayout(lm);

    JPanel pnl1 = new JPanel();
    BoxLayout lm1 = new BoxLayout(pnl1, BoxLayout.Y_AXIS);
    pnl1.setLayout(lm1);

    JPanel pnl2 = new JPanel();
    BoxLayout lm2 = new BoxLayout(pnl2, BoxLayout.Y_AXIS);
    pnl2.setLayout(lm2);
    
    pnl.add(pnl1);
    pnl.add(pnl2);

    for (int i = 0; i < 5; i++) {
      JTextPane jp = new JTextPane();
      JScrollPane c = new JScrollPane(jp, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      c.setSize(100,150);
      //c.setPreferredSize(new Dimension(100, 150));
      pnl1.add(c);

      JButton cc = new JButton("Button " + i);
      pnl2.add(cc);
      //cc.setPreferredSize(new Dimension(20, 0));
    }      
    
    f.add(pnl);
  }
  
  public static void testBorderLayout() {
    BorderLayout lm = new BorderLayout();
    lm.setHgap(10);
    f.setLayout(lm);
    f.add(new Button("BorderLayout.NORTH"), BorderLayout.NORTH);
    f.add(new Button("BorderLayout.SOUTH"), BorderLayout.SOUTH);
    f.add(new Button("BorderLayout.WEST"), BorderLayout.WEST);
    f.add(new Button("BorderLayout.EAST"), BorderLayout.EAST);
    f.add(new Button("BorderLayout.CENTER"), BorderLayout.CENTER);
  }
  
  static CardLayout cardLayout;
  static JPanel pnlBottom ;
  public static void testCardLayout() {
    JPanel pnl = new JPanel();
    pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
    
//    f.addComponentListener(new ComponentAdapter() {
//      public void componentMoved(ComponentEvent e) {
//        super.componentMoved(e);
//      }
//      
//      public void componentResized(ComponentEvent e) { 
//        super.componentResized(e);
//        f.pack();
//      }
//      
//    });
    
    JPanel pnlTop = new JPanel();
    pnlTop.setLayout(new FlowLayout());
    JButton btn;
    btn = new JButton("|<");
    btn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cardLayout.first(pnlBottom);
      }
    });
    pnlTop.add(btn);
    
    btn = new JButton("<");
    btn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cardLayout.previous(pnlBottom);
      }
    });
    pnlTop.add(btn);
    
    btn = new JButton(">");
    btn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cardLayout.next(pnlBottom);
      }
    });
    pnlTop.add(btn);
    
    btn = new JButton(">|");
    btn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cardLayout.last(pnlBottom);
      }
    });
    pnlTop.add(btn);
    
    pnlBottom = new JPanel();
    cardLayout = new CardLayout();
    pnlBottom.setLayout(cardLayout);
    for (int i = 0; i < 5; i++)
      pnlBottom.add(new JButton("Button " + i), "Button " + i);
    pnl.add(pnlTop);
    pnl.add(pnlBottom);
    f.add(pnl);
  }
  
  public static void testFlowLayout() {
    FlowLayout lm = new FlowLayout(FlowLayout.RIGHT);
    f.setLayout(lm);
    
    for (int i = 0; i < 5; i++) {
      JTextPane jp = new JTextPane();
      JScrollPane c = new JScrollPane(jp, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      c.setSize(100,150);
      c.setPreferredSize(new Dimension(100, 150));
      f.add(c);
    }      
  }
  
  public static void testScrollPane() {
    //ScrollPanel = new ScrollPane();
    
  } 
  
  public static void testScrollPaneLayout()  {
    ScrollPaneLayout lm = new ScrollPaneLayout();
    JPanel pnl = new JPanel();
    pnl.setLayout(lm);
    f.add(pnl);
    lm.layoutContainer(pnl);
    
    for (int i = 0; i < 5; i++) {
      JTextPane jp = new JTextPane();
      JScrollPane c = new JScrollPane(jp, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      c.setSize(100,150);
      c.setPreferredSize(new Dimension(100, 150));
      pnl.add(c);
    }      
  }
  
  public static void testOverlayLayout() {
    OverlayLayout lm = new OverlayLayout(f);
    f.setLayout(lm);

    for (int i = 0; i < 5; i++) {
      JTextPane jp = new JTextPane();
      JScrollPane c = new JScrollPane(jp, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      c.setSize(100,150);
      c.setPreferredSize(new Dimension(100, 150));
      c.setLocation(i * 30, i * 30);
      f.add(c);
    }      
  }

  public static void testViewportLayout() {
    JPanel pnl = new JPanel();
    ViewportLayout lm = new ViewportLayout();
    //lm.layoutContainer(pnl);
    pnl.setLayout(lm);
    f.add(pnl);

    for (int i = 0; i < 5; i++) {
      JTextPane jp = new JTextPane();
      JScrollPane c = new JScrollPane(jp, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      c.setSize(100,150);
      c.setPreferredSize(new Dimension(100, 150));
      pnl.add(c);
    }      
  }
  
  public static void testNone() {
    for (int i = 0; i < 5; i++) {
      JTextPane jp = new JTextPane();
      JScrollPane c = new JScrollPane(jp, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      c.setSize(100,150);
      c.setPreferredSize(new Dimension(100, 150));
      f.add(c);
    }      
  }
  
  public static void main(String[] args) {
    f = new JFrame("Layout manager test");
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    //testBoxLayout();
    //testBorderLayout();
    //testCardLayout(); 
    //testFlowLayout();
    //testScrollPane();
    //testScrollPaneLayout(); // ???? DOES NOT WORK ???
    //testOverlayLayout();
    //testViewportLayout();
    testNone();
    
    f.setSize(300, 300);
    f.setVisible(true);
  }
}
