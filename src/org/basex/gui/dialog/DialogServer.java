package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
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

  /** Key listener. */
  final KeyAdapter keys = new KeyAdapter() {
    @Override
    public void keyReleased(final KeyEvent e) {
      action(null);
    }
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
  /** Info label. */
  BaseXLabel infop1;
  /** Info label. */
  BaseXLabel infop2;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogServer(final GUI main) {
    super(main, SERVERTITLE);

    tabs = new BaseXTabs(this);
    tabs.setPreferredSize(new Dimension(450, 350));

    // Server panel
    p1.setLayout(new TableLayout(3, 1));
    // User management panel
    p2.setLayout(new TableLayout(8, 1, 0, 4));
    p2.setBorder(8, 8, 8, 8);

    tabs.add(SERVER, p1);
    tabs.add(USERS, p2);

    start = new BaseXButton(START, null, this);
    stop = new BaseXButton(STOP, null, this);
    host = new BaseXTextField(ctx.prop.get(Prop.HOST), null, this);
    port = new BaseXTextField(Integer.toString(ctx.prop.num(Prop.PORT)), null,
        this);
    port.addKeyListener(keys);
    host.addKeyListener(keys);
    change = new BaseXButton(CHANGE, null, this);

    // Start-stop server panel
    final BaseXBack p11 = new BaseXBack();
    p11.setLayout(new TableLayout(1, 2, 6, 0));
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
    infop1 = new BaseXLabel(" ");
    infop1.setBorder(40, 0, 0, 0);
    p1.add(infop1);
    set(tabs, BorderLayout.CENTER);

    user = new BaseXTextField("", null, this);
    user.addKeyListener(keys);
    create = new BaseXButton(CREATE, null, this);
    pass = new JPasswordField(10);
    pass.addKeyListener(keys);
    userco = new BaseXCombo(new String[] {}, null, this);
    delete = new BaseXButton(DROP, null, this);
    table = new JTable(new TableModel());
    table.setPreferredScrollableViewportSize(new Dimension(420, 100));

    p2.add(new BaseXLabel(CREATEUSER, false, true));
    final BaseXBack p21 = new BaseXBack();
    p21.setLayout(new TableLayout(1, 5, 6, 0));
    p21.add(new BaseXLabel(SERVERUSER));
    BaseXLayout.setWidth(user, 100);
    p21.add(user);
    p21.add(new BaseXLabel(SERVERPW));
    p21.add(pass);
    p21.add(create);
    p2.add(p21);
    p2.add(new BaseXLabel(DROPUSER, false, true));
    final BaseXBack p22 = new BaseXBack();
    p22.setLayout(new TableLayout(1, 2, 6, 0));
    p22.add(userco);
    p22.add(delete);
    p2.add(p22);
    p2.add(Box.createVerticalStrut(8));
    p2.add(new BaseXLabel(PERMISSIONS + COL, false, true));
    p2.add(new JScrollPane(table));
    infop2 = new BaseXLabel(" HELLO ");
    infop2.setBorder(40, 0, 0, 0);
    p2.add(infop2);

    // test if server is running
    try {
      createSession();
    } catch(final IOException e1) { }

    action(null);
    finish(null);
  }

  /**
   * Creates a new client session.
   * @throws IOException I/O exception
   */
  private void createSession() throws IOException {
    cs = new ClientSession(ctx, ADMIN, ADMIN);
    setData();
  }

  @Override
  public void action(final String cmd) {
    if(START.equals(cmd)) {
      try {
        // [AW] Prop,work + "bin" will not always work. hmm..
        // - possible solution: add HOME key to .basex config file
        // - next: other distributions (jar file..) should be supported as well
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
      }
    } else if(CHANGE.equals(cmd)) {
      ctx.prop.set(Prop.HOST, host.getText());
      try {
        final int p = Integer.parseInt(port.getText());
        ctx.prop.set(Prop.PORT, p);
      } catch(final NumberFormatException n) {
        // [CG] can be ignored if input is verified before being set
        port.setText(Integer.toString(ctx.prop.num(Prop.PORT)));
      }
    } else if(CREATE.equals(cmd)) {
      final String u = user.getText();
      final String p = new String(pass.getPassword());
      try {
        cs.execute(new CreateUser(u, p));
        user.setText("");
        pass.setText("");
        setData();
      } catch(final IOException ex) {
        // [AW] to be visualized somewhere...
        Main.debug(ex);
      }
    } else if(DROP.equals(cmd)) {
      final String test = (String) userco.getSelectedItem();
      try {
        cs.execute(new DropUser(test));
        setData();
      } catch(final IOException ex) {
        // [AW] to be visualized somewhere...
        Main.debug(ex);
      }
    }

    // [AW] info labels should be added for simple input checks
    // (hosts/ports, user/passwords, see: DialogCreate.info)
    final boolean run = cs == null;
    stop.setEnabled(!run);
    start.setEnabled(run);
    host.setEnabled(run);
    port.setEnabled(run);
    boolean onep1 = port.getText().matches("^[0-9]+$");
    boolean twop1 = host.getText().isEmpty();
    change.setEnabled(run && onep1 && !twop1);
    if(!onep1 || twop1) {
      infop1.setIcon(BaseXLayout.icon("warn"));
      if(!onep1) {
        infop1.setText("Invalid port");
      } else {
        infop1.setText("Invalid hostname");
      }
    } else {
      infop1.setText("");
      infop1.setIcon(null);
    }
    tabs.setEnabledAt(1, !run);
    boolean onep2 = user.getText().matches("^[A-Za-z0-9_.-]+$");
    boolean twop2 = new String(pass.getPassword()).matches("^[A-Za-z0-9_.-]+$");
    create.setEnabled(onep2 && twop2);
    if(!onep2 && !user.getText().isEmpty()) {
      infop2.setIcon(BaseXLayout.icon("warn"));
      infop2.setText("Invalid username");
    } else if(!twop2 && !new String(pass.getPassword()).isEmpty()) {
      infop2.setIcon(BaseXLayout.icon("warn"));
      infop2.setText("Invalid password");
    } else {
      infop2.setText("");
      infop2.setIcon(null);
    }
    delete.setEnabled(data.size() != 0);
  }

  /**
   * Sets new data.
   */
  void setData() {
    fillLists();
    ((TableModel) table.getModel()).fireTableDataChanged();
    userco.removeAllItems();
    for(final Object[] o : data)
      userco.addItem(o[0]);
  }

  /**
   * Fills all lists.
   */
  void fillLists() {
    try {
      final CachedOutput out = new CachedOutput();
      cs.execute(new Show("Users"), out);
      data = new ArrayList<Object[]>();
      for(final Object[] o : table(out.toString())) {
        for(int i = 1; i < o.length; i++)
          o[i] = o[i].toString().length() != 0;
        data.add(o);
      }
    } catch(final IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Parses the user table. [AW] might be generalized and moved to another class
   * @param info input
   * @return list
   */
  private ArrayList<Object[]> table(final String info) {
    // not optimized yet
    final ArrayList<Object[]> list = new ArrayList<Object[]>();
    final Scanner s = new Scanner(info);
    // header is used to calculate column widths
    String line = s.nextLine();
    final IntList il = new IntList();
    int i = 0;
    while(i < line.length()) {
      il.add(i);
      while(++i < line.length() && line.charAt(i) != ' ')
        ;
      while(++i < line.length() && line.charAt(i) == ' ')
        ;
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
   * Dialog specific table model.
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

    @Override
    public Class<?> getColumnClass(final int c) {
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
      else if(col == 2) right = CmdPerm.WRITE.toString();
      else if(col == 3) right = CmdPerm.CREATE.toString();
      else if(col == 4) right = CmdPerm.ADMIN.toString();
      final String uname = (String) data.get(row)[0];
      try {
        cs.execute(value.equals(true) ? new Grant(right, uname) : new Revoke(
            right, uname));
        data.get(row)[col] = value;
        fireTableCellUpdated(row, col);
      } catch(final IOException ex) {
        // [AW] to be visualized somewhere...
        ex.printStackTrace();
      }
    }
  }
}
