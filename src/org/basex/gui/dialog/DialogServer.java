package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.net.BindException;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.basex.BaseXServer;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.Commands.CmdPerm;
import org.basex.core.proc.Exit;
import org.basex.core.proc.ShowDatabases;
import org.basex.core.proc.ShowSessions;
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
import org.basex.util.Performance;
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

  /** Tabulators. */
  final BaseXTabs tabs;
  /** Server panel. */
  final BaseXBack conn = new BaseXBack();
  /** User panel. */
  final DialogUser user = new DialogUser(true, this);
  /** Databases panel. */
  final DialogUser dbsP = new DialogUser(false, this);
  /** Sessions/Databases panel. */
  final BaseXBack sess = new BaseXBack();
  /** Log panel. */
  final BaseXBack logs = new BaseXBack();
  /** Stop button. */
  private final BaseXButton stop;
  /** Start button. */
  private final BaseXButton start;
  /** Connect button. */
  private final BaseXButton connect;
  /** Disconnect button. */
  private final BaseXButton disconnect;
  /** Refresh button. */
  private final BaseXButton refreshSess;
  /** Updates log file. */
  final BaseXButton refreshLog;
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
  /** Current databases. */
  private final BaseXText sese;
  /** Current sessions. */
  private final BaseXText sedb;
  /** Log text. */
  private final BaseXText logt;
  /** Username textfield. */
  private final BaseXTextField loguser;
  /** Password textfield. */
  private final JPasswordField logpass;
  /** Info label. */
  private final BaseXLabel infoC;
  /** Info label. */
  private final BaseXLabel infoL;
  /** Combobox for log files. */
  private final BaseXCombo logc;
  /** String for log dir. */
  private final String logdir = ctx.prop.get(Prop.DBPATH) + "/.logs/";

  /** Boolean for check is server is running. */
  private boolean running;
  /** Boolean for check if client is connected. */
  private boolean connected;
  /** Int which tab is activated. */
  int tab;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogServer(final GUI main) {
    super(main, GUISERVER);

    final BaseXBack db = dbsP.getTablePanel();
    db.setBorder(8, 8, 8, 8);
    tabs = new BaseXTabs(this);
    tabs.add("Server", conn);
    tabs.add(USERS, user);
    tabs.add(DATABASES, db);
    tabs.add(SESSIONS, sess);
    tabs.add(LOGS, logs);

    // Server Tab
    conn.setLayout(new TableLayout(6, 1, 0, 4));
    conn.setBorder(8, 8, 8, 8);
    conn.add(new BaseXLabel(LOCALSERVER + COLS, false, true));

    start = new BaseXButton(BUTTONSTART, this);
    stop = new BaseXButton(BUTTONSTOP, this);
    connect = new BaseXButton(BUTTONCONNECT, this);
    disconnect = new BaseXButton(BUTTONDISCONNECT, this);
    host = new BaseXTextField(ctx.prop.get(Prop.HOST), this);
    host.addKeyListener(keys);
    ports = new BaseXTextField(Integer.toString(ctx.prop.num(Prop.SERVERPORT)),
        this);
    ports.addKeyListener(keys);
    portc = new BaseXTextField(Integer.toString(ctx.prop.num(Prop.PORT)), this);
    portc.addKeyListener(keys);
    loguser = new BaseXTextField(gui.prop.get(GUIProp.SERVERUSER), this);
    loguser.addKeyListener(keys);
    logpass = new JPasswordField(gui.prop.get(GUIProp.SERVERPASS));
    logpass.addKeyListener(keys);
    BaseXLayout.setWidth(logpass, BaseXTextField.TWIDTH);
    infoC = new BaseXLabel(" ");
    infoC.setBorder(8, 0, 0, 0);

    // Local server panel.
    BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(2, 2, 8, 2));
    p.setBorder(0, 0, 0, 0);
    p.add(new BaseXLabel(PORT + COLS));
    p.add(ports);
    p.add(new BaseXLabel(" "));
    BaseXBack pp = new BaseXBack();
    pp.setLayout(new TableLayout(1, 2, 2, 2));
    pp.add(start);
    pp.add(stop);
    p.add(pp);
    conn.add(p);

    conn.add(new BaseXLabel(" "));
    conn.add(new BaseXLabel("Administrator " + LOGIN + 
        " (local or remote)" + COLS, false, true));

    // Login panel.
    p = new BaseXBack();
    p.setLayout(new TableLayout(5, 2, 8, 2));
    p.add(new BaseXLabel(SERVERUSER + COLS));
    p.add(loguser);
    p.add(new BaseXLabel(SERVERPW + COLS));
    p.add(logpass);
    p.add(new BaseXLabel(HOST + COLS));
    p.add(host);
    p.add(new BaseXLabel(PORT + COLS));
    p.add(portc);
    p.add(new BaseXLabel(" "));
    pp = new BaseXBack();
    pp.setLayout(new TableLayout(1, 2, 2, 2));
    pp.add(connect);
    pp.add(disconnect);
    p.add(pp);
    conn.add(p);
    conn.add(infoC);

    // Session Tab
    sess.setLayout(new BorderLayout());
    /*
    conn.add(new BaseXLabel(LOCALSERVER + COLS, false, true));
    conn.add(p1);
    conn.add(new BaseXLabel(LOGIN + " on local or remote server"  +
        COLS, false, true));
    conn.add(p2);
    conn.add(info);

    // Session panel
    sess.setLayout(new TableLayout(4, 1, 2, 2));
    */
    sess.setBorder(8, 8, 8, 8);
    sese = new BaseXText(false, this);
    sese.setFont(getFont());
    sese.setBorder(new EmptyBorder(5, 5, 5, 5));
    sedb = new BaseXText(false, this);
    sedb.setFont(getFont());
    sedb.setBorder(new EmptyBorder(5, 5, 5, 5));
    refreshSess = new BaseXButton(BUTTONREFRESH, this);

    p = new BaseXBack();
    p.setLayout(new GridLayout(2, 1, 0, 2));

    pp = new BaseXBack();
    pp.setLayout(new BorderLayout());
    pp.add(new BaseXLabel(SESSIONS + COLS, false, true), BorderLayout.NORTH);
    pp.add(sese, BorderLayout.CENTER);
    p.add(pp);

    pp = new BaseXBack();
    pp.setLayout(new BorderLayout());
    pp.add(new BaseXLabel(DATABASES + COLS, false, true), BorderLayout.NORTH);
    pp.add(sedb, BorderLayout.CENTER);
    p.add(pp);
    sess.add(p, BorderLayout.CENTER);

    p = new BaseXBack();
    p.setLayout(new BorderLayout(0, 0));
    p.add(refreshSess, BorderLayout.EAST);
    sess.add(p, BorderLayout.SOUTH);

    
    // Logging Tab
    logs.setLayout(new BorderLayout());
    logs.setBorder(8, 8, 8, 8);
    delete = new BaseXButton(BUTTONDELETE, this);
    deleteAll = new BaseXButton(BUTTONDELALL, this);
    logc = new BaseXCombo(true, new String[] {}, this);
    logt = new BaseXText(false, this);
    logt.setFont(getFont());
    logt.setBorder(new EmptyBorder(5, 5, 5, 5));
    infoL = new BaseXLabel(" ");
    infoL.setBorder(8, 0, 0, 0);
    refreshLog = new BaseXButton(BUTTONREFRESH, this);

    p = new BaseXBack();
    p.setLayout(new BorderLayout());
    pp = new BaseXBack();
    pp.add(logc);
    pp.add(delete);
    pp.add(deleteAll);
    p.add(pp, BorderLayout.WEST);
    logs.add(p, BorderLayout.NORTH);
    logs.add(logt, BorderLayout.CENTER);

    p = new BaseXBack();
    p.setLayout(new BorderLayout(8, 0));
    p.add(infoL, BorderLayout.WEST);
    p.add(refreshLog, BorderLayout.EAST);
    logs.add(p, BorderLayout.SOUTH);

    set(tabs, BorderLayout.CENTER);

    // test if server is running
    running = ping(true);

    tabs.addChangeListener(new ChangeListener() {
      public void stateChanged(final ChangeEvent evt) {
        final BaseXTabs pane = (BaseXTabs) evt.getSource();
        tab = pane.getSelectedIndex();
        final Object o = pane.getSelectedComponent();
        if(o == logs) refreshLog();
      }
    });
    refreshLog();
    action(null);
    setResizable(true);
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
  public void action(final Object cmp) {
    String msg = null;
    String msg2 = null;
    String gmsg = null;
    try {
      if(cmp == start) {
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
        
        for(int c = 0; c < 3; c++) {
          running = ping(true);
          if(running) break;
          Performance.sleep(500);
        }
        if(!running) {
          msg = SERVERBIND;
        } else {
          gmsg = SRVST;
        }
      } else if(cmp == stop) {
        BaseXServer.stop(gui.context);
        running = ping(true);
        connected = connected && ping(false);
        if(!connected) gmsg = SERVERSTOPPED;
      } else if(cmp == connect) {
        gui.prop.set(GUIProp.SERVERUSER, loguser.getText());
        gui.prop.set(GUIProp.SERVERPASS, new String(logpass.getPassword()));
        ctx.prop.set(Prop.HOST, host.getText());
        ctx.prop.set(Prop.PORT, Integer.parseInt(portc.getText()));
        cs = new ClientSession(ctx, gui.prop.get(GUIProp.SERVERUSER),
            gui.prop.get(GUIProp.SERVERPASS));
        user.setSess(cs);
        dbsP.setSess(cs);
        connected = true;
        gmsg = Main.info(CONNECTED, host.getText(), portc.getText());
        refreshSess();
      } else if(cmp == disconnect) {
        cs.execute(new Exit());
        connected = false;
        logpass.setText("");
        gmsg = DISCONNECTED;
      } else if(cmp == refreshSess) {
        refreshSess();
      } else if(cmp == refreshLog) {
        byte[] cont = Token.EMPTY;
        if(logc.getSelectedIndex() != -1) {
          cont = IO.get(logdir + logc.getSelectedItem().toString()).content();
        }
        logt.setText(cont);
      } else if(cmp == delete) {
        final File f = new File(logdir + logc.getSelectedItem().toString());
        if(f.delete()) {
          logc.setSelectedIndex(-1);
          refreshLog();
        } else {
          msg2 = Main.info(DBNOTDELETED, f.getName());
        }
      } else if(cmp == deleteAll) {
        for(int i = 0; i < logc.getItemCount(); i++) {
          final File f = new File(logdir + logc.getItemAt(i).toString());
          if(!f.delete()) {
            msg2 = Main.info(DBNOTDELETED, f.getName());
            break;
          }
        }
        logc.setSelectedIndex(-1);
        refreshLog();
      } else if(cmp == logc) {
        action(refreshLog);
      } else if(connected) {
        if(tab == 1) user.action(cmp);
        if(tab == 2) dbsP.action(cmp);
      }
    } catch(final IOException ex) {
      Main.debug(ex);
      if(ex instanceof BindException) msg = SERVERBIND;
      else if(ex instanceof LoginException) msg = SERVERLOGIN;
      else {
        msg = ex.getMessage(); //SERVERERR;
        if(msg.equals(Main.info(PERMNO, CmdPerm.values()[3]))) {
          try {
            cs.execute(new Exit());
          } catch(IOException e) {
            e.printStackTrace();
          }
        }
      }
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
    infoC.setError(msg, warn);
    infoL.setError(msg2, warn);
    if(gmsg != null) {
      infoC.setText(gmsg);
      infoC.setIcon(BaseXLayout.icon("ok"));
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
    tabs.setEnabledAt(2, connected);
    tabs.setEnabledAt(3, connected);
    tabs.setEnabledAt(4, running || logc.getItemCount() > 0);
    refreshLog.setEnabled(logc.getSelectedIndex() != -1);
    delete.setEnabled(logc.getSelectedIndex() != -1);
    deleteAll.setEnabled(logc.getItemCount() > 0);
    if (loguser.hasFocus()) {
      logpass.setText("");
      connect.setEnabled(false);
    }
    ctx.prop.write();
  }

  /**
   * Fills sessions/databases panel.
   * @throws IOException Exception
   */
  void refreshSess() throws IOException {
    CachedOutput out = new CachedOutput();
    cs.execute(new ShowSessions(), out);
    sese.setText(out.finish());
    out = new CachedOutput();
    cs.execute(new ShowDatabases(), out);
    sedb.setText(out.finish());
  }

  /**
   * Refreshes the log panel.
   */
  void refreshLog() {
    logc.removeAllItems();
    final File f = new File(logdir);
    final String[] files = f.list();
    if(files != null) {
      for(final String s : files) if(s.endsWith(".log")) logc.addItem(s);
    }
    action(refreshLog);
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

  @Override
  public void close() {
    if(ok) super.close();
  }
}
