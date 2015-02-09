package org.basex.examples.server;

import static org.basex.core.users.UserText.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * This class tests the event mechanism with a gui.
 *
 * @author BaseX Team 2005-15, BSD License
 */
public final class ServerEventsGUI extends JFrame {
  /** Name of test database and user. */
  static final String NAME = Util.className(ServerEventsGUI.class);
  /** Number of clients. */
  static final int CLIENTS = 2;
  /** Color. */
  static final String RED = "RED";
  /** Color. */
  static final String BLUE = "BLUE";
  /** Color. */
  static final String YELLOW = "YELLOW";

  /** Database server. */
  private static BaseXServer server;
  /** Number of open windows. */
  private static int open = CLIENTS;

  /** Client session. */
  ClientSession session;
  /** Main panel. */
  JPanel main;

  /**
   * Main method, launching the test gui.
   * @param args ignored
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    server = new BaseXServer("-z");

    // initialization
    try(ClientSession cs = new ClientSession(server.context, ADMIN, ADMIN)) {
      cs.execute("create event " + NAME);
      cs.execute("create db " + NAME + " <Application><Background/></Application>");
    }

    for(int i = 0; i < CLIENTS; i++) new ServerEventsGUI(i);
  }

  /**
   * Default Constructor.
   * @param count window counter
   * @throws IOException I/O exception
   */
  private ServerEventsGUI(final int count) throws IOException {
    super("Window " + (count + 1));

    JPanel buttons = new JPanel();
    buttons.setLayout(new FlowLayout());
    buttons.setOpaque(false);

    final JTextArea area = new JTextArea();
    area.setPreferredSize(new Dimension(280, 90));
    area.setEditable(false);
    area.setBorder(new CompoundBorder(new EtchedBorder(),
        new EmptyBorder(10, 10, 10, 10)));

    // create notifier instance
    final EventNotifier en = new EventNotifier() {
      @Override
      public void notify(final String value) {
        // use event feedback to repaint background
        Color c = Color.WHITE;
        String tmp = value.replaceAll("\"", "");
        if(tmp.equals(RED)) {
          c = Color.RED;
        } else if(tmp.equals(BLUE)) {
          c = Color.BLUE;
        } else if(tmp.equals(YELLOW)) {
          c = Color.YELLOW;
        }
        main.setBackground(c);

        // display updated XML fragment
        try(final ClientQuery cq = session.query("/")) {
          area.setText(cq.execute());
        } catch(IOException ex) {
          ex.printStackTrace();
        }
      }
    };

    // create session, open database and register event watcher
    session = new ClientSession(server.context, ADMIN, ADMIN);
    session.execute("open " + NAME);
    session.watch(NAME, en);

    // create notification buttons
    for(final String color : new String[] { RED, BLUE, YELLOW }) {
      final JButton b = new JButton(color);
      // create action event
      b.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
          try {
            // send update query and event
            String query =
              "let $color := '" + color + "' return " +
              "(replace value of node /Application/Background with $color," +
              " db:event('" + NAME + "', $color))";
            try(ClientQuery cq = session.query(query)) {
              cq.execute();
            }
          } catch(IOException ex) {
            ex.printStackTrace();
          }
          en.notify(b.getText());
        }
      });
      buttons.add(b);
    }

    main = new JPanel();
    main.setLayout(new BorderLayout());
    main.setBackground(Color.WHITE);
    main.add(area, BorderLayout.CENTER);
    main.add(buttons, BorderLayout.SOUTH);

    add(main);
    pack();
    setLocation(300 * count, 0);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setVisible(true);
  }

  @Override
  public void dispose() {
    try {
      session.close();
      if(--open == 0) {
        // no sessions left: drop event and database and stop server
        try(ClientSession css = new ClientSession(server.context, ADMIN, ADMIN)) {
          css.execute("drop event " + NAME);
          css.execute("drop db " + NAME);
        }
        server.stop();
      }
      super.dispose();
    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }
}
