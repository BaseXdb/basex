package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import org.basex.core.Commands.CmdPerm;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.proc.CreateUser;
import org.basex.core.proc.DropUser;
import org.basex.core.proc.Grant;
import org.basex.core.proc.IntStop;
import org.basex.core.proc.Revoke;
import org.basex.core.proc.Show;
import org.basex.gui.GUI;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXTabs;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.CachedOutput;
import org.basex.server.ClientSession;
import org.basex.util.IntList;

/**
 * Dialog window for displaying information about the server.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class DialogServer extends Dialog {
  // [AW] to be externalized...
  /** Server string. */
  static final String START = "Start server";
  /** Server string. */
  static final String STOP = "Stop server";
  /** Server string. */
  static final String CHANGE = "Change";
  /** Server string. */
  static final String SERVERTITLE = "Server Properties";
  /** Server string. */
  static final String SERVER = "Server";
  /** Server string. */
  static final String USERS = "Users";
  /** Server string. */
  static final String HOST = "Host" + COLS;
  /** Server string. */
  static final String PORT = "Port" + COLS;
  /** Server string. */
  static final String CREATEUSER = "Create User" + COL;
  /** Server string. */
  static final String DROPUSER = "Drop User" + COL;
  /** Server string. */
  static final String CREATE = "Create";
  /** Server string. */
  static final String DROP = "Drop";
  /** Server string. */
  static final String PERMISSIONS = "Permissions";
  
  /** ArrayList for table. */
  ArrayList<Object[]> data = new ArrayList<Object[]>();
  /** Context. */
  final Context ctx = gui.context;
  /** ClientSession. */
  ClientSession cs;
  /** Vector for combobox. */
  Vector<String> usernames = new Vector<String>();

  /** Key listener. */
  final KeyAdapter keys = new KeyAdapter() {
    @Override
    public void keyReleased(final KeyEvent e) { action(null); }
  };

  /** Server panel. */
  final BaseXBack p1 = new BaseXBack();
  /** User panel. */
  final BaseXBack p2 = new BaseXBack();
  /** Stop button. */
  final BaseXButton stop;
  /** Start button. */
  final BaseXButton start;
  /** Server host. */
  final BaseXTextField host;
  /** Server port. */
  final BaseXTextField port;
  /** Change button. */
  final BaseXButton change;
  /** Change button. */
  final BaseXTabs tabs;
  /** Create button. */
  BaseXButton create;
  /** Delete button. */
  BaseXButton delete;
  /** Delete button. */
  BaseXTextField user;
  /** Delete button. */
  JPasswordField pass;
  /** User columns. */
  BaseXCombo userco;
  /** User table. */
  JTable table; 

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogServer(final GUI main) {
    super(main, SERVERTITLE);

    tabs = new BaseXTabs(this);
    tabs.setPreferredSize(new Dimension(450, 300));

    // Server panel
    p1.setLayout(new TableLayout(3, 1));
    // User management panel
    p2.setLayout(new TableLayout(7, 1, 0, 4));
    p2.setBorder(8, 8, 8, 8);

    tabs.add(SERVER, p1);
    tabs.add(USERS, p2);

    // Start-stop server panel
    final BaseXBack p11 = new BaseXBack();
    p11.setLayout(new TableLayout(1, 2, 6, 0));

    start = new BaseXButton(START, null, this);
    stop = new BaseXButton(STOP, null, this);
    host = new BaseXTextField(ctx.prop.get(Prop.HOST), null, this);
    port = new BaseXTextField(
        Integer.toString(ctx.prop.num(Prop.PORT)), null, this);
    port.addKeyListener(keys);
    change = new BaseXButton(CHANGE, null, this);

    // test if server is running
    try { createSession(); } catch(final IOException e1) { }

    p11.add(start);
    p11.add(stop);

    // Server preferences panel.
    final BaseXBack p12 = new BaseXBack();
    p12.setLayout(new TableLayout(3, 2, 2, 2));
    p12.add(new BaseXLabel(HOST));
    p12.add(host);
    p12.add(new BaseXLabel(PORT));
    p12.add(port);
    p12.add(new BaseXLabel(""));
    p12.add(change);
    p12.setBorder(8, 8, 8, 8);
    p11.setBorder(8, 8, 8, 8);

    // adding to main panel
    p1.add(p11);
    p1.add(p12);
    set(tabs, BorderLayout.CENTER);
    action(null);
    finish(null);
  }

  /**
   * Creates a new client session.
   * @throws IOException I/O exception
   */
  private void createSession() throws IOException {
    cs = new ClientSession(ctx, ADMIN, ADMIN);
    fillLists();
    fillUserTab();
  }

  @Override
  public void action(final String cmd) {
    if(START.equals(cmd)) {
      try {
        // [AW] Prop,work + "bin" will not always work.
        // hmm.. possible solution: add HOME key to .basex config file
        final ProcessBuilder pb = new ProcessBuilder("java", "-cp", Prop.WORK
            + "bin", org.basex.BaseXServer.class.getName());
        pb.start();
        createSession();
      } catch(final IOException ex) {
        // [AW] to be visualized somewhere...
        Main.debug(ex);
        ex.printStackTrace();
      }
    } else if(STOP.equals(cmd)) {
      try {
        cs.execute(new IntStop(), null);
        cs = null;
      } catch(final IOException ex) {
        // [AW] to be visualized somewhere...
        Main.debug(ex);
        ex.printStackTrace();
      }
    } else if(CHANGE.equals(cmd)) {
      ctx.prop.set(Prop.HOST, host.getText());
      try {
        final int p = Integer.parseInt(port.getText());
        ctx.prop.set(Prop.PORT, p);
      } catch(final NumberFormatException n) {
        port.setText(Integer.toString(ctx.prop.num(Prop.PORT)));
      }
    } else if(CREATE.equals(cmd)) {
      final String u = user.getText();
      final String p = new String(pass.getPassword());
      try {
        cs.execute(new CreateUser(u, p));
        user.setText("");
        pass.setText("");
        ((TableModel) table.getModel()).setData();
        userco.addItem(u);
        delete.setEnabled(true);
      } catch(final IOException e1) {
        e1.printStackTrace();
      }
    } else if(DROP.equals(cmd)) {
      final String test = (String) userco.getSelectedItem();
      try {
        cs.execute(new DropUser(test));
        userco.removeItem(test);
        ((TableModel) table.getModel()).setData();
        if(usernames.size() == 0) delete.setEnabled(false);
      } catch(final IOException e1) {
        e1.printStackTrace();
      }
    }

    // [AW] info labels should be added for simple input checks
    // (hosts/ports, user/passwords, see: DialogCreate.info)
    final boolean run = cs == null;
    stop.setEnabled(!run);
    start.setEnabled(run);
    host.setEnabled(run);
    port.setEnabled(run);
    change.setEnabled(run && port.getText().matches("^[0-9]+$"));
    tabs.setEnabledAt(1, !run);
    if(user != null) {
      create.setEnabled(user.getText().matches("^[A-Za-z0-9_.-]+$") &&
          new String(pass.getPassword()).matches("^[A-Za-z0-9_.-]+$"));
      delete.setEnabled(usernames.size() != 0);
    }
  }
  
  /**
   * Fills the user tab with components.
   */
  void fillUserTab() {
    p2.add(new BaseXLabel(CREATEUSER, false, true));

    final BaseXBack p21 = new BaseXBack();
    p21.setLayout(new TableLayout(1, 5, 6, 0));

    p21.add(new BaseXLabel(SERVERUSER));
    user = new BaseXTextField("", null, this);
    BaseXLayout.setWidth(user, 100);
    user.addKeyListener(keys);
    p21.add(user);
    p21.add(new BaseXLabel(SERVERPW));
    pass = new JPasswordField(10);
    pass.addKeyListener(keys);
    p21.add(pass);
    create = new BaseXButton(CREATE, null, this);
    p21.add(create);
    p2.add(p21);

    p2.add(new BaseXLabel(DROPUSER, false, true));

    final BaseXBack p22 = new BaseXBack();
    p22.setLayout(new TableLayout(1, 2, 6, 0));
    userco = new BaseXCombo(usernames, null, this);
    p22.add(userco);
    delete = new BaseXButton(DROP, null, this);
    p22.add(delete);
    p2.add(p22);

    p2.add(Box.createVerticalStrut(8));
    p2.add(new BaseXLabel(PERMISSIONS + COL, false, true));

    table = new JTable(new TableModel());
    table.setPreferredScrollableViewportSize(new Dimension(420, 100));
    p2.add(new JScrollPane(table));
  }

  /**
   * Fills all lists.
   */
  void fillLists() {
    try {
      final CachedOutput out = new CachedOutput();
      cs.execute(new Show("Users"), out);
      data = new ArrayList<Object[]>();
      usernames = new Vector<String>();
      for(final Object[] o : table(out.toString())) {
        usernames.addElement(o[0].toString());
        for(int i = 1; i < o.length; i++) o[i] = o[i].toString().length() != 0;
        data.add(o);
      }
    } catch(final IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Parses the user table.
   * [AW] might be generalized and moved to another class
   * @param info input
   * @return list
   */
  private ArrayList<Object[]> table(final String info) {
    final ArrayList<Object[]> list = new ArrayList<Object[]>();
    final Scanner s = new Scanner(info);
    String line = s.nextLine();
    final IntList il = new IntList();
    int i = 0;
    while(i < line.length()) {
      il.add(i);
      while(++i < line.length() && line.charAt(i) != ' ');
      while(++i < line.length() && line.charAt(i) == ' ');
    }
    s.nextLine();
    while((line = s.nextLine()).length() != 0) {
      final Object[] entry = new Object[il.size()];
      for(int e = 0; e < entry.length; e++) {
        entry[e] = line.substring(il.get(e),
            e + 1 == entry.length ? line.length() : il.get(e + 1)).trim();
      }
      list.add(entry);
    }
    return list;
  }

  /**
   * Class of own table model.
   *
   * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
   * @author Andreas Weiler
   */
  class TableModel extends AbstractTableModel {
    public int getColumnCount() {
      return USERHEAD.length;
    }

    public int getRowCount() {
      return data.size();
    }

    @Override
    public String getColumnName(final int col) {
      return USERHEAD[col];
    }

    public Object getValueAt(final int row, final int col) {
      return data.get(row)[col];
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class getColumnClass(final int c) {
      return getValueAt(0, c).getClass();
    }

    @Override
    public boolean isCellEditable(final int row, final int col) {
      return col != 0;
    }

    @Override
    public void setValueAt(final Object value, final int row, final int col) {
      String right = "";
      if(col == 1) right = CmdPerm.READ.toString();
      if(col == 2) right = CmdPerm.WRITE.toString();
      if(col == 3) right = CmdPerm.CREATE.toString();
      if(col == 4) right = CmdPerm.ADMIN.toString();
      final String uname = (String) data.get(row)[0];
      if(value.equals(true)) {
        try {
          cs.execute(new Grant(right, uname));
        } catch(final IOException e) {
          e.printStackTrace();
        }
      } else {
        try {
          cs.execute(new Revoke(right, uname));
        } catch(final IOException e) {
          e.printStackTrace();
        }
      }
      data.get(row)[col] = value;
      fireTableCellUpdated(row, col);
    }

    /**
     * Sets new data.
     */
    public void setData() {
      fillLists();
      fireTableDataChanged();
    }

    /**
     * Returns the data after updating.
     * @return object array
     */
    public ArrayList<Object[]> getData() {
      return data;
    }
  }
}
