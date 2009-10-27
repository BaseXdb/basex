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
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXTabs;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.CachedOutput;
import org.basex.io.IOFile;
import org.basex.server.ClientSession;
import org.basex.util.StringList;
import org.basex.util.Table;

/**
 * Dialog window for displaying information about the server.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class DialogServer extends Dialog {
  /** Context. */
  final Context ctx = gui.context;
  /** ArrayList for table. */
  Table data = new Table();
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
  /** Connect button. */
  final BaseXButton connect;
  /** Server host. */
  final BaseXTextField host;
  /** Local server port. */
  final BaseXTextField portl;
  /** Server port. */
  final BaseXTextField port;
  /** Change button. */
  final BaseXButton change;
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
  /** Username textfield. */
  BaseXTextField loguser;
  /** Password textfield. */
  JPasswordField pass;
  /** Password textfield. */
  JPasswordField newpass;
  /** Password textfield. */
  JPasswordField logpass;
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
  /** String for error messages. */
  String err1;
  /** String for error messages. */
  String err2;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogServer(final GUI main) {
    super(main, SRVTITLE);

    tabs = new BaseXTabs(this);
    
    // Server panel
    p1.setLayout(new TableLayout(5, 1, 0, 4));
    p1.setBorder(8, 8, 8, 8);
    // User management panel
    p2.setLayout(new TableLayout(12, 1, 0, 4));
    p2.setBorder(8, 8, 8, 8);

    tabs.add(SERVERN, p1);
    tabs.add(USERS, p2);

    start = new BaseXButton(BUTTONSTASERV, null, this);
    stop = new BaseXButton(BUTTONSTOSERV, null, this);
    connect = new BaseXButton(BUTTONCONNECT, null, this);
    host = new BaseXTextField(ctx.prop.get(Prop.HOST), null, this);
    portl = new BaseXTextField(Integer.toString(ctx.prop.num(Prop.PORT)), null,
        this);
    port = new BaseXTextField(Integer.toString(ctx.prop.num(Prop.PORT)), null,
        this);
    portl.addKeyListener(keys);
    port.addKeyListener(keys);
    host.addKeyListener(keys);

    // Local server panel.
    final BaseXBack p11 = new BaseXBack();
    p11.setLayout(new TableLayout(3, 2, -18, 2));
    p11.add(new BaseXLabel(LOCAL + SERVERN + COLS, false, true));
    p11.add(new BaseXLabel(" "));
    p11.add(new BaseXLabel(PORT + COLS));
    p11.add(portl);
    p11.add(new BaseXLabel(" "));
    final BaseXBack p111 = new BaseXBack();
    p111.setLayout(new TableLayout(1, 2, 2, 2));
    p111.add(start);
    p111.add(stop);
    p11.add(p111);
    
    // Login panel.
    final BaseXBack p12 = new BaseXBack();
    p12.setLayout(new TableLayout(6, 2, 2, 2));
    p12.add(new BaseXLabel(LOGIN + COLS, false, true));
    p12.add(new BaseXLabel(" "));
    p12.add(new BaseXLabel(SERVERUSER + COLS));
    loguser = new BaseXTextField("", null, this);
    loguser.addKeyListener(keys);
    p12.add(loguser);
    p12.add(new BaseXLabel(SERVERPW + COLS));
    logpass = new JPasswordField();
    logpass.addKeyListener(keys);
    BaseXLayout.setWidth(logpass, 200);
    p12.add(logpass);
    p12.add(new BaseXLabel(HOST + COLS));
    p12.add(host);
    p12.add(new BaseXLabel(PORT + COLS));
    p12.add(port);
    p12.add(new BaseXLabel(" "));
    p12.add(connect);
    
    // adding to main panel
    p11.setBorder(8, 8, 8, 8);
    p12.setBorder(8, 8, 8, 8);
    infop1 = new BaseXLabel(" ");
    infop1.setBorder(40, 0, 0, 0);
    
    p1.add(p11);
    p1.add(p12);
    p1.add(infop1);
    set(tabs, BorderLayout.CENTER);

    user = new BaseXTextField("", null, this);
    user.addKeyListener(keys);
    BaseXLayout.setWidth(user, 100);
    create = new BaseXButton(BUTTONCREATE, null, this);
    pass = new JPasswordField();
    pass.addKeyListener(keys);
    BaseXLayout.setWidth(pass, 100);
    userco1 = new BaseXCombo(new String[] {}, null, this);
    delete = new BaseXButton(BUTTONDROP, null, this);
    table = new JTable(new TableModel());
    table.setPreferredScrollableViewportSize(new Dimension(420, 100));

    p2.add(new BaseXLabel(CREATEU + COLS, false, true));
    final BaseXBack p21 = new BaseXBack();
    p21.setLayout(new TableLayout(1, 5, 6, 0));
    p21.add(new BaseXLabel(SERVERUSER));
    p21.add(user);
    p21.add(new BaseXLabel(SERVERPW));
    p21.add(pass);
    p21.add(create);
    p2.add(p21);
    p2.add(new BaseXLabel(DROPU + COLS, false, true));
    final BaseXBack p22 = new BaseXBack();
    p22.setLayout(new TableLayout(1, 2, 6, 0));
    p22.add(userco1);
    p22.add(delete);
    p2.add(p22);
    final BaseXBack p23 = new BaseXBack();
    p23.setLayout(new TableLayout(2, 4, 6, 0));
    p23.add(new BaseXLabel(ALTERPW + COLS, false, true));
    p23.add(new BaseXLabel(" "));
    p23.add(new BaseXLabel(" "));
    p23.add(new BaseXLabel(" "));
    newpass = new JPasswordField();
    alter = new BaseXButton(BUTTONALTER, null, this);
    userco2 = new BaseXCombo(new String[] {}, null, this);
    newpass.addKeyListener(keys);
    BaseXLayout.setWidth(newpass, 100);
    p23.add(userco2);
    p23.add(new BaseXLabel(NEWPW));
    p23.add(newpass);
    p23.add(alter);
    p21.setBorder(0, 0, 5, 0);
    p22.setBorder(0, 0, 5, 0);
    p23.setBorder(0, 0, 5, 0);
    p2.add(p23);
    p2.add(Box.createVerticalStrut(8));
    p2.add(new BaseXLabel(PERMS, false, true));
    p2.add(new JScrollPane(table));
    infop2 = new BaseXLabel(" ");
    change = new BaseXButton(BUTTONCHANGE, null, this);
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
    if(BUTTONSTASERV.equals(cmd)) {
      try {
        final String path = IOFile.file(getClass().getProtectionDomain().
            getCodeSource().getLocation().toString());
        final String mem = "-Xmx" + Runtime.getRuntime().maxMemory();
        final String clazz = org.basex.BaseXServer.class.getName();
        new ProcessBuilder(
            new String[] { "java", mem, "-cp", path, clazz, "-n",
                host.getText() }).start();
        createSession();
        ctx.prop.set(Prop.HOST, host.getText());
        final int p = Integer.parseInt(portl.getText());
        ctx.prop.set(Prop.PORT, p);
      } catch(final Exception ex) {
        err1 = BUTTONSTASERV + FAILED + ex.getMessage();
        Main.debug(ex);
      }
    } else if(BUTTONSTOSERV.equals(cmd)) {
      try {
        cs.execute(new IntStop(), null);
        cs = null;
      } catch(final IOException ex) {
        err1 = BUTTONSTOSERV + FAILED + ex.getMessage();
        Main.debug(ex);
      }
    } else if(BUTTONCHANGE.equals(cmd)) {
      for(Process p : permps) {
        try {
          cs.execute(p);
        } catch(IOException e) {
          err2 = BUTTONCHANGE + FAILED + e.getMessage();
          Main.debug(e);
        }
      }
      permps.clear();
    } else if(BUTTONCREATE.equals(cmd)) {
      final String u = user.getText();
      final String p = new String(pass.getPassword());
      try {
        cs.execute(new CreateUser(u, p));
        user.setText("");
        pass.setText("");
        setData();
      } catch(final IOException ex) {
        err2 = CREATEU + FAILED + ex.getMessage();
        Main.debug(ex);
      }
    } else if(BUTTONDROP.equals(cmd)) {
      try {
        final String u = (String) userco1.getSelectedItem();
        if(Dialog.confirm(this, Main.info(DRQUESTION, u))) {
          cs.execute(new DropUser(u));
          setData();
        }
      } catch(final Exception ex) {
        err2 = DROPU + FAILED + ex.getMessage();
        Main.debug(ex);
      }
    } else if(BUTTONALTER.equals(cmd)) {
      final String u = (String) userco2.getSelectedItem();
      final String p = new String(newpass.getPassword());
      try {
        cs.execute(new AlterUser(u, p));
      } catch(IOException e) {
        err2 = ALTERPW + FAILED + e.getMessage();
        Main.debug(e);
      }
    } else if(BUTTONCONNECT.equals(cmd)) {
      //[AW] to be implemented...
    }
    final boolean run = cs == null;
    //[AW] to be revised...
    stop.setEnabled(!run);
    portl.setEnabled(run);
    boolean valh = host.getText().matches("^([A-Za-z]+://)?[A-Za-z0-9-.]+$");
    boolean valpl = portl.getText().matches("^[0-9]{2,5}$");
    boolean valp = port.getText().matches("^[0-9]{2,5}$");
    start.setEnabled(run && valpl);
    if(!valpl || !valh || !valp) {
      infop1.setIcon(BaseXLayout.icon("warn"));
      if(!valh) {
        infop1.setText(HOST + INVALID);
      } else if(!valpl) {
        infop1.setText(LOCAL + PORT + INVALID);
      } else {
        infop1.setText(PORT + INVALID);
      }
    } else if(err1 != null) {
      infop1.setText(err1);
      infop1.setIcon(BaseXLayout.icon("error"));
      err1 = null;
    } else {
      infop1.setText(" ");
      infop1.setIcon(null);
    }
    tabs.setEnabledAt(1, !run);
    boolean valuname = user.getText().matches("^[A-Za-z0-9_.-]+$");
    boolean valpass = new String(
        pass.getPassword()).matches("^[A-Za-z0-9_.-]+$");
    boolean valnewpass = new String(
        newpass.getPassword()).matches("^[A-Za-z0-9_.-]+$");
    alter.setEnabled(valnewpass);
    create.setEnabled(valuname && valpass);
    if(!valuname && !user.getText().isEmpty()) {
      infop2.setIcon(BaseXLayout.icon("warn"));
      infop2.setText(SERVERUSER + INVALID);
    } else if((!valpass && !new String(pass.getPassword()).isEmpty())
        || (!valnewpass && !new String(newpass.getPassword()).isEmpty())) {
      infop2.setIcon(BaseXLayout.icon("warn"));
      infop2.setText(SERVERPW + INVALID);
    } else if(err2 != null) {
      infop2.setText(err2);
      infop2.setIcon(BaseXLayout.icon("error"));
      err2 = null;
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
    userco1.removeAllItems();
    userco2.removeAllItems();
    StringList tmp = new StringList();
    for(final StringList o : data.contents) {
      String check = o.get(0);
      if(!check.equals(ADMIN)) {
        userco1.addItem(check);
        userco2.addItem(check);
      } else {
        userco2.addItem(check);
        tmp = o;
      }
    }
    data.contents.remove(tmp);
    ((TableModel) table.getModel()).fireTableChanged(null);
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
      final String o = data.contents.get(row).get(col);
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
      final String uname = data.contents.get(row).get(0);
      final String right = CmdPerm.values()[col - 1].toString();
      permps.add(value.equals(true) ? new Grant(right, uname) : new Revoke(
          right, uname));
      data.contents.get(row).set(value == Boolean.TRUE ? "X" : "", col);
      fireTableCellUpdated(row, col);
    }
  }
}
