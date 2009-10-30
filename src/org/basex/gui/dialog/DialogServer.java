package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.Socket;
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
import org.basex.core.proc.Exit;
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
import org.basex.server.LoginException;
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
  /** Disconnect button. */
  final BaseXButton disconnect;
  /** Server host. */
  final BaseXTextField host;
  /** Local server port. */
  final BaseXTextField ports;
  /** Server port. */
  final BaseXTextField portc;
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
  /** Boolean for check is server is running. */
  boolean run;
  /** Boolean for check if client is connected. */
  boolean connected;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogServer(final GUI main) {
    super(main, SRVTITLE);

    tabs = new BaseXTabs(this);

    // Server panel
    p1.setLayout(new TableLayout(6, 1, 0, 4));
    p1.setBorder(8, 8, 8, 8);
    // User management panel
    p2.setLayout(new TableLayout(13, 1, 0, 4));
    p2.setBorder(8, 8, 8, 8);

    tabs.add(SERVERN, p1);
    tabs.add(USERS, p2);

    start = new BaseXButton(BUTTONSTASERV, null, this);
    stop = new BaseXButton(BUTTONSTOSERV, null, this);
    connect = new BaseXButton(BUTTONCONNECT, null, this);
    disconnect = new BaseXButton(BUTTONDISCONNECT, null, this);
    host = new BaseXTextField(ctx.prop.get(Prop.HOST), null, this);
    host.addKeyListener(keys);
    ports = new BaseXTextField(
        Integer.toString(ctx.prop.num(Prop.SERVERPORT)), null, this);
    ports.addKeyListener(keys);
    portc = new BaseXTextField(
        Integer.toString(ctx.prop.num(Prop.PORT)), null, this);
    portc.addKeyListener(keys);
    loguser = new BaseXTextField(ADMIN, null, this);
    loguser.addKeyListener(keys);
    logpass = new JPasswordField();
    logpass.addKeyListener(keys);

    p1.add(new BaseXLabel(LOCAL + SERVERN + COLS, false, true));

    // Local server panel.
    final BaseXBack p11 = new BaseXBack();
    p11.setLayout(new TableLayout(2, 2, 0, 2));
    p11.add(new BaseXLabel(PORT + COLS));
    p11.add(ports);

    final BaseXBack p111 = new BaseXBack();
    p111.setLayout(new TableLayout(1, 2, 2, 2));
    p111.add(start);
    p111.add(stop);
    p11.add(new BaseXLabel(" "));
    p11.add(p111);

    // Login panel.
    final BaseXBack p12 = new BaseXBack();
    p12.setLayout(new TableLayout(6, 2, 2, 2));
    p12.add(new BaseXLabel(LOGIN + COLS, false, true));
    p12.add(new BaseXLabel(" "));
    p12.add(new BaseXLabel(SERVERUSER + COLS));
    p12.add(loguser);
    p12.add(new BaseXLabel(SERVERPW + COLS));
    BaseXLayout.setWidth(logpass, 200);
    p12.add(logpass);
    p12.add(new BaseXLabel(HOST + COLS));
    p12.add(host);
    p12.add(new BaseXLabel(PORT + COLS));
    p12.add(portc);
    p12.add(new BaseXLabel(" "));
    final BaseXBack p121 = new BaseXBack();
    p121.setLayout(new TableLayout(1, 2, 2, 2));
    p121.add(connect);
    p121.add(disconnect);
    p12.add(p121);

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
    p21.setBorder(0, 0, 5, 0);
    p21.add(new BaseXLabel(SERVERUSER));
    p21.add(user);
    p21.add(new BaseXLabel(SERVERPW));
    p21.add(pass);
    p21.add(create);
    p2.add(p21);
    p2.add(new BaseXLabel(ALTERPW + COLS, false, true));

    final BaseXBack p22 = new BaseXBack();
    p22.setLayout(new TableLayout(1, 4, 6, 0));
    p22.setBorder(0, 0, 5, 0);
    newpass = new JPasswordField();
    alter = new BaseXButton(BUTTONALTER, null, this);
    userco2 = new BaseXCombo(new String[] {}, null, this);
    newpass.addKeyListener(keys);
    BaseXLayout.setWidth(newpass, 100);
    p22.add(userco2);
    p22.add(new BaseXLabel(NEWPW));
    p22.add(newpass);
    p22.add(alter);
    p2.add(p22);
    p2.add(new BaseXLabel(PERMS, false, true));
    p2.add(new JScrollPane(table));
    change = new BaseXButton(BUTTONCHANGE, null, this);

    final BaseXBack p23 = new BaseXBack();
    p23.setLayout(new BorderLayout());
    p23.add(new BaseXLabel(DROPU + COLS, false, true), BorderLayout.WEST);
    p23.add(change, BorderLayout.EAST);
    BaseXLayout.setWidth(p23, 420);
    p2.add(p23);

    final BaseXBack p24 = new BaseXBack();
    p24.setLayout(new TableLayout(1, 2, 6, 0));
    p24.setBorder(0, 0, 5, 0);
    p24.add(userco1);
    p24.add(delete);
    p2.add(p24);

    infop2 = new BaseXLabel(" ");
    p2.add(infop2);
    p2.add(Box.createVerticalStrut(16));

    // test if server is running
    Socket s = new Socket();
    try {
      run = true;
      s.connect(new InetSocketAddress("localhost", ctx.prop.num(Prop.PORT)));
    } catch(IOException e) {
      run = false;
    }
    action(null);
    finish(null);
  }

  /**
   * Creates a new client session.
   * @param u user name
   * @param p password
   * @throws IOException I/O exception
   */
  private void createSession(final String u, final String p)
      throws IOException {
    cs = new ClientSession(ctx.prop.get(Prop.HOST),
        ctx.prop.num(Prop.PORT), u, p);
  }

  @Override
  public void action(final String cmd) {
    if(BUTTONSTASERV.equals(cmd)) {
      try {
        final int p = Integer.parseInt(ports.getText());
        ctx.prop.set(Prop.SERVERPORT, p);
        ctx.prop.write();

        final String path = IOFile.file(getClass().getProtectionDomain().
            getCodeSource().getLocation().toString());
        final String mem = "-Xmx" + Runtime.getRuntime().maxMemory();
        final String clazz = org.basex.BaseXServer.class.getName();
        new ProcessBuilder(new String[] { "java", mem, "-cp", path, clazz,
            "-p", String.valueOf(p)}).start();
        run = true;
      } catch(final Exception ex) {
        err1 = BUTTONSTASERV + FAILED + error(ex);
        Main.debug(ex);
      }
    } else if(BUTTONSTOSERV.equals(cmd)) {
      try {
        createSession(ADMIN, ADMIN);
        cs.execute(new IntStop(), null);
        cs = null;
        run = false;
        connected = false;
      } catch(final IOException ex) {
        err1 = BUTTONSTOSERV + FAILED + error(ex);
        Main.debug(ex);
      }
    } else if(BUTTONCHANGE.equals(cmd)) {
      for(final Process p : permps) {
        try {
          cs.execute(p);
        } catch(final IOException e) {
          err2 = BUTTONCHANGE + FAILED + error(e);
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
        err2 = CREATEU + FAILED + error(ex);
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
        err2 = DROPU + FAILED + error(ex);
        Main.debug(ex);
      }
    } else if(BUTTONALTER.equals(cmd)) {
      final String u = (String) userco2.getSelectedItem();
      final String p = new String(newpass.getPassword());
      try {
        cs.execute(new AlterUser(u, p));
      } catch(final IOException e) {
        err2 = ALTERPW + FAILED + error(e);
        Main.debug(e);
      }
    } else if(BUTTONCONNECT.equals(cmd)) {
      try {
        ctx.prop.set(Prop.PORT, Integer.parseInt(ports.getText()));
        ctx.prop.set(Prop.HOST, host.getText());
        ctx.prop.write();

        createSession(loguser.getText(), new String(logpass.getPassword()));
        setData();
        connected = true;
      } catch(Exception e) {
        err1 = BUTTONCONNECT + FAILED + error(e);
      }
    } else if(BUTTONDISCONNECT.equals(cmd)) {
      try {
        cs.execute(new Exit());
        connected = false;
      } catch(final IOException e) {
        err1 = BUTTONDISCONNECT + FAILED + error(e);
        Main.debug(e);
      }
    }

    stop.setEnabled(run);
    ports.setEnabled(!run);
    loguser.setEnabled(!connected);
    logpass.setEnabled(!connected);
    portc.setEnabled(!connected);
    host.setEnabled(!connected);
    boolean valh = host.getText().matches("^([A-Za-z]+://)?[A-Za-z0-9-.]+$");
    boolean valpl = ports.getText().matches("^[0-9]{2,5}$");
    boolean valp = portc.getText().matches("^[0-9]{2,5}$");
    boolean vallu = true;
    if(!loguser.getText().isEmpty()) {
      vallu = loguser.getText().matches("^[A-Za-z0-9_.-]+$");
    }
    boolean vallp = true;
    if(!new String(logpass.getPassword()).isEmpty()) {
      vallp = new String(logpass.getPassword()).matches("^[A-Za-z0-9_.-]+$");
    }
    disconnect.setEnabled(connected);
    start.setEnabled(!run && valpl);
    if(!valpl || !valh || !valp || !vallu || !vallp) {
      infop1.setIcon(BaseXLayout.icon("warn"));
      if(!valh) {
        infop1.setText(HOST + INVALID);
      } else if(!valpl) {
        infop1.setText(LOCAL + PORT + INVALID);
      } else if(!valp) {
        infop1.setText(PORT + INVALID);
      } else if(!vallu) {
        infop1.setText(SERVERUSER + INVALID);
      } else {
        infop1.setText(SERVERPW + INVALID);
      }
    } else if(err1 != null) {
      infop1.setText(err1);
      infop1.setIcon(BaseXLayout.icon("error"));
      err1 = null;
    } else {
      connect.setEnabled(!connected && !loguser.getText().isEmpty()
          && !new String(logpass.getPassword()).isEmpty());
      infop1.setText(" ");
      infop1.setIcon(null);
    }
    tabs.setEnabledAt(1, connected);
    final boolean valuname = user.getText().matches("^[A-Za-z0-9_.-]+$");
    final boolean valpass = new String(pass.getPassword()).
      matches("^[A-Za-z0-9_.-]+$");
    final boolean valnewpass = new String(newpass.getPassword()).
      matches("^[A-Za-z0-9_.-]+$");
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
   * @throws IOException Exception
   */
  void setData() throws IOException {
    fillLists();
    userco1.removeAllItems();
    userco2.removeAllItems();
    StringList tmp = new StringList();
    for(final StringList o : data.contents) {
      final String check = o.get(0);
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
   * @throws IOException Exception
   */
  void fillLists() throws IOException {
    final CachedOutput out = new CachedOutput();
    if(!cs.execute(new Show("Users"), out)) {
      throw new IOException(cs.info());
    }
    data = new Table(out.toString());
  }

  /**
   * Returns a server error message.
   * @param ex exception reference
   * @return String error message
   */
  String error(final Exception ex) {
    if(ex instanceof BindException) {
      return SERVERBIND;
    } else if(ex instanceof LoginException) {
      return SERVERLOGIN;
    } else if(ex instanceof IOException) {
      return SERVERERR;
    } else {
      return ex.getMessage();
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
