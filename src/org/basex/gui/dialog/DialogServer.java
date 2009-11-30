package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.net.BindException;

import javax.swing.Box;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.basex.BaseXServer;
import org.basex.core.Commands.CmdShow;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.proc.Exit;
import org.basex.core.proc.Show;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXTabs;
import org.basex.gui.layout.BaseXText;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.CachedOutput;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.server.ClientSession;
import org.basex.server.LoginException;
import org.basex.util.Token;

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
  private final DialogUser user = new DialogUser(true, this, gui);
  /** Sessions/Databases panel. */
  private final BaseXBack sedb = new BaseXBack();
  /** Log panel. */
  private final BaseXBack logs = new BaseXBack();
  /** Stop button. */
  private final BaseXButton stop;
  /** Start button. */
  private final BaseXButton start;
  /** Connect button. */
  private final BaseXButton connect;
  /** Disconnect button. */
  private final BaseXButton disconnect;
  /** Refresh button. */
  private final BaseXButton refresh;
  /** Shows log file. */
  private final BaseXButton show;
  /** Deletes log file. */
  private final BaseXButton delete;
  /** Deletes all log files. */
  private final BaseXButton deleteAll;
  /** Server host. */
  private final BaseXTextField host;
  /** Local server port. */
  private final BaseXTextField ports;
  /** Server port. */
  private final BaseXTextField portc;
  /** Log text. */
  final BaseXText logt;
  /** Change button. */
  private final BaseXTabs tabs;
  /** Username textfield. */
  private final BaseXTextField loguser;
  /** Password textfield. */
  private final JPasswordField logpass;
  /** Info label. */
  private final BaseXLabel info;
  /** Info label. */
  private final BaseXLabel info2;
  /** Combobox for log files. */
  final BaseXCombo logc;
  /** String for log dir. */
  private final String logdir = ctx.prop.get(Prop.DBPATH) + "/.logs/";

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
    tabs.add(SESSIONS + "/" + DATABASES, sedb);
    tabs.add("Logs", logs);

    start = new BaseXButton(BUTTONSTASERV, this);
    stop = new BaseXButton(BUTTONSTOSERV, this);
    connect = new BaseXButton(BUTTONCONNECT, this);
    disconnect = new BaseXButton(BUTTONDISCONNECT, this);
    refresh = new BaseXButton(BUTTONREFRESH, this);
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
    BaseXLayout.setWidth(logpass, BaseXTextField.TWIDTH);
    info = new BaseXLabel(" ");
    info.setBorder(8, 0, 0, 0);

    // Local server panel.
    final BaseXBack p1 = new BaseXBack();
    p1.setLayout(new TableLayout(2, 2, 8, 2));
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
    p2.setLayout(new TableLayout(5, 2, 8, 2));
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

    conn.add(new BaseXLabel(LOCALSERVER + COLS, false, true));
    conn.add(p1);
    conn.add(new BaseXLabel(LOGIN + COLS, false, true));
    conn.add(p2);
    conn.add(info);
    
    logc = new BaseXCombo(new String[] {}, this);
    logt = new BaseXText(false, this);
    logs.setLayout(new BorderLayout(2, 0));
    logs.setBorder(8, 8, 8, 8);
    final BaseXBack pl1 = new BaseXBack();
    show = new BaseXButton("Show", this);
    delete = new BaseXButton("Delete", this);
    deleteAll = new BaseXButton("Delete All", this);
    pl1.add(logc);
    pl1.add(show);
    pl1.add(delete);
    pl1.add(deleteAll);
    pl1.add(Box.createHorizontalGlue());
    logs.add(pl1, BorderLayout.NORTH);
    info2 = new BaseXLabel(" ");
    info2.setBorder(8, 0, 0, 0);
    logs.add(logt, BorderLayout.CENTER);
    logs.add(info2, BorderLayout.SOUTH);
    
    set(tabs, BorderLayout.CENTER);

    // test if server is running
    running = ping(true);
    
    // Register a change listener
    tabs.addChangeListener(new ChangeListener() {
        // This method is called whenever the selected tab changes
        public void stateChanged(final ChangeEvent evt) {
            BaseXTabs pane = (BaseXTabs) evt.getSource();
            if (pane.getSelectedIndex() == 3) {
              Object tmp = logc.getSelectedItem();
              int i = logc.getSelectedIndex();
              if(i != -1) {
              action("Show");
              }
              refreshLog();
              logc.setSelectedItem(tmp);
            }
        }
    });
    initLog();
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
    String msg = null;
    String msg2 = null;

    try {
      if(BUTTONSTASERV.equals(cmd)) {
        final int p = Integer.parseInt(ports.getText());
        ctx.prop.set(Prop.SERVERPORT, p);
        if(host.getText().equals("localhost")) {
          ctx.prop.set(Prop.PORT, p);
          portc.setText(ports.getText());
        }
        final String path = IOFile.file(getClass().getProtectionDomain().
            getCodeSource().getLocation().toString());
        final String mem = "-Xmx" + Runtime.getRuntime().maxMemory();
        final String clazz = org.basex.BaseXServer.class.getName();
        new ProcessBuilder(new String[] { "java", mem, "-cp", path, clazz,
            "-p", String.valueOf(p)}).start();
        running = ping(true);
      } else if(BUTTONSTOSERV.equals(cmd)) {
        BaseXServer.stop(gui.context);
        running = ping(true);
        connected = connected && ping(false);
      } else if(BUTTONCONNECT.equals(cmd)) {
        gui.prop.set(GUIProp.SERVERUSER, loguser.getText());
        gui.prop.set(GUIProp.SERVERPASS, new String(logpass.getPassword()));
        ctx.prop.set(Prop.HOST, host.getText());
        ctx.prop.set(Prop.PORT, Integer.parseInt(portc.getText()));
        cs = new ClientSession(ctx, gui.prop.get(GUIProp.SERVERUSER),
            gui.prop.get(GUIProp.SERVERPASS));
        user.setSess(cs);
        fillsedb();
        connected = true;
      } else if(BUTTONDISCONNECT.equals(cmd)) {
        cs.execute(new Exit());
        connected = false;
      } else if(BUTTONREFRESH.equals(cmd)) {
        fillsedb();
      } else if("Show".equals(cmd)) {
        final IO io = IO.get(logdir + logc.getSelectedItem().toString());
        logt.setText(io.content());
      } else if("Delete".equals(cmd)) {
        File f = new File(logdir + logc.getSelectedItem().toString());
        if(f.delete()) {
          initLog();
        } else {
          msg2 = "Delete not possible";
        }
      } else if("Delete All".equals(cmd)) {
        boolean d = false;
        for(int i = 0; i < logc.getItemCount(); i++) {
          File f = new File(logdir + logc.getItemAt(i).toString());
          d = f.delete();
        }
        if(!d) msg2 = "Delete of all logs not possible";
        initLog();
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

    boolean warn = true;
    if(msg != null || msg2 != null) {
      warn = false;
    } else if(!(valpl && valh && valp && vallu && vallp)) {
      msg = Main.info(INVALID, !valpl ? LOCALPORT : !valh ? HOST :
        !valp ? PORT : !vallu ? SERVERUSER : SERVERPW);
    }
    info.setError(msg, warn);
    info2.setError(msg2, warn);
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
    tabs.setEnabledAt(2, connected);
    show.setEnabled(logc.getSelectedIndex() != -1);
    delete.setEnabled(logc.getSelectedIndex() != -1);
    deleteAll.setEnabled(logc.getItemCount() > 0);
    ctx.prop.write();
  }

  /**
   * Fills sessions/databases panel.
   * @throws IOException Exception
   */
  private void fillsedb() throws IOException {
    sedb.removeAll();
    sedb.setLayout(new TableLayout(4, 1, 2, 2));
    sedb.setBorder(8, 8, 8, 8);
    final BaseXBack sess = new BaseXBack();
    CachedOutput out = new CachedOutput();
    sess.setLayout(new TableLayout(2, 1, 2, 2));
    sess.add(new BaseXLabel(SESSIONS + COLS, false, true));
    cs.execute(new Show(CmdShow.SESSIONS), out);
    BaseXText text = new BaseXText(false, this);
    text.setFont(getFont());
    text.setBorder(new EmptyBorder(5, 5, 5, 5));
    text.setText(out.finish());
    BaseXLayout.setWidth(text, 420);
    BaseXLayout.setHeight(text, 100);
    sess.add(text);
    sedb.add(sess);
    sedb.add(new BaseXLabel("  "));
    final BaseXBack dbs = new BaseXBack();
    dbs.setLayout(new TableLayout(2, 1, 2, 2));
    out = new CachedOutput();
    cs.execute(new Show(CmdShow.DATABASES), out);
    BaseXText text1 = new BaseXText(false, this);
    text1.setFont(getFont());
    text1.setBorder(new EmptyBorder(5, 5, 5, 5));
    text1.setText(out.finish());
    BaseXLayout.setWidth(text1, 420);
    BaseXLayout.setHeight(text1, 100);
    dbs.add(new BaseXLabel(DATABASES + COLS, false, true));
    dbs.add(text1);
    sedb.add(dbs);
    sedb.add(refresh);
  }
  
  /**
   * Inits the log panel.
   */
  void initLog() {
    refreshLog();
    logc.setSelectedIndex(-1);
    logt.setText(Token.EMPTY);
  }
  
  /**
   * Refreshs the log panel.
   */
  void refreshLog() {
    logc.removeAllItems();
    final File f = new File(logdir);
    final String[] files = f.list();
    if(files != null) {
      for(final String s : files) {
        if(s.endsWith(".log")) logc.addItem(s);
      }
    }
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
