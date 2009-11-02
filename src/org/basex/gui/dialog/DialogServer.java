package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.io.IOException;
import java.net.BindException;
import javax.swing.ImageIcon;
import javax.swing.JPasswordField;
import org.basex.BaseXServer;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.proc.Exit;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
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

  /** Server panel. */
  private final BaseXBack conn = new BaseXBack();
  /** User panel. */
  private final DialogUser user = new DialogUser(true, this);
  /** Stop button. */
  private final BaseXButton stop;
  /** Start button. */
  private final BaseXButton start;
  /** Connect button. */
  private final BaseXButton connect;
  /** Disconnect button. */
  private final BaseXButton disconnect;
  /** Server host. */
  private final BaseXTextField host;
  /** Local server port. */
  private final BaseXTextField ports;
  /** Server port. */
  private final BaseXTextField portc;
  /** Change button. */
  private final BaseXTabs tabs;
  /** Username textfield. */
  private final BaseXTextField loguser;
  /** Password textfield. */
  private final JPasswordField logpass;
  /** Info label. */
  private final BaseXLabel info;

  /** Boolean for check is server is running. */
  private boolean running;
  /** Boolean for check if client is connected. */
  private boolean connected;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogServer(final GUI main) {
    super(main, SRVTITLE);

    tabs = new BaseXTabs(this);

    // Server panel
    conn.setLayout(new TableLayout(5, 1, 0, 4));
    conn.setBorder(8, 8, 8, 8);

    tabs.add(SERVERN, conn);
    tabs.add(USERS, user);

    start = new BaseXButton(BUTTONSTASERV, this);
    stop = new BaseXButton(BUTTONSTOSERV, this);
    connect = new BaseXButton(BUTTONCONNECT, this);
    disconnect = new BaseXButton(BUTTONDISCONNECT, this);
    host = new BaseXTextField(ctx.prop.get(Prop.HOST), this);
    host.addKeyListener(keys);
    ports = new BaseXTextField(Integer.toString(ctx.prop.num(Prop.SERVERPORT)),
        this);
    ports.addKeyListener(keys);
    portc = new BaseXTextField(Integer.toString(ctx.prop.num(Prop.PORT)),
        this);
    portc.addKeyListener(keys);
    loguser = new BaseXTextField(gui.prop.get(GUIProp.SERVERUSER), this);
    loguser.addKeyListener(keys);
    logpass = new JPasswordField(gui.prop.get(GUIProp.SERVERPASS));
    logpass.addKeyListener(keys);
    BaseXLayout.setWidth(logpass, 200);
    info = new BaseXLabel(" ");
    info.setBorder(8, 0, 0, 0);

    // Local server panel.
    final BaseXBack p1 = new BaseXBack();
    p1.setLayout(new TableLayout(2, 2, 0, 2));
    p1.setBorder(8, 0, 8, 0);
    p1.add(new BaseXLabel(PORT + COLS));
    p1.add(ports);

    final BaseXBack p11 = new BaseXBack();
    p11.setLayout(new TableLayout(1, 2, 2, 2));
    p11.add(start);
    p11.add(stop);
    p1.add(new BaseXLabel(" "));
    p1.add(p11);

    // Login panel.
    final BaseXBack p2 = new BaseXBack();
    p2.setLayout(new TableLayout(5, 2, 2, 2));
    p2.add(new BaseXLabel(SERVERUSER + COLS));
    p2.add(loguser);
    p2.add(new BaseXLabel(SERVERPW + COLS));
    p2.add(logpass);
    p2.add(new BaseXLabel(HOST + COLS));
    p2.add(host);
    p2.add(new BaseXLabel(PORT + COLS));
    p2.add(portc);
    p2.add(new BaseXLabel(" "));
    final BaseXBack p21 = new BaseXBack();
    p21.setLayout(new TableLayout(1, 2, 2, 2));
    p21.add(connect);
    p21.add(disconnect);
    p2.add(p21);

    conn.add(new BaseXLabel(LOCAL + SERVERN + COLS, false, true));
    conn.add(p1);
    conn.add(new BaseXLabel(LOGIN + COLS, false, true));
    conn.add(p2);
    conn.add(info);
    set(tabs, BorderLayout.CENTER);

    // test if server is running
    running = ping(true);

    action(null);
    finish(null);
  }

  /**
   * Checks if a server is running, ping like.
   * @param local local/remote flag
   * @return boolean success
   */
  private boolean ping(final boolean local) {
    try {
      new ClientSession(local ? "localhost" : ctx.prop.get(Prop.HOST),
          ctx.prop.num(local ? Prop.SERVERPORT : Prop.PORT), "", "");
    } catch(final IOException e) {
      if(e instanceof LoginException) return true;
    }
    return false;
  }

  @Override
  public void action(final String cmd) {
    ImageIcon icon = null;
    String msg = "";

    try {
      if(BUTTONSTASERV.equals(cmd)) {
        final int p = Integer.parseInt(ports.getText());
        ctx.prop.set(Prop.PORT, p);
        ctx.prop.set(Prop.SERVERPORT, p);
        portc.setText(ports.getText());
        final String path = IOFile.file(getClass().getProtectionDomain().
            getCodeSource().getLocation().toString());
        final String mem = "-Xmx" + Runtime.getRuntime().maxMemory();
        final String clazz = org.basex.BaseXServer.class.getName();
        new ProcessBuilder(new String[] { "java", mem, "-cp", path, clazz,
            "-p", String.valueOf(p)}).start();
        running = ping(true);
      } else if(BUTTONSTOSERV.equals(cmd)) {
        new BaseXServer("stop");
        running = ping(true);
        connected = connected && ping(false);
      } else if(BUTTONCONNECT.equals(cmd)) {
        gui.prop.set(GUIProp.SERVERUSER, loguser.getText());
        gui.prop.set(GUIProp.SERVERPASS, new String(logpass.getPassword()));
        ctx.prop.set(Prop.HOST, host.getText());
        ctx.prop.set(Prop.PORT, Integer.parseInt(portc.getText()));
        cs = new ClientSession(ctx, gui.prop.get(GUIProp.SERVERUSER),
            gui.prop.get(GUIProp.SERVERPASS));
        user.setCs(cs);
        connected = true;
      } else if(BUTTONDISCONNECT.equals(cmd)) {
        cs.execute(new Exit());
        connected = false;
      } else if(connected) {
        user.action(cmd);
      }
    } catch(final IOException ex) {
      Main.debug(ex);
      if(ex instanceof BindException) msg = SERVERBIND;
      else if(ex instanceof LoginException) msg = SERVERLOGIN;
      else msg = ex.getMessage(); //SERVERERR;
    }

    final boolean valp = portc.getText().matches("[\\d]+") &&
      Integer.parseInt(portc.getText()) <= 65535;
    final boolean valpl = ports.getText().matches("[\\d]+") &&
      Integer.parseInt(ports.getText()) <= 65535;
    final boolean vallu = loguser.getText().matches("[\\w]*");
    final boolean vallp = new String(logpass.getPassword()).matches("[\\w]*");
    final boolean valh = host.getText().matches("([\\w]+://)?[\\w.-]+");

    if(!msg.isEmpty()) {
      icon = BaseXLayout.icon("error");
    } else if(!(valpl && valh && valp && vallu && vallp)) {
      icon = BaseXLayout.icon("warn");
      msg = (!valpl ? LOCAL + PORT : !valh ? HOST : !valp ? PORT :
        !vallu ? SERVERUSER : SERVERPW) + INVALID;
    }

    ports.setEnabled(!running);
    start.setEnabled(!running && valpl);
    stop.setEnabled(running);
    loguser.setEnabled(!connected);
    logpass.setEnabled(!connected);
    host.setEnabled(!connected);
    portc.setEnabled(!connected);
    connect.setEnabled(!connected && vallu && vallp && valh && valp &&
        !loguser.getText().isEmpty() && logpass.getPassword().length != 0);
    disconnect.setEnabled(connected);
    tabs.setEnabledAt(1, connected);
    info.setText(msg);
    info.setIcon(icon);
    ctx.prop.write();
  }

  @Override
  public void cancel() {
    try {
      if(connected) cs.execute(new Exit());
    } catch(final IOException ex) {
      Main.debug(ex);
    }
    super.cancel();
  }
}
