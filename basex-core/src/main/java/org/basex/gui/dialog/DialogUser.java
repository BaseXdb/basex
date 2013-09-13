package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.io.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.List;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.*;
import org.basex.server.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Panel for displaying information about global/local users.
 *
 * @author BaseX Team 2005-12, BSD License
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
    BaseXLayout.setWidth(user, 100);
    create = new BaseXButton(CREATE, dia);
    pass = new BaseXPassword(dia);

    BaseXLayout.setWidth(pass, 100);
    alter = new BaseXButton(S_ALTER, dia);
    drop = new BaseXButton(DROP + DOTS, dia);
    info = new BaseXLabel(" ");

    add(new BaseXLabel(S_CREATEU + COLS, false, true));
    BaseXBack p = new BaseXBack(new TableLayout(1, 5, 8, 0)).border(0, 0, 6, 0);
    p.add(new BaseXLabel(USERNAME + COLS));
    p.add(user);
    p.add(new BaseXLabel(PASSWORD + COLS));
    p.add(pass);
    p.add(create);
    add(p);

    tablePanel = new BaseXBack(new BorderLayout(0, 5));

    databases = new BaseXCombo(dia);
    BaseXLayout.setWidth(databases, 210);
    addUser = new BaseXCombo(dia);
    add = new BaseXButton(ADD, dia);
    BaseXLayout.setWidth(addUser, 131);

    if(!global) {
      p = new BaseXBack(new TableLayout(2, 3, 8, 2));
      p.add(new BaseXLabel(DATABASES + COL, false, true));
      p.add(new BaseXLabel(ADD + COL, false, true));
      p.add(new BaseXLabel());
      p.add(databases);
      p.add(addUser);
      p.add(add);
      tablePanel.add(p, BorderLayout.NORTH);
    }

    p = new BaseXBack(new TableLayout(2, 2, 8, 5));
    p.add(new BaseXLabel(global ? S_GLOBPERM : S_LOCPERM, false, true));
    p.add(new BaseXLabel());

    table = new BaseXTable(users, dia);
    final JScrollPane sp = new JScrollPane(table);
    sp.setPreferredSize(new Dimension(350, 220));
    p.add(sp);

    final BaseXBack pp = new BaseXBack(new TableLayout(2, 1, 0, 5));
    if(global) pp.add(alter);
    pp.add(drop);
    p.add(pp);
    tablePanel.add(p, BorderLayout.CENTER);

    tablePanel.add(info, BorderLayout.SOUTH);
    add(tablePanel);
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
        final Perm perm = Perm.values()[(Integer) o[2] - (g ? 0 : 1)];
        final String uname = table.getModel().getValueAt((Integer) o[1], 0).toString();

        final boolean confirm = !g && uname.equals(dia.admuser.getText());
        if(confirm && !BaseXDialog.confirm(dia.gui, Util.info(S_DBREVOKE))) return;

        sess.execute(new Grant(perm, uname, db));
        msg = sess.info();
        if(confirm) {
          if(perm == Perm.ADMIN) {
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
      } else if(cmp == create) {
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
        if(BaseXDialog.confirm(dia.gui, Util.info(S_DRQUESTION, rows.length))) {
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
          final String perm = Perm.values()[c].toString();
          sess.execute(new Grant(perm, us, db));
          msg = sess.info();
          if(!ok) break;
        }
        setData();
        addUser.requestFocusInWindow();
      }
    } catch(final IOException ex) {
      msg = Util.message(ex);
      ok = false;
    }

    final boolean valname = Databases.validName(user.getText());
    final boolean valpass = new String(pass.getPassword()).matches("[^ ;'\"]*");
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
      msg = !newname ? Util.info(USER_EXISTS_X, user.getText()) : !valdrop ?
          ADMIN_STATIC_X : Util.info(INVALID_X, !valname ? USERNAME : PASSWORD);
      icon = Msg.WARN;
    }
    info.setText(msg, icon);
  }

  /**
   * Sets new data.
   * @throws IOException I/O exception
   */
  void setData() throws IOException {
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
   * @throws IOException I/O exception
   */
  private Table table(final String db) throws IOException {
    return new Table(sess.execute(new ShowUsers(db)));
  }

  /**
   * Sets session.
   * @param s session
   * @throws IOException I/O exception
   */
  public void setSess(final Session s) throws IOException {
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
  private static String numberof(final String w, final int n) {
    return "(" + n + ' ' + w + ')';
  }

  /**
   * Returns table panel.
   * @return BaseXBack
   */
  public BaseXBack getTablePanel() {
    return tablePanel;
  }
}
