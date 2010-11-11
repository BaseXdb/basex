package org.basex.gui.server;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;

import org.basex.core.Context;
import org.basex.core.Text;
import org.basex.gui.AGUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXPassword;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.server.ClientSession;

/**
 * This class is the main window of the BaseXServerGUI.
 * It is the central instance for user interactions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class SGUI extends AGUI {
  
  /** Clientsession. */
  ClientSession client;
  /** Name of the server. */
  String servertitle;

  /**
   * Default constructor.
   * @param ctx context reference
   * @param gprops gui properties
   */
  public SGUI(final Context ctx, final GUIProp gprops) {
    super(ctx, gprops, Text.TITLE);
    
    // set window size
    final Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
    final int[] ps = prop.nums(GUIProp.GUILOC);
    final int[] sz = prop.nums(GUIProp.GUISIZE);
    final int x = Math.max(0, Math.min(scr.width - sz[0], ps[0]));
    final int y = Math.max(0, Math.min(scr.height - sz[1], ps[1]));
    setBounds(x, y, sz[0], sz[1]);
    if(prop.is(GUIProp.MAXSTATE)) {
      setExtendedState(MAXIMIZED_HORIZ);
      setExtendedState(MAXIMIZED_VERT);
      setExtendedState(MAXIMIZED_BOTH);
    }
    setVisible(true);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    showDialog();
  }
  
  /** 
   * Shows the connection dialog.
   */
  private void showDialog() {
    final JDialog dialog = new JDialog(this, "Connection Properties", true);
    dialog.setSize(new Dimension(400, 250));
    dialog.setLocationRelativeTo(this);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    BaseXBack connection = new BaseXBack();
    connection.setLayout(new TableLayout(7, 2));
    final BaseXTextField sname = new BaseXTextField(this);
    final BaseXTextField sport = new BaseXTextField(this);
    final BaseXTextField suser = new BaseXTextField(this);
    final BaseXPassword spw = new BaseXPassword(this);
    final BaseXLabel message = new BaseXLabel();
    connection.setBorder(8, 8, 8, 8);
    connection.add(new BaseXLabel("Server: "));
    connection.add(sname);
    connection.add(new BaseXLabel("Port: "));
    connection.add(sport);
    connection.add(new BaseXLabel("Username: "));
    connection.add(suser);
    connection.add(new BaseXLabel("Password: "));
    connection.add(spw);
    BaseXButton connect = new BaseXButton("Connect", this);
    connection.add(new BaseXLabel("       "));
    connection.add(connect);
    connection.add(new BaseXLabel("       "));
    connection.add(new BaseXLabel("       "));
    connection.add(new BaseXLabel(""));
    connection.add(message);
    connect.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        try {
          final int p = Integer.parseInt(sport.getText());
          client = new ClientSession(sname.getText(), p, suser.getText(),
              new String(spw.getPassword()));
          servertitle = "Server - " + suser.getText() + "@" + 
            sname.getText() + ":" + sport.getText();
        } catch(IOException e1) {
          message.setText("Connection failed.");
        } catch(NumberFormatException ne) {
          message.setText("Port has to be a number.");
        }
        if(client != null) {
          dialog.dispose();
          addComponents();
        }
      }
    });
    dialog.getContentPane().add(connection);
    dialog.setVisible(true);
  }
  
  /**
   * Adds the main components.
   */
  void addComponents() {
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setLeftComponent(getTree());
    splitPane.setRightComponent(new JPanel());
    add(splitPane);
    validate();
  }
  
  /**
   * Returns the tree.
   * @return tree JTree
   */
  private JTree getTree() {
    return new MainTree(servertitle).getTree();
  }
}
