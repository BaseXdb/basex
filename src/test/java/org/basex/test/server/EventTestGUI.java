package org.basex.test.server;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.basex.BaseXServer;
import org.basex.core.BaseXException;
import org.basex.server.ClientSession;
import org.basex.server.EventNotification;

/**
 * This class tests the event mechanism with a gui.
 * @author BaseX Team 2005-11, BSD License
 */
public final class EventTestGUI {

  /** Client session. */
  ClientSession cs;
  /** Main panel. */
  JPanel main;
  /** Main frame. */
  JFrame frame;

  /**
   * Main method, launching the test gui.
   * @param args ignored
   * @throws IOException I/O exception
   */
  public static void main(final String[] args) throws IOException {
    new BaseXServer();
    for(int i = 0; i < 4; i++) new EventTestGUI(i * 300);
  }

  /**
   * Default Constructor.
   * @param pos position on screen
   * @throws IOException I/O exception
   */
  private EventTestGUI(final int pos) throws IOException {
    frame = new JFrame();
    cs = new ClientSession("localhost", 1984, "admin", "admin");

    try {
      cs.watch("event", new EventNotification() {
          @Override
          public void update(final String data) {
            Color c = Color.WHITE;
            if(data.equals("RED")) {
              c = Color.RED;
            } else if(data.equals("BLUE")) {
              c = Color.BLUE;
            } else if(data.equals("YELLOW")) {
              c = Color.YELLOW;
            }
            main.setBackground(c);
            frame.repaint();
          }
        });
    } catch(BaseXException e1) {
      try {
        cs.execute("create event event");
        cs.watch("event", new EventNotification() {
          @Override
          public void update(final String data) {
            Color c = Color.WHITE;
            if(data.equals("RED")) {
              c = Color.RED;
            } else if(data.equals("BLUE")) {
              c = Color.BLUE;
            } else if(data.equals("YELLOW")) {
              c = Color.YELLOW;
            }
            main.setBackground(c);
            frame.repaint();
          }
        });
      } catch(BaseXException e2) {
        e2.printStackTrace();
      }
    }
    JButton red = new JButton("RED");
    JButton blue = new JButton("BLUE");
    JButton yellow = new JButton("YELLOW");

    red.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(final ActionEvent e) {
        action("RED");
      }
    });

    blue.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(final ActionEvent e) {
        action("BLUE");
      }
    });

    yellow.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(final ActionEvent e) {
        action("YELLOW");
      }
    });

    JPanel jp = new JPanel();
    jp.setLayout(new FlowLayout());
    jp.add(red);
    jp.add(blue);
    jp.add(yellow);
    main = new JPanel();
    main.setPreferredSize(new Dimension(300, 150));
    main.setBackground(Color.WHITE);
    main.add(jp);
    frame.getContentPane().add(main);
    frame.pack();
    frame.setLocation(pos + 20, 0);
    frame.setVisible(true);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
  }

  /**
   * Runs action.
   * @param c color
   */
  void action(final String c) {
    try {
      cs.event("event", "()", "'" + c + "'");
    } catch(BaseXException e) {
      e.printStackTrace();
    }
  }
}
