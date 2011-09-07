package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import javax.swing.JScrollPane;
import org.basex.core.BaseXException;
import org.basex.core.Commands.CmdPerm;
import org.basex.core.cmd.AlterUser;
import org.basex.core.cmd.CreateUser;
import org.basex.core.cmd.DropUser;
import org.basex.core.cmd.Grant;
import org.basex.core.cmd.List;
import org.basex.core.cmd.ShowUsers;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXPassword;
import org.basex.gui.layout.BaseXTable;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.server.Session;
import org.basex.util.Table;
import org.basex.util.Token;
import org.basex.util.Util;
import org.basex.util.list.StringList;
import org.basex.util.list.TokenList;

/**
 * Panel for displaying information about global/local users.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Andreas Weiler
 */
final class DialogUser extends BaseXBack {
  /** Session. */
  private Session sess;
  /** Global user table. */
  private Table users = new Table();

  /** Create button. */
  private final BaseXButton create;
  /** Alter button. */
  private final BaseXButton alter;
  /** Delete button. */
  private final BaseXButton drop;
  /** Add button. */
  private final BaseXButton add;
  /** Username textfield. */
  private final BaseXTextField user;
  /** Password textfield. */
  private final BaseXPassword pass;
  /** User columns. */
  private final BaseXCombo addUser;
  /** Databases. */
  private final BaseXCombo databases;
  /** User table. */
  private final BaseXTable table;
  /** Info label. */
  private final BaseXLabel info;
  /** Flag global/local. */
  private final boolean global;
  /** Dialog. */
  private final DialogServer dia;
  /** Table panel. */
  private final BaseXBack tablePanel;

  /**
   * Constructor.
   * @param g global/local flag
   * @param d dialog window
   */
  DialogUser(final boolean g, final DialogServer d) {
    global = g;
    dia = d;

    layout(new TableLayout(7, 1, 0, 4)).border(8);

    user = new BaseXTextField("", dia);
    user.addKeyListener(dia.keys);
    BaseXLayout.setWidth(user, 100);
    create = new BaseXButton(BUTTONCREATE, dia);
    pass = new BaseXPassword(dia.gui);
    pass.addKeyListener(dia.keys);
    BaseXLayout.setWidth(pass, 100);
    alter = new BaseXButton(BUTTONALTER, dia);
    drop = new BaseXButton(BUTTONDROP, dia);
    info = new BaseXLabel(" ");

    add(new BaseXLabel(CREATEU + COLS, false, true));
    BaseXBack p = new BaseXBack(new TableLayout(1, 5, 8, 0)).border(0, 0, 5, 0);
    p.add(new BaseXLabel(SERVERUSER + COLS));
    p.add(user);
    p.add(new BaseXLabel(SERVERPW + COLS));
    p.add(pass);
    p.add(create);
    add(p);

    tablePanel = new BaseXBack(new BorderLayout(0, 5));

    databases = new BaseXCombo(dia);
    BaseXLayout.setWidth(databases, 210);
    addUser = new BaseXCombo(dia);
    add = new BaseXButton(BUTTONADD, dia);
    BaseXLayout.setWidth(addUser, 131);

    if(!global) {
      p = new BaseXBack(new TableLayout(2, 3, 8, 2));
      p.add(new BaseXLabel(DATABASES + COL, false, true));
      p.add(new BaseXLabel(BUTTONADD + COL, false, true));
      p.add(new BaseXLabel());
      p.add(databases);
      p.add(addUser);
      p.add(add);
      tablePanel.add(p, BorderLayout.NORTH);
    }

    p = new BaseXBack(new TableLayout(2, 2, 8, 5));
    p.add(new BaseXLabel(global ? GLOBPERM : LOCPERM, false, true));
    p.add(new BaseXLabel());

    table = new BaseXTable(users, dia);
    final JScrollPane sp = new JScrollPane(table);
    BaseXLayout.setHeight(sp, 220);
    BaseXLayout.setWidth(sp, 350);
    p.add(sp);

    final BaseXBack pp = new BaseXBack(new TableLayout(2, 1, 0, 5));
    if(global) pp.add(alter);
    pp.add(drop);
    p.add(pp);
    tablePanel.add(p, BorderLayout.CENTER);

    tablePanel.add(info, BorderLayout.SOUTH);
    add(tablePanel);

    BaseXButton.setMnemonics(global ? new BaseXButton[] { create, alter, drop }
      : new BaseXButton[] { add, drop });

    action(null);
  }

