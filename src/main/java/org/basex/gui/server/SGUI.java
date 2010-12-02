package org.basex.gui.server;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.gui.AGUI;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXPassword;
import org.basex.gui.layout.BaseXTabs;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.gui.server.MainTree.TreeNode;
import org.basex.server.ClientSession;

/**
 * This class is the main window of the BaseXServerGUI. It is the central
 * instance for user interactions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class SGUI extends AGUI {

  /** Clientsession. */
  ClientSession client;
  /** Name of the server. */
  String title;
  /** Right side of main window. */
  BaseXTabs right = new BaseXTabs(this);

  /**
   * Default constructor.
   * @param ctx context reference
   * @param gprops gui properties
   */
  public SGUI(final Context ctx, final GUIProp gprops) {
    super(ctx, gprops);
    // set window size
    final Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
    final int[] ps = gprop.nums(GUIProp.GUILOC);
    final int[] sz = gprop.nums(GUIProp.GUISIZE);
    final int x = Math.max(0, Math.min(scr.width - sz[0], ps[0]));
    final int y = Math.max(0, Math.min(scr.height - sz[1], ps[1]));
    setBounds(x, y, sz[0], sz[1]);
    if(gprop.is(GUIProp.MAXSTATE)) {
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
    final BaseXBack connection = new BaseXBack();
    connection.setLayout(new TableLayout(7, 2));
    final BaseXTextField sname = new BaseXTextField("localhost", this);
    final BaseXTextField sport = new BaseXTextField("1984", this);
    final BaseXTextField suser = new BaseXTextField("admin", this);
    final BaseXPassword spw = new BaseXPassword(this);
    final BaseXLabel message = new BaseXLabel();
    connection.border(8, 8, 8, 8);
    connection.add(new BaseXLabel("Server: "));
    connection.add(sname);
    connection.add(new BaseXLabel("Port: "));
    connection.add(sport);
    connection.add(new BaseXLabel("Username: "));
    connection.add(suser);
    connection.add(new BaseXLabel("Password: "));
    connection.add(spw);
    final BaseXButton connect = new BaseXButton("Connect", this);
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
          title = "Server - " + suser.getText() + "@" + sname.getText() + ":"
              + sport.getText();
        } catch(final IOException e1) {
          message.setText("Connection failed.");
        } catch(final NumberFormatException ne) {
          message.setText("Port has to be a number.");
        }
        if(client != null) {
          dialog.dispose();
          try {
            addComponents();
          } catch(final BaseXException ex) {
            ex.printStackTrace();
          }
        }
      }
    });
    dialog.getContentPane().add(connection);
    dialog.setVisible(true);
  }

  /**
   * Adds the main components.
   * @throws BaseXException database exception
   */
  void addComponents() throws BaseXException {
    final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setLeftComponent(initTree());
    splitPane.setRightComponent(right);
    add(splitPane);
    validate();
  }

  /**
   * Initializes the main tree.
   * @return tree
   * @throws BaseXException database exception
   */
  private JTree initTree() throws BaseXException {
    final JTree jt = new MainTree(title, client).getTree();
    jt.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent evt) {
        if(evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() == 2) {
          // retrieve selected node
          final TreeNode node = (TreeNode) jt.getLastSelectedPathComponent();
          if(node.getType() == 1) {
            final String t = node.getParent() + " - " + node;
            try {
              addTab(t, node.getIcon());
            } catch(final BaseXException e) {
              e.printStackTrace();
            }
          }
        }
      }
    });
    return jt;
  }

  /**
   * Adds a tab to the right side.
   * @param t type string
   * @param ico icon
   * @throws BaseXException database exception
   */
  public void addTab(final String t, final Icon ico) throws BaseXException {
    final BaseXBack b = new BaseXBack(Fill.NONE);
    b.setLayout(new TableLayout(1, 4));
    final BaseXLabel tit = new BaseXLabel(t);
    tit.setIcon(ico);
    b.add(tit);
    final BaseXLabel w = new BaseXLabel("  ");
    b.add(w);
    final BaseXLabel close = new BaseXLabel();
    close.setIcon(BaseXLayout.icon("cmd-close"));
    close.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1) {
          right.remove(right.getSelectedIndex());
        }
      }
    });
    b.add(close);
    right.addTab(t, ico, new Tab(t, this));
    right.setTabComponentAt(right.getTabCount() - 1, b);
  }
}
