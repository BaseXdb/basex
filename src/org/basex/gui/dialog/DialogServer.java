package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.swing.JPasswordField;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.proc.Exit;
import org.basex.core.proc.IntStop;
import org.basex.gui.GUI;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXTabs;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.IOFile;
import org.basex.server.ClientSession;
import org.basex.server.LoginException;

/**
 * Dialog window for displaying information about the server.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class DialogServer extends Dialog {
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
  final DialogUser p2 = new DialogUser(true, this);
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
  final BaseXTabs tabs;
  /** Username textfield. */
  BaseXTextField loguser;
  /** Password textfield. */
  JPasswordField logpass;
  /** Info label. */
  BaseXLabel infop1;
  /** String for error messages. */
  String err1;
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

    tabs.add(SERVERN, p1);
    tabs.add(USERS, p2);

    start = new BaseXButton(BUTTONSTASERV, null, this);
    stop = new BaseXButton(BUTTONSTOSERV, null, this);
    connect = new BaseXButton(BUTTONCONNECT, null, this);
    disconnect = new BaseXButton(BUTTONDISCONNECT, null, this);
    host = new BaseXTextField(ctx.prop.get(Prop.HOST), null, this);
    host.addKeyListener(keys);
    ports = new BaseXTextField(Integer.toString(ctx.prop.num(Prop.SERVERPORT)),
        null, this);
    ports.addKeyListener(keys);
    portc = new BaseXTextField(Integer.toString(ctx.prop.num(Prop.PORT)), null,
        this);
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
    cs = new ClientSession(ctx.prop.get(Prop.HOST), ctx.prop.num(Prop.PORT), u,
        p);
    p2.setCs(cs);
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
    } else if(BUTTONCONNECT.equals(cmd)) {
      try {
        ctx.prop.set(Prop.PORT, Integer.parseInt(ports.getText()));
        ctx.prop.set(Prop.HOST, host.getText());
        ctx.prop.write();

        createSession(loguser.getText(), new String(logpass.getPassword()));
        p2.setData();
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
    } else if(connected) {
      p2.action(cmd);
    }

    stop.setEnabled(run);
    ports.setEnabled(!run);
    loguser.setEnabled(!connected);
    logpass.setEnabled(!connected);
    portc.setEnabled(!connected);
    host.setEnabled(!connected);
    boolean valh = host.getText().matches("^([A-Za-z]+://)?[A-Za-z0-9-.]+$");
    boolean valpl = ports.getText().matches("^[0-9]{2,5}$")
        && Integer.parseInt(ports.getText()) <= 65535;
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
}