  /**
   * Reacts on user input.
   * @param cmp calling component
   */
  void action(final Object cmp) {
    boolean ok = true;
    String msg = null;

    try {
      final Object di = databases.getSelectedItem();
      final String db = di == null ? null : di.toString();

      if(cmp instanceof Object[]) {
        final Object[] o = (Object[]) cmp;
        final boolean g = o[0] == Boolean.TRUE;
        final CmdPerm perm = CmdPerm.values()[(Integer) o[2] - (g ? 0 : 1)];
        final String uname = table.getModel().getValueAt(
            (Integer) o[1], 0).toString();

        final boolean confirm = !g && uname.equals(dia.loguser.getText());
        if(confirm && !Dialog.confirm(dia.gui, Util.info(DBREVOKE))) return;

        sess.execute(new Grant(perm, uname, db));
        msg = sess.info();
        if(confirm) {
          if(perm == CmdPerm.ADMIN) {
            dia.tabs.setSelectedIndex(0);
            dia.action(dia.disconnect);
          } else {
            setSess(sess);
          }
        } else {
          setData();
        }
      } else if(cmp == this) {
        setSess(sess);
      } else if(cmp == databases) {
        setData();
      } else if(cmp == create || cmp == user || cmp == pass) {
        final String u = user.getText();
        final String p = Token.md5(new String(pass.getPassword()));
        sess.execute(new CreateUser(u, p));
        msg = sess.info();
        setData();
        user.setText("");
        pass.setText("");
        user.requestFocusInWindow();
      } else if(cmp == drop) {
        String msg2 = "";
        final int[] rows = table.getSelectedRows();
        if(Dialog.confirm(dia.gui, Util.info(DRQUESTION, rows.length))) {
          for(final int r : rows) {
            sess.execute(new DropUser(table.data.value(r, 0), db));
            if(msg == null) msg = sess.info();
            else if(msg2.isEmpty()) msg2 = " (...)";
            if(!ok) break;
          }
          msg += msg2;
          setData();
        }
      } else if(cmp == alter) {
        final DialogPass dp = new DialogPass(dia.gui);
        if(dp.ok()) {
          sess.execute(new AlterUser(table.getValueAt(
              table.getSelectedRow(), 0).toString(), Token.md5(dp.pass())));
          msg = sess.info();
        }
      } else if(cmp == add) {
        final String us = addUser.getSelectedItem().toString();
        for(int r = 0; r < users.contents.size(); ++r) {
          if(!users.value(r, 0).equals(us)) continue;
          int c = 3;
          while(--c >= 0 && users.value(r, c).isEmpty());
          final String perm = CmdPerm.values()[c].toString();
          sess.execute(new Grant(perm, us, db));
          msg = sess.info();
          if(!ok) break;
        }
        setData();
        addUser.requestFocusInWindow();
      }
    } catch(final BaseXException ex) {
      msg = Util.message(ex);
      ok = false;
    }

    final boolean valname = user.getText().matches("[\\w]*");
    final boolean valpass = new String(
        pass.getPassword()).matches("[^ ;'\\\"]*");
    boolean newname = true;
    for(int r = 0; r < users.contents.size(); ++r)
      newname &= !users.value(r, 0).equals(user.getText());

    alter.setEnabled(table.getSelectedRows().length == 1);
    create.setEnabled(valname && valpass && newname &&
        !user.getText().isEmpty() && pass.getPassword().length != 0);
    add.setEnabled(addUser.getSelectedIndex() > 0);
    addUser.setEnabled(addUser.getSelectedIndex() > -1);
    boolean valdrop = true;
    for(final int r : table.getSelectedRows()) {
      valdrop &= !table.data.value(r, 0).equals(ADMIN);
    }
    drop.setEnabled(valdrop && table.getSelectedRows().length > 0);
    valdrop |= table.getSelectedRows().length == 1;

    Msg icon = ok ? Msg.SUCCESS : Msg.ERROR;
    if(msg == null && !(valname && valpass && newname && valdrop)) {
      msg = !newname ? Util.info(USERKNOWN, user.getText()) : !valdrop ?
          USERADMIN : Util.info(INVALID, !valname ? SERVERUSER : SERVERPW);
      icon = Msg.WARN;
    }
    info.setText(msg, icon);
  }

  /**
   * Sets new data.
   * @throws BaseXException database exception
   */
  public void setData() throws BaseXException {
    users = table(null);

    if(global) {
      table.update(users);
    } else {
      addUser.removeAllItems();

      final int i = databases.getSelectedIndex();
      if(i == 0) table.update(new Table());
      if(i <= 0) return;

      final Table data = table(databases.getSelectedItem().toString());
      table.update(data);

      final StringList added = new StringList();
      for(final TokenList l : data.contents) added.add(Token.string(l.get(0)));

      final StringList adding = new StringList();
      for(final TokenList l : users.contents) {
        final String s = Token.string(l.get(0));
        if(!s.equals(ADMIN) && !added.contains(s)) adding.add(s);
      }
      addUser.addItem(numberof(USERS, adding.size()));
      for(final String s : adding) addUser.addItem(s);

      addUser.setSelectedIndex(0);
    }
  }

  /**
   * Returns a global or local user table.
   * @param db database (optional)
   * @return table
   * @throws BaseXException database exception
   */
  private Table table(final String db) throws BaseXException {
    return new Table(sess.execute(new ShowUsers(db)));
  }

  /**
   * Sets session.
   * @param s session
   * @throws BaseXException database exception
   */
  public void setSess(final Session s) throws BaseXException {
    sess = s;
    if(!global) {
      final Table dbs = new Table(sess.execute(new List()));
      databases.removeAllItems();
      databases.addItem(numberof(DATABASES, dbs.contents.size()));
      for(final TokenList l : dbs.contents) {
        databases.addItem(Token.string(l.get(0)));
      }
      databases.setSelectedIndex(0);
    }
    setData();
  }

  /**
   * Returns the first entry of combo boxes.
   * @param w what
   * @param n number
   * @return String
   */
  private String numberof(final String w, final int n) {
    return "(" + n + " " + w + ")";
  }

  /**
   * Returns table panel.
   * @return BaseXBack
   */
  public BaseXBack getTablePanel() {
    return tablePanel;
  }
}
