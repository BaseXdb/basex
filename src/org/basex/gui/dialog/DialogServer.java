package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import org.basex.core.Commands.CmdPerm;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.Process;
import org.basex.core.proc.AlterUser;
import org.basex.core.proc.CreateUser;
import org.basex.core.proc.DropUser;
import org.basex.core.proc.Grant;
import org.basex.core.proc.IntStop;
import org.basex.core.proc.Revoke;
import org.basex.core.proc.Show;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXTabs;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.CachedOutput;
import org.basex.io.IO;
import org.basex.server.ClientSession;
import org.basex.util.Table;

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
  static final String PERMISSIONS = "Permissions" + COL;
  /** Server string. */
  static final String SURE = "Are you really sure?";
  /** Server String. */
  static final String ALTERP = "Alter password" + COL;
  /** Server String. */
  static final String NEWP = "New password" + COL;
  /** Server String. */
  static final String ALTER = "Alter";
  /** Server String. */
  static final String SRVPATH = "Serverpath" + COL;

  /** ArrayList for table. */
  Table data = new Table();
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
  /** Browse button. */
  final BaseXButton browse;
  /** Alter button. */
  final BaseXButton alter;
  /** Change button. */
  final BaseXTabs tabs;
  /** Create button. */
  BaseXButton create;
  /** Delete button. */
  BaseXButton delete;
  /** Username textfield. */
  BaseXTextField user;
  /** Serverpath textfield. */
  BaseXTextField srvpath;
  /** Password textfield. */
  JPasswordField pass;
  /** Password textfield. */
  JPasswordField newpass;
  /** User columns. */
  BaseXCombo userco1;
  /** User columns. */
  BaseXCombo userco2;
  /** User table. */
  JTable table;
  /** Info label. */
  BaseXLabel infop1;
  /** Info label. */
  BaseXLabel infop2;
  /** List of permission processes. */
  ArrayList<Process> permps = new ArrayList<Process>();

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogServer(final GUI main) {
    super(main, SERVERTITLE);

    tabs = new BaseXTabs(this);

    // Server panel
    p1.setLayout(new TableLayout(4, 1));
    // User management panel
    p2.setLayout(new TableLayout(12, 1, 0, 4));
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

    // Start-stop server panel
    final BaseXBack p11 = new BaseXBack();
    p11.setLayout(new TableLayout(1, 2, 6, 0));
    p11.add(start);
    p11.add(stop);

    // Server preferences panel.
    final BaseXBack p12 = new BaseXBack();
    p12.setLayout(new TableLayout(3, 3, 2, 2));
    p12.add(new BaseXLabel(HOST));
    p12.add(host);
    p12.add(new BaseXLabel(""));
    p12.add(new BaseXLabel(PORT));
    p12.add(port);
    p12.add(new BaseXLabel(""));
    browse = new BaseXButton(BUTTONBROWSE, HELPBROWSE, this);
    p12.add(new BaseXLabel(SRVPATH));
    srvpath = new BaseXTextField("C:\\basex\\basex.jar", null, this);
    p12.add(srvpath);
    p12.add(browse);
    // adding to main panel
    p12.setBorder(8, 8, 8, 8);
    p11.setBorder(8, 8, 8, 8);
    p1.add(p12);
    p1.add(p11);
    infop1 = new BaseXLabel(" ");
    infop1.setBorder(40, 0, 0, 0);
    p1.add(infop1);
    set(tabs, BorderLayout.CENTER);

    user = new BaseXTextField("", null, this);
    user.addKeyListener(keys);
    BaseXLayout.setWidth(user, 100);
    create = new BaseXButton(CREATE, null, this);
    pass = new JPasswordField();
    pass.addKeyListener(keys);
    BaseXLayout.setWidth(pass, 100);
    userco1 = new BaseXCombo(new String[] {}, null, this);
    delete = new BaseXButton(DROP, null, this);
    table = new JTable(new TableModel());
    table.setPreferredScrollableViewportSize(new Dimension(420, 100));

    p2.add(new BaseXLabel(CREATEUSER, false, true));
    final BaseXBack p21 = new BaseXBack();
    p21.setLayout(new TableLayout(1, 5, 6, 0));
    p21.add(new BaseXLabel(SERVERUSER));
    p21.add(user);
    p21.add(new BaseXLabel(SERVERPW));
    p21.add(pass);
    p21.add(create);
    p2.add(p21);
    p2.add(new BaseXLabel(DROPUSER, false, true));
    final BaseXBack p22 = new BaseXBack();
    p22.setLayout(new TableLayout(1, 2, 6, 0));
    p22.add(userco1);
    p22.add(delete);
    p2.add(p22);
    final BaseXBack p23 = new BaseXBack();
    p23.setLayout(new TableLayout(2, 4, 6, 0));
    p23.add(new BaseXLabel(ALTERP, false, true));
    p23.add(new BaseXLabel(" "));
    p23.add(new BaseXLabel(" "));
    p23.add(new BaseXLabel(" "));
    newpass = new JPasswordField();
    alter = new BaseXButton(ALTER, null, this);
    userco2 = new BaseXCombo(new String[] {}, null, this);
    newpass.addKeyListener(keys);
    BaseXLayout.setWidth(newpass, 100);
    p23.add(userco2);
    p23.add(new BaseXLabel(NEWP));
    p23.add(newpass);
    p23.add(alter);
    p21.setBorder(0, 0, 5, 0);
    p22.setBorder(0, 0, 5, 0);
    p23.setBorder(0, 0, 5, 0);
    p2.add(p23);
    p2.add(Box.createVerticalStrut(8));
    p2.add(new BaseXLabel(PERMISSIONS, false, true));
    p2.add(new JScrollPane(table));
    infop2 = new BaseXLabel(" ");
    change = new BaseXButton(CHANGE, null, this);
    p2.add(change);
    p2.add(infop2);
    p2.add(new BaseXLabel(" "));

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
      ctx.prop.set(Prop.HOST, host.getText());
      final int p = Integer.parseInt(port.getText());
      ctx.prop.set(Prop.PORT, p);
      final String path = srvpath.getText();
      if(path.endsWith(".jar")) {
        final Runtime run = Runtime.getRuntime();
        try {
          run.exec("java -cp \"" + path + "\" org.basex.BaseXServer");
          //createSession();
        } catch(IOException e) {
          e.printStackTrace();
        }
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
      for(Process p : permps) {
        try {
          cs.execute(p);
        } catch(IOException e) {
          e.printStackTrace();
        }
      }
      permps.clear();
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
      final String u = (String) userco1.getSelectedItem();
      try {
        if(Dialog.confirm(this, SURE)) {
        cs.execute(new DropUser(u));
        setData();
        }
      } catch(final IOException ex) {
        // [AW] to be visualized somewhere...
        Main.debug(ex);
      }
    } else if(ALTER.equals(cmd)) {
      final String u = (String) userco2.getSelectedItem();
      final String p = new String(newpass.getPassword());
      try {
        cs.execute(new AlterUser(u, p));
      } catch(IOException e) {
        e.printStackTrace();
      }
    } else if(BUTTONBROWSE.equals(cmd)) {
      final GUIProp gprop = gui.prop;
      final BaseXFileChooser fc = new BaseXFileChooser(CREATETITLE,
          gprop.get(GUIProp.CREATEPATH), gui);
      fc.addFilter(CREATEJARDESC, IO.JARSUFFIX);
      final IO file = fc.select(BaseXFileChooser.Mode.FDOPEN);
      if(file != null) srvpath.setText(file.path());
    }

    // [AW] info labels should be added for simple input checks
    // (hosts/ports, user/passwords, see: DialogCreate.info)
    final boolean run = cs == null;
    stop.setEnabled(!run);
    host.setEnabled(run);
    port.setEnabled(run);
    srvpath.setEnabled(run);
    browse.setEnabled(run);
    boolean valh = host.getText().matches("^([A-Za-z]+://)?[A-Za-z0-9-.]+$");
    boolean valp = port.getText().matches("^[0-9]{2,5}$");
    start.setEnabled(run && valp && valh);
    if(!valp || !valh) {
      infop1.setIcon(BaseXLayout.icon("warn"));
      if(!valh) {
        infop1.setText("Invalid hostname");
      } else {
        infop1.setText("Invalid port");
      }
    } else {
      infop1.setText(" ");
      infop1.setIcon(null);
    }
    tabs.setEnabledAt(1, !run);
    boolean valuname = user.getText().matches("^[A-Za-z0-9_.-]+$");
    boolean valpass = new String(pass.getPassword()).
      matches("^[A-Za-z0-9_.-]+$");
    boolean valnewpass = new String(newpass.getPassword()).
    matches("^[A-Za-z0-9_.-]+$");
    alter.setEnabled(valnewpass);
    create.setEnabled(valuname && valpass);
    if(!valuname && !user.getText().isEmpty()) {
      infop2.setIcon(BaseXLayout.icon("warn"));
      infop2.setText("Invalid username");
    } else if((!valpass && !new String(pass.getPassword()).isEmpty()) ||
        (!valnewpass && !new String(newpass.getPassword()).isEmpty())) {
      infop2.setIcon(BaseXLayout.icon("warn"));
      infop2.setText("Invalid password");
    } else {
      infop2.setText(" ");
      infop2.setIcon(null);
    }
    delete.setEnabled(data.contents.size() != 0);
  }

  /**
   * Sets new data.
   */
  void setData() {
    fillLists();
    ((TableModel) table.getModel()).fireTableChanged(null);
    userco1.removeAllItems();
    userco2.removeAllItems();
    for(final Object[] o : data.contents) {
      userco2.addItem(o[0]);
      if(!o[0].equals(ADMIN)) {
        userco1.addItem(o[0]);
      }
    }
  }

  /**
   * Fills all lists.
   */
  void fillLists() {
    try {
      final CachedOutput out = new CachedOutput();
      cs.execute(new Show("Users"), out);
      data = new Table(out.toString());
    } catch(final IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Dialog specific table model.
   * 
   * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
   * @author Andreas Weiler
   */
  class TableModel extends AbstractTableModel {
    public int getColumnCount() {
      return data.header.size();
    }

    public int getRowCount() {
      return data.contents.size();
    }

    @Override
    public String getColumnName(final int col) {
      return data.header.get(col);
    }

    public Object getValueAt(final int row, final int col) {
      final String o = data.contents.get(row)[col];
      return o.equals("") ? Boolean.FALSE : o.equals("X") ? Boolean.TRUE : o;
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
      final String uname = data.contents.get(row)[0];
      final String right = CmdPerm.values()[col - 1].toString();
      permps.add(value.equals(true) ? new Grant(right, uname) : new Revoke(
          right, uname));
      data.contents.get(row)[col] = value == Boolean.TRUE ? "X" : "";
      fireTableCellUpdated(row, col);
    }
  }
}
