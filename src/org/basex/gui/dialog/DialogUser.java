package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import org.basex.core.Main;
import org.basex.core.Proc;
import org.basex.core.Session;
import org.basex.core.Commands.CmdPerm;
import org.basex.core.proc.AlterUser;
import org.basex.core.proc.CreateUser;
import org.basex.core.proc.DropUser;
import org.basex.core.proc.Grant;
import org.basex.core.proc.List;
import org.basex.core.proc.Revoke;
import org.basex.core.proc.ShowUsers;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.CachedOutput;
import org.basex.server.LoginException;
import org.basex.util.StringList;
import org.basex.util.Table;
import org.basex.util.Token;
import org.basex.util.TokenList;

/**
 * Panel for displaying information about global/lokal users.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class DialogUser extends BaseXBack {
  /** ArrayList for table. */
  Table data = new Table();
  /** Session. */
  Session sess;

  /** List of permission processes. */
  final ArrayList<Proc> permps = new ArrayList<Proc>();
  /** List of temp global permissions. */
  final ArrayList<StringList> tempP = new ArrayList<StringList>();
  /** Change button. */
  final BaseXButton change;

  /** Alter button. */
  private final BaseXButton alter;
  /** Create button. */
  private final BaseXButton create;
  /** Delete button. */
  private final BaseXButton drop;
  /** Remove button. */
  private final BaseXButton remove;
  /** Add button. */
  private final BaseXButton add;
  /** Username textfield. */
  private final BaseXTextField user;
  /** Password textfield. */
  private final JPasswordField pass;
  /** Password textfield. */
  private final JPasswordField newpass;
  /** User columns. */
  private final BaseXCombo dropUser;
  /** User columns. */
  private final BaseXCombo alterUser;
  /** User columns. */
  final BaseXCombo removeUser;
  /** User columns. */
  final BaseXCombo addUser;
  /** Databases. */
  final BaseXCombo databases;
  /** User table. */
  final JTable table;
  /** Info label. */
  final BaseXLabel info;
  /** Flag global/local. */
  final boolean global;
  /** Dialog. */
  final Dialog dia;
  /** Table panel. */
  final BaseXBack tablePanel;

  /** Key listener. */
  private final KeyAdapter keys = new KeyAdapter() {
    @Override
    public void keyReleased(final KeyEvent e) {
      action(null);
    }
  };

  /**
   * Constructor.
   * @param g global/local flag
   * @param d dialog window
   */
  public DialogUser(final boolean g, final Dialog d) {
    global = g;
    dia = d;

    setLayout(new TableLayout(7, 1, 0, 4));
    setBorder(8, 8, 8, 8);

    user = new BaseXTextField("", dia);
    user.addKeyListener(keys);
    BaseXLayout.setWidth(user, 100);
    create = new BaseXButton(BUTTONCREATE, dia);
    pass = new JPasswordField();
    pass.addKeyListener(keys);
    BaseXLayout.setWidth(pass, 100);
    newpass = new JPasswordField();
    newpass.addKeyListener(keys);
    BaseXLayout.setWidth(newpass, 100);
    alter = new BaseXButton(BUTTONALTER, dia);
    alterUser = new BaseXCombo(new String[] {}, dia);
    BaseXLayout.setWidth(alterUser, BaseXCombo.DWIDTH);
    change = new BaseXButton(BUTTONCHANGE, dia);
    dropUser = new BaseXCombo(new String[] {}, dia);
    BaseXLayout.setWidth(dropUser, BaseXCombo.DWIDTH);
    drop = new BaseXButton(BUTTONDROP, dia);
    info = new BaseXLabel(" ");

    add(new BaseXLabel(CREATEU + COLS, false, true));
    BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(1, 5, 6, 0));
    p.setBorder(0, 0, 5, 0);
    p.add(new BaseXLabel(SERVERUSER + COLS));
    p.add(user);
    p.add(new BaseXLabel(SERVERPW + COLS));
    p.add(pass);
    p.add(create);
    add(p);

    add(new BaseXLabel(ALTERPW + COLS, false, true));
    p = new BaseXBack();
    p.setLayout(new TableLayout(1, 4, 6, 0));
    p.add(alterUser);
    p.add(new BaseXLabel(NEWPW));
    p.add(newpass);
    p.add(alter);
    add(p);

    add(new BaseXLabel(DROPU + COLS, false, true));
    p = new BaseXBack();
    p.setLayout(new TableLayout(1, 2, 6, 0));
    p.add(dropUser);
    p.add(drop);
    add(p);

    tablePanel = new BaseXBack();
    tablePanel.setLayout(new BorderLayout(0, 5));

    add = new BaseXButton(BUTTONADD, dia);
    databases = new BaseXCombo(true, new String[] {}, dia);
    addUser = new BaseXCombo(new String[] {}, dia);
    BaseXLayout.setWidth(addUser, BaseXCombo.DWIDTH);
    removeUser = new BaseXCombo(new String[] {}, dia);
    remove = new BaseXButton(BUTTONREMOVE, dia);
    BaseXLayout.setWidth(removeUser, BaseXCombo.DWIDTH);

    if(!global) {
      p = new BaseXBack();
      p.setLayout(new TableLayout(2, 5, 8, 2));
      p.add(new BaseXLabel(DATABASES + COL, false, true));
      p.add(new BaseXLabel(BUTTONADD + COL, false, true));
      p.add(new BaseXLabel(""));
      p.add(new BaseXLabel(BUTTONREMOVE + COL, false, true));
      p.add(new BaseXLabel(""));
      p.add(databases);
      p.add(addUser);
      p.add(add);
      p.add(removeUser);
      p.add(remove);
      tablePanel.add(p, BorderLayout.NORTH);
    }

    p = new BaseXBack();
    p.setLayout(new TableLayout(2, 1, 0, 5));
    p.add(new BaseXLabel(PERMS, false, true));
    table = new JTable(new TableModel());
    table.setCellSelectionEnabled(false);
    table.setFocusable(false);
    final JScrollPane sp = new JScrollPane(table);
    BaseXLayout.setHeight(sp, 130);
    p.add(sp);
    tablePanel.add(p, BorderLayout.CENTER);

    p = new BaseXBack();
    p.setLayout(new BorderLayout());
    p.add(change, BorderLayout.EAST);
    p.add(info, BorderLayout.WEST);
    tablePanel.add(p, BorderLayout.SOUTH);
    add(tablePanel);

    action(null);
  }

  /**
   * Reacts on user input.
   * @param cmp calling component
   */
  public void action(final Object cmp) {
    String msg = null;
    try {
      final Object di = databases.getSelectedItem();
      final String db = di == null ? null : di.toString();
      if(cmp == change) {
        for(final Proc p : permps) {
          if(!sess.execute(p)) {
            msg = sess.info();
            break;
          }
        }
        permps.clear();
        if(global) {
          setData();
        } else {
          setDataL();
        }
      } else if(cmp == create) {
        final String u = user.getText();
        final String p = new String(pass.getPassword());
        if(!sess.execute(new CreateUser(u, p))) msg = sess.info();
        user.setText("");
        pass.setText("");
        setData();
      } else if(cmp == drop) {
        final String u = dropUser.getSelectedItem().toString();
        if(Dialog.confirm(this, Main.info(DRQUESTION, u)) &&
          !sess.execute(new DropUser(u))) msg = sess.info();
        setData();
      } else if(cmp == alter) {
        final String u = alterUser.getSelectedItem().toString();
        final String p = new String(newpass.getPassword());
        if(!sess.execute(new AlterUser(u, p))) msg = sess.info();
        newpass.setText("");
        setData();
      } else if(cmp == remove) {
        final String u = removeUser.getSelectedItem().toString();
        if(!sess.execute(new DropUser(u, db))) msg = sess.info();
        setDataL();
      } else if(cmp == add) {
        final String value = addUser.getSelectedItem().toString();
        for(final StringList l : tempP) {
          if(l.get(0).equals(value)) {
            for(int i = 1; i <= 2; i++) {
              final String o = l.get(i);
              final Object val = o.equals("") ? Boolean.FALSE : o.equals("X") ?
                  Boolean.TRUE : o;
              final String right = CmdPerm.values()[i - 1].toString();
              permps.add(val.equals(true) ? new Grant(right, value, db) :
                new Revoke(right, value, db));
            }
          }
        }
        action(change);
      } else if(cmp == databases) {
        try {
          setDataL();
        } catch(final IOException e1) {
          addUser.removeAllItems();
          removeUser.removeAllItems();
          data = new Table();
          if(databases.getSelectedIndex() > 0) {
          msg = e1.getMessage();
          }
          ((TableModel) table.getModel()).fireTableChanged(null);
        }
      }
    } catch(final IOException ex) {
      Main.debug(ex);
      if(ex instanceof BindException) msg = SERVERBIND;
      else if(ex instanceof LoginException) msg = SERVERLOGIN;
      else msg = ex.getMessage(); // SERVERERR;
    }

    final boolean n = user.getText().matches("[\\w]*");
    final boolean p = new String(pass.getPassword()).matches("[\\w]*");
    final boolean np = new String(newpass.getPassword()).matches("[\\w]*");
    boolean na = !user.getText().equals(ADMIN);
    for(int i = 0; i < dropUser.getItemCount(); i++) {
      na &= !user.getText().equals(dropUser.getItemAt(i).toString());
    }

    boolean warn = true;
    if(msg != null) {
      warn = false;
    } else if(!(n && p && np && na)) {
      msg = !na ? Main.info(USERKNOWN, user.getText()) : Main.info(
          INVALID, !n ? SERVERUSER : SERVERPW);
    }
    info.setError(msg, warn);

    alter.setEnabled(np && newpass.getPassword().length != 0
        && alterUser.getSelectedIndex() > 0);
    create.setEnabled(n && p && na
        && !user.getText().isEmpty() && pass.getPassword().length != 0);
    drop.setEnabled(dropUser.getSelectedIndex() > 0);
    remove.setEnabled(removeUser.getSelectedIndex() > 0);
    add.setEnabled(addUser.getSelectedIndex() > 0);
    change.setEnabled(false);
  }

  /**
   * Sets new data.
   * @throws IOException I/O Exception
   */
  public void setData() throws IOException {
    final CachedOutput co = new CachedOutput();
    if(!sess.execute(new ShowUsers(), co)) throw new IOException(sess.info());

    data = new Table(co.toString());
    dropUser.removeAllItems();
    alterUser.removeAllItems();
    TokenList tmp = null;
    dropUser.addItem(numberof(USERS, data.contents.size() - 1));
    alterUser.addItem(numberof(USERS, data.contents.size()));
    for(final TokenList o : data.contents) {
      final String check = Token.string(o.get(0));
      if(!check.equals(ADMIN)) {
        dropUser.addItem(check);
      } else {
        tmp = o;
      }
      alterUser.addItem(check);
    }
    dropUser.setSelectedIndex(0);
    alterUser.setSelectedIndex(0);
    data.contents.remove(tmp);
    ((TableModel) table.getModel()).fireTableChanged(null);
  }

  /**
   * Sets local data.
   * @throws IOException I/O Exception
   */
  void setDataL() throws IOException {
    removeUser.removeAllItems();
    addUser.removeAllItems();
    tempP.clear();

    final int i = databases.getSelectedIndex();
    final String db = i > 0 ? databases.getSelectedItem().toString() : null;
    if(db == null) return;
    
    CachedOutput co = new CachedOutput();
    if(!sess.execute(new ShowUsers(db), co)) throw new IOException(sess.info());
    final String users = co.toString();
    co = new CachedOutput();
    if(!sess.execute(new ShowUsers(), co)) throw new IOException(sess.info());

    data = new Table(users);
    final Table data2 = new Table(co.toString());
    final StringList tmp1 = new StringList();
    for(final TokenList l : data.contents) tmp1.add(Token.string(l.get(0)));
    final StringList tmp2 = new StringList();
    for(final TokenList l : data2.contents) tmp2.add(Token.string(l.get(0)));
    final StringList tmp3 = new StringList();
    final StringList tmp4 = new StringList();
    for(final String s : tmp2) {
      if(s.equals(ADMIN)) continue;
      if(!tmp1.contains(s)) {
        tmp3.add(s);
        for(final TokenList l : data2.contents) {
          if(Token.string(l.get(0)).equals(s)) {
            final StringList tmp = new StringList();
            tmp.add(s);
            tmp.add(Token.string(l.get(1)));
            tmp.add(Token.string(l.get(2)));
            tempP.add(tmp);
          }
        }
      } else {
        tmp4.add(s);
      }
    }
    addUser.addItem(numberof(USERS, tmp3.size()));
    removeUser.addItem(numberof(USERS, tmp4.size()));
    for(final String s : tmp3) {
      addUser.addItem(s);
    }
    for(final String s : tmp4) {
      removeUser.addItem(s);
    }
    removeUser.setSelectedIndex(0);
    addUser.setSelectedIndex(0);
    ((TableModel) table.getModel()).fireTableChanged(null);
  }

  /**
   * Sets session.
   * @param s session
   * @throws IOException I/O Exception
   */
  public void setSess(final Session s) throws IOException {
    sess = s;
    if(global) {
      setData();
    } else {
      final CachedOutput out = new CachedOutput();
      sess.execute(new List(), out);
      final Table dbs = new Table(out.toString());
      databases.removeAllItems();
      databases.addItem(numberof(DATABASES, dbs.contents.size()));
      for(final TokenList l : dbs.contents) {
        databases.addItem(Token.string(l.get(0)));
      }
      databases.setSelectedIndex(0);
    }
  }
  
  /**
   * Returns the first entry of comboboxes.
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

  /**
   * Dialog specific table model.
   *
   * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
   * @author Andreas Weiler
   */
  final class TableModel extends AbstractTableModel {
    public int getColumnCount() {
      return data.header.size();
    }

    public int getRowCount() {
      return data.contents.size();
    }

    @Override
    public String getColumnName(final int col) {
      return Token.string(data.header.get(col));
    }

    public Object getValueAt(final int row, final int col) {
      final String o = Token.string(data.contents.get(row).get(col));
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
        final String uname = Token.string(data.contents.get(row).get(0));
        if(!uname.equals(" ")) {
          final String right = CmdPerm.values()[col - 1].toString();
          if(global) {
            permps.add(value.equals(true) ? new Grant(right, uname)
                : new Revoke(right, uname));
          } else {
            final String db = databases.getSelectedItem().toString();
            permps.add(value.equals(true) ? new Grant(right, uname, db)
                : new Revoke(right, uname, db));
          }
          data.contents.get(row).set(
              Token.token(value == Boolean.TRUE ? "X" : ""), col);
          fireTableCellUpdated(row, col);
          change.setEnabled(true);
      }
    }
  }
}