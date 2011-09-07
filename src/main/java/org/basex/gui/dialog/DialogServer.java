package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.basex.BaseXServer;
import org.basex.core.MainProp;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Commands.CmdPerm;
import org.basex.core.cmd.Exit;
import org.basex.core.cmd.ShowDatabases;
import org.basex.core.cmd.ShowSessions;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXPassword;
import org.basex.gui.layout.BaseXTabs;
import org.basex.gui.layout.BaseXEditor;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.IOFile;
import org.basex.server.ClientSession;
import org.basex.util.Token;
import org.basex.util.Util;
import org.basex.util.list.StringList;

/**
 * Dialog window for displaying information about the server.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Andreas Weiler
 */
public final class DialogServer extends Dialog {
  /** Password textfield. */
  public final BaseXPassword logpass;

  /** Tabulators. */
  final BaseXTabs tabs;
  /** User panel. */
  final DialogUser user = new DialogUser(true, this);
  /** Databases panel. */
  final DialogUser dbsP = new DialogUser(false, this);
  /** Databases panel. */
  final BaseXBack databases = dbsP.getTablePanel();
  /** Sessions/Databases panel. */
  final BaseXBack sess = new BaseXBack();
  /** Log panel. */
  final BaseXBack logs = new BaseXBack();
  /** Username textfield. */
  final BaseXTextField loguser;
  /** Disconnect button. */
  final BaseXButton disconnect;
  /** Refresh button. */
  final BaseXButton refreshSess;
  /** Indicates which tab is activated. */
  int tab;

  /** Context. */
  private final Context ctx = gui.context;
  /** Updates log file. */
  private final BaseXButton refreshLog;
  /** Server panel. */
  private final BaseXBack conn = new BaseXBack();
  /** Stop button. */
  private final BaseXButton stop;
  /** Start button. */
  private final BaseXButton start;
  /** Connect button. */
  private final BaseXButton connect;
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
  private final BaseXEditor sese;
  /** Current sessions. */
  private final BaseXEditor sedb;
  /** Log text. */
  private final BaseXEditor logt;
  /** Info label. */
  private final BaseXLabel infoC;
  /** Info label. */
  private final BaseXLabel infoL;
  /** Combobox for log files. */
  private final BaseXCombo logc;
  /** String for log dir. */
  private final File logdir = ctx.mprop.dbpath(".logs");
  /** ClientSession. */
  private ClientSession cs;
  /** Boolean for check is server is running. */
  private boolean running;
  /** Boolean for check if client is connected. */
  private boolean connected;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogServer(final GUI main) {
    super(main, GUISERVER);
    databases.border(8);
    tabs = new BaseXTabs(this);
    tabs.add(CONNECT, conn);
    tabs.add(USERS, user);
    tabs.add(DATABASES, databases);
    tabs.add(SESSIONS, sess);
    tabs.add(LOCALLOGS, logs);

    // server tab
    conn.border(8).layout(new BorderLayout(0, 32));

    start = new BaseXButton(BUTTONSTART, this);
    stop = new BaseXButton(BUTTONSTOP, this);
    connect = new BaseXButton(BUTTONCONNECT, this);
    disconnect = new BaseXButton(BUTTONDISCONNECT, this);
    BaseXButton.setMnemonics(start, stop, connect, disconnect);

    host = new BaseXTextField(ctx.mprop.get(MainProp.HOST), this);
    host.addKeyListener(keys);
    ports = new BaseXTextField(Integer.toString(
        ctx.mprop.num(MainProp.SERVERPORT)), this);
    ports.addKeyListener(keys);
    portc = new BaseXTextField(Integer.toString(
        ctx.mprop.num(MainProp.PORT)), this);
    portc.addKeyListener(keys);
    loguser = new BaseXTextField(gui.gprop.get(GUIProp.SERVERUSER), this);
    loguser.addKeyListener(keys);
    logpass = new BaseXPassword(main);
    logpass.addKeyListener(keys);
    infoC = new BaseXLabel(" ").border(8, 0, 0, 0);

    BaseXBack p = new BaseXBack(new TableLayout(6, 1, 0, 0));

    // local server panel
    p.add(new BaseXLabel(LOCALSERVER + COLS, true, true));

    BaseXBack pp = new BaseXBack(new TableLayout(2, 2, 8, 4)).border(
        0, 0, 0, 0);
    pp.add(new BaseXLabel(PORT + COLS));
    pp.add(ports);
    pp.add(new BaseXLabel());
    BaseXBack ppp = new BaseXBack(new TableLayout(1, 2, 5, 0));
    ppp.add(start);
    ppp.add(stop);
    pp.add(ppp);
    p.add(pp);

    p.add(new BaseXLabel());
    p.add(new BaseXLabel(ADMINLOGIN + COLS, true, true).border(8, 0, 4, 0));

    // login panel
    pp = new BaseXBack(new TableLayout(5, 2, 8, 4));
    pp.add(new BaseXLabel(SERVERUSER + COLS));
    pp.add(loguser);
    pp.add(new BaseXLabel(SERVERPW + COLS));
    pp.add(logpass);
    pp.add(new BaseXLabel(HOST + COLS));
    pp.add(host);
    pp.add(new BaseXLabel(PORT + COLS));
    pp.add(portc);
    pp.add(new BaseXLabel());
    ppp = new BaseXBack(new TableLayout(1, 2, 5, 0));
    ppp.add(connect);
    ppp.add(disconnect);
    pp.add(ppp);
    p.add(pp);
    p.add(infoC);

    conn.add(p, BorderLayout.CENTER);

    p = new BaseXBack(new TableLayout(2, 1));
    BaseXLabel l = new BaseXLabel(SERVERINFO1);
    l.setForeground(GUIConstants.DGRAY);
    p.add(l);
    l = new BaseXLabel(SERVERINFO2);
    l.setForeground(GUIConstants.DGRAY);
    p.add(l);
    conn.add(p, BorderLayout.SOUTH);

    // session tab
    sess.border(8).layout(new BorderLayout());
    sese = new BaseXEditor(false, this);
    sese.setFont(start.getFont());
    sedb = new BaseXEditor(false, this);
    sedb.setFont(start.getFont());
    refreshSess = new BaseXButton(BUTTONREFRESH, this);
    refreshSess.setMnemonic();

    p = new BaseXBack(new GridLayout(2, 1, 0, 2));

    pp = new BaseXBack(new BorderLayout());
    pp.add(new BaseXLabel(SESSIONS + COLS, false, true), BorderLayout.NORTH);
    pp.add(sese, BorderLayout.CENTER);
    p.add(pp);

    pp = new BaseXBack(new BorderLayout());
    pp.add(new BaseXLabel(DATABASES + COLS, false, true), BorderLayout.NORTH);
    pp.add(sedb, BorderLayout.CENTER);
    p.add(pp);
    sess.add(p, BorderLayout.CENTER);

    p = new BaseXBack(new BorderLayout(0, 0));
    p.add(refreshSess, BorderLayout.EAST);
    sess.add(p, BorderLayout.SOUTH);

    // logging tab
    logs.border(8).layout(new BorderLayout());
    delete = new BaseXButton(BUTTONDELETE, this);
    deleteAll = new BaseXButton(BUTTONDELALL, this);
    BaseXButton.setMnemonics(delete, deleteAll);

    logc = new BaseXCombo(this);
    logt = new BaseXEditor(false, this);
    logt.setFont(start.getFont());
    BaseXLayout.setHeight(logt, 100);

    logt.border(5, 5, 5, 5);
    infoL = new BaseXLabel(" ").border(8, 0, 0, 0);
    refreshLog = new BaseXButton(BUTTONREFRESH, this);
    refreshLog.setMnemonic();

    p = new BaseXBack(new BorderLayout());
    pp = new BaseXBack();
    pp.add(logc);
    pp.add(delete);
    pp.add(deleteAll);
    p.add(pp, BorderLayout.WEST);
    logs.add(p, BorderLayout.NORTH);
    logs.add(logt, BorderLayout.CENTER);

    p = new BaseXBack(new BorderLayout(8, 0));
    p.add(infoL, BorderLayout.WEST);
    p.add(refreshLog, BorderLayout.EAST);
    logs.add(p, BorderLayout.SOUTH);

    set(tabs, BorderLayout.CENTER);

    // test if server is running
    running = ping(true);

    tabs.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(final ChangeEvent evt) {
        final BaseXTabs pane = (BaseXTabs) evt.getSource();
        tab = pane.getSelectedIndex();
        final Object o = pane.getSelectedComponent();
        if(o == logs) refreshLog();
        if(o == user) action(user);
        if(o == databases) action(dbsP);
        if(o == sess) action(refreshSess);
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
    return BaseXServer.ping(local ? LOCALHOST : ctx.mprop.get(MainProp.HOST),
        ctx.mprop.num(local ? MainProp.SERVERPORT : MainProp.PORT));
  }

  @Override
  public void action(final Object cmp) {
    Msg icon = Msg.SUCCESS;
    String msg = null;
    String msg2 = null;

    try {
      if(cmp == start || cmp == ports) {
        final int p = Integer.parseInt(ports.getText());
        gui.setMain(MainProp.SERVERPORT, p);
        if(host.getText().equals(LOCALHOST)) {
          gui.setMain(MainProp.PORT, p);
          portc.setText(ports.getText());
        }
        msg = BaseXServer.start(p, BaseXServer.class);
        if(msg.equals(SERVERSTART)) {
          running = true;
        } else {
          icon = Msg.ERROR;
        }
      } else if(cmp == stop) {
        if(running) BaseXServer.stop(ctx.mprop.num(MainProp.SERVERPORT),
            ctx.mprop.num(MainProp.EVENTPORT));
        running = ping(true);
        connected = connected && ping(false);
        if(!connected) msg = SERVERSTOPPED;
        if(host.getText().equals(LOCALHOST)) logpass.setText("");
        if(!connected) super.setTitle(GUISERVER);
      } else if(cmp == connect || cmp == loguser || cmp == logpass ||
          cmp == host || cmp == portc) {
        gui.gprop.set(GUIProp.SERVERUSER, loguser.getText());
        final String pw = new String(logpass.getPassword());
        gui.setMain(MainProp.HOST, host.getText());
        gui.setMain(MainProp.PORT, Integer.parseInt(portc.getText()));
        cs = new ClientSession(ctx, gui.gprop.get(GUIProp.SERVERUSER), pw);
        user.setSess(cs);
        dbsP.setSess(cs);
        connected = true;
        super.setTitle(GUISERVER + LI + loguser.getText() + "@" + host.getText()
            + COL + portc.getText());
        msg = Util.info(CONNECTED, host.getText(), portc.getText());
        refreshSess();
      } else if(cmp == disconnect) {
        cs.execute(new Exit());
        connected = false;
        logpass.setText("");
        super.setTitle(GUISERVER);
        msg = DISCONNECTED;
      } else if(cmp == refreshSess) {
        refreshSess();
      } else if(cmp == refreshLog || cmp == logc) {
        byte[] cont = Token.EMPTY;
        if(logc.getSelectedIndex() != -1) {
          final File f = new File(logdir, logc.getSelectedItem().toString());
          cont = new IOFile(f).read();
        }
        logt.setText(cont);
        logt.scrollToEnd();
      } else if(cmp == delete) {
        final File f = new File(logdir, logc.getSelectedItem().toString());
        if(f.delete()) {
          logc.setSelectedIndex(-1);
          refreshLog();
        } else {
          msg2 = Util.info(DBNOTDELETED, f.getName());
          icon = Msg.ERROR;
        }
      } else if(cmp == deleteAll) {
        File file = null;
        for(int i = 0; i < logc.getItemCount(); ++i) {
          final File f = new File(logdir, logc.getItemAt(i).toString());
          if(!f.delete()) file = f;
        }
        if(file != null) {
          msg2 = Util.info(DBNOTDELETED, file.getName());
          icon = Msg.ERROR;
        }
        logc.setSelectedIndex(-1);
        refreshLog();
      } else if(connected) {
        if(tab == 1) user.action(cmp);
        if(tab == 2) dbsP.action(cmp);
      }
    } catch(final Exception ex) {
      icon = Msg.ERROR;
      msg = Util.message(ex);
      if(msg.equals(Util.info(PERMNO, CmdPerm.values()[4]))) {
        try {
          cs.execute(new Exit());
        } catch(final BaseXException exx) {
          Util.stack(exx);
        }
      }
    }

    final boolean valp = portc.getText().matches("[\\d]+") &&
      Integer.parseInt(portc.getText()) <= 65535;
    final boolean valpl = ports.getText().matches("[\\d]+") &&
      Integer.parseInt(ports.getText()) <= 65535;
    final boolean vallu = loguser.getText().matches("[\\w]*");
    final boolean vallp = new String(
        logpass.getPassword()).matches("[^ ;'\\\"]*");
    final boolean valh = host.getText().matches("([\\w]+://)?[\\w.-]+");

    if(msg == null && msg2 == null &&
        !(valpl && valh && valp && vallu && vallp)) {
      msg = Util.info(INVALID, !valpl ? LOCALPORT : !valh ? HOST :
        !valp ? PORT : !vallu ? SERVERUSER : SERVERPW);
      icon = Msg.WARN;
    }
    infoC.setText(msg, icon);
    infoL.setText(msg2, icon);

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
    if(loguser.hasFocus()) {
      logpass.setText("");
      connect.setEnabled(false);
    }
    ctx.mprop.write();
  }

  /**
   * Fills sessions/databases panel.
   * @throws BaseXException Exception
   */
  private void refreshSess() throws BaseXException {
    sese.setText(Token.token(cs.execute(new ShowSessions())));
    sedb.setText(Token.token(cs.execute(new ShowDatabases())));
  }

  /**
   * Refreshes the log panel.
   */
  void refreshLog() {
    logc.removeAllItems();
    final String[] files = logdir.list();
    final StringList sl = new StringList();
    if(files != null) {
      for(final String s : files) if(s.endsWith(".log")) sl.add(s);
    }
    sl.sort(false, false);
    for(final String s : sl) logc.addItem(s);
    action(refreshLog);
  }

  @Override
  public void cancel() {
    try {
      if(connected) cs.execute(new Exit());
    } catch(final BaseXException ex) {
      Util.debug(ex);
    }
    super.cancel();
  }

  @Override
  public void close() {
    if(ok) super.close();
  }
}
