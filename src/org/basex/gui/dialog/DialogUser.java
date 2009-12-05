package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import org.basex.core.Main;
import org.basex.core.Proc;
import org.basex.core.Session;
import org.basex.core.Commands.CmdPerm;
import org.basex.core.proc.AlterUser;
import org.basex.core.proc.Close;
import org.basex.core.proc.CreateUser;
import org.basex.core.proc.DropUser;
import org.basex.core.proc.Grant;
import org.basex.core.proc.InfoUsers;
import org.basex.core.proc.List;
import org.basex.core.proc.Open;
import org.basex.core.proc.Revoke;
import org.basex.core.proc.Show;
import org.basex.gui.GUI;
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

/**
 * Panel for displaying information about global/lokal users.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class DialogUser extends BaseXBack {
  /** Reference to main window. */
  public final GUI gui;
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
  private final BaseXCombo  alterUser;
  /** User columns. */
  private final BaseXCombo removeUser;
  /** User columns. */
  private final BaseXCombo addUser;
  /** Databases. */
  final BaseXCombo databases;
  /** User table. */
  final JTable table;
  /** Info label. */
  private final BaseXLabel info;
  /** Flag global/local. */
  final boolean global;
  /** Dialog. */
  final Dialog dia;
  /** Table panel. */
  BaseXBack tablePanel;

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
   * @param d Dialog window
   * @param gu Gui
   */
  public DialogUser(final boolean g, final Dialog d, final GUI gu) {
    global = g;
    dia = d;
    gui = gu;

    setLayout(new TableLayout(8, 1, 0, 4));
    setBorder(8, 8, 8, 8);

    user = new BaseXTextField("", dia);
    user.addKeyListener(keys);
    BaseXLayout.setWidth(user, 100);
    create = new BaseXButton(BUTTONCREATE, dia);
    pass = new JPasswordField();
    pass.addKeyListener(keys);
    BaseXLayout.setWidth(pass, 100);
    table = new JTable(new TableModel());
    table.setPreferredScrollableViewportSize(new Dimension(420, 100));
    newpass = new JPasswordField();
    newpass.addKeyListener(keys);
    BaseXLayout.setWidth(newpass, 100);
    alter = new BaseXButton(BUTTONALTER, dia);
    alterUser = new BaseXCombo(new String[] {}, dia);
    change = new BaseXButton(BUTTONCHANGE, dia);
    dropUser = new BaseXCombo(new String[] {}, dia);
    drop = new BaseXButton(BUTTONDROP, dia);
    info = new BaseXLabel(" ");

    add(new BaseXLabel(CREATEU + COLS, false, true));

    final BaseXBack p1 = new BaseXBack();
    p1.setLayout(new TableLayout(1, 5, 6, 0));
    p1.setBorder(0, 0, 5, 0);
    p1.add(new BaseXLabel(SERVERUSER + COLS));
    p1.add(user);
    p1.add(new BaseXLabel(SERVERPW + COLS));
    p1.add(pass);
    p1.add(create);
    add(p1);
    add(new BaseXLabel(ALTERPW + COLS, false, true));

    final BaseXBack p2 = new BaseXBack();
    p2.setLayout(new TableLayout(1, 4, 6, 0));
    p2.add(alterUser);
    p2.add(new BaseXLabel(NEWPW));
    p2.add(newpass);
    p2.add(alter);
    add(p2);

    add(new BaseXLabel(DROPU + COLS, false, true));
    final BaseXBack p4 = new BaseXBack();
    p4.setLayout(new TableLayout(1, 2, 6, 0));
    p4.add(dropUser);
    p4.add(drop);
    add(p4);
    tablePanel = new BaseXBack();
    tablePanel.setLayout(new TableLayout(7, 1, 2, 2));
    add = new BaseXButton("Add", dia);
    addUser = new BaseXCombo(new String[] {}, dia);
    removeUser = new BaseXCombo(new String[] {}, dia);
    remove = new BaseXButton("Remove", dia);
    databases = new BaseXCombo(new String[] {}, dia);
    if(!global) {
      final BaseXBack tmp = new BaseXBack();
      tmp.setLayout(new TableLayout(2, 4, 2, 2));
      tmp.add(new BaseXLabel("Databases: ", false, true));
      tmp.add(Box.createHorizontalStrut(80));
      tmp.add(new BaseXLabel("Add: ", false, true));
      tmp.add(new BaseXLabel("Remove: ", false, true));
      final BaseXBack dPanel = new BaseXBack();
      dPanel.add(databases);
      tmp.add(dPanel);
      tmp.add(Box.createHorizontalStrut(20));
      final BaseXBack addPanel = new BaseXBack();
      addPanel.add(addUser);
      addPanel.add(add);
      tmp.add(addPanel);
      final BaseXBack removePanel = new BaseXBack();
      removePanel.add(removeUser);
      removePanel.add(remove);
      tmp.add(removePanel);
      tablePanel.add(tmp);
    }
    tablePanel.add(new BaseXLabel(PERMS, false, true));
    tablePanel.add(new JScrollPane(table));
    final BaseXBack tablePanel1 = new BaseXBack();
    tablePanel1.setLayout(new BorderLayout());
    tablePanel1.add(change, BorderLayout.EAST);
    tablePanel1.add(info, BorderLayout.WEST);
    BaseXLayout.setWidth(tablePanel1, 420);
    tablePanel.add(tablePanel1);
    add(tablePanel);
    add(Box.createVerticalStrut(20));
    action(null);
  }

  /**
   * Action.
   * @param cmd Command
   */
  public void action(final String cmd) {
    String msg = null;

    try {
      if(BUTTONCHANGE.equals(cmd)) {
        for(final Proc p : permps)
          if(!sess.execute(p)) msg = sess.info();
        permps.clear();
        if(global) {
          setData();
        } else {
          setDataL();
        }
      } else if(BUTTONCREATE.equals(cmd)) {
        final String u = user.getText();
        final String p = new String(pass.getPassword());
        if(!sess.execute(new CreateUser(u, p))) msg = sess.info();
        user.setText("");
        pass.setText("");
        setData();
      } else if(BUTTONDROP.equals(cmd)) {
        final String u = dropUser.getSelectedItem().toString();
        if(Dialog.confirm(this, Main.info(DRQUESTION, u))) {
          if(!sess.execute(new DropUser(u))) msg = sess.info();
        }
        setData();
      } else if(BUTTONALTER.equals(cmd)) {
        final String u = alterUser.getSelectedItem().toString();
        final String p = new String(newpass.getPassword());
        if(!sess.execute(new AlterUser(u, p))) msg = sess.info();
        newpass.setText("");
        setData();
      } else if("Remove".equals(cmd)) {
        // [AW] send remove to server...
        setDataL();
      } else if("Add".equals(cmd)) {
        String value = addUser.getSelectedItem().toString();
        for(StringList l : tempP) {
          if(l.get(0).equals(value)) {
            String db = databases.getSelectedItem().toString();
            String o = l.get(1);
            Object val = o.equals("") ? Boolean.FALSE : o.equals("X") ?
                Boolean.TRUE : o;
            String right = CmdPerm.values()[0].toString();
            permps.add(val.equals(true) ? new Grant(right, value, db) :
              new Revoke(right, value, db));
            o = l.get(2);
            val = o.equals("") ? Boolean.FALSE : o.equals("X") ?
                Boolean.TRUE : o;
            right = CmdPerm.values()[1].toString();
            permps.add(val.equals(true) ? new Grant(right, value, db) :
              new Revoke(right, value, db));
          }
        }
        action(BUTTONCHANGE);
      }
    } catch(final IOException ex) {
      Main.debug(ex);
      if(ex instanceof BindException) msg = SERVERBIND;
      else if(ex instanceof LoginException) msg = SERVERLOGIN;
      else msg = ex.getMessage(); // SERVERERR;
    }

    final boolean valuname = user.getText().matches("[\\w]*");
    final boolean valpass = new String(pass.getPassword()).matches("[\\w]*");
    final boolean valnewpass = new String(newpass.
        getPassword()).matches("[\\w]*");
    boolean disname = !user.getText().equals(ADMIN);
    for(int i = 0; i < dropUser.getItemCount(); i++) {
      disname &= !user.getText().equals(dropUser.getItemAt(i).toString());
    }

    boolean warn = true;
    if(msg != null) {
      warn = false;
    } else if(!(valuname && valpass && valnewpass && disname)) {
      msg = !disname ? Main.info(USERKNOWN, user.getText()) : Main.info(
          INVALID, !valuname ? SERVERUSER : SERVERPW);
    }
    info.setError(msg, warn);

    alter.setEnabled(valnewpass && newpass.getPassword().length != 0
        && alterUser.getSelectedIndex() != -1);
    create.setEnabled(valuname && valpass && disname
        && !user.getText().isEmpty() && pass.getPassword().length != 0);
    drop.setEnabled(dropUser.getSelectedIndex() != -1);
    remove.setEnabled(removeUser.getSelectedIndex() != -1);
    add.setEnabled(addUser.getSelectedIndex() != -1);
    change.setEnabled(false);
  }

  /**
   * Sets new data.
   * @throws IOException I/O Exception
   */
  public void setData() throws IOException {
    CachedOutput out = new CachedOutput();
    if(!sess.execute(new Show("Users"), out)) {
      throw new IOException(sess.info());
    }
    data = new Table(out.toString());
    dropUser.removeAllItems();
    alterUser.removeAllItems();
    StringList tmp = new StringList();
    for(final StringList o : data.contents) {
      final String check = o.get(0);
      if(!check.equals(ADMIN)) {
        dropUser.addItem(check);
        alterUser.addItem(check);
      } else {
        alterUser.addItem(check);
        tmp = o;
      }
    }
    dropUser.setSelectedIndex(-1);
    alterUser.setSelectedIndex(-1);
    data.contents.remove(tmp);
    ((TableModel) table.getModel()).fireTableChanged(null);
  }
  
  /**
   * Sets local data.
   * @throws IOException I/O Exception
   */
  void setDataL() throws IOException {
    CachedOutput out = new CachedOutput();
    if(!sess.execute(new InfoUsers(), out)) {
      throw new IOException(sess.info());
    }
    data = new Table(out.toString());
    removeUser.removeAllItems();
    addUser.removeAllItems();
    tempP.clear();
    out = new CachedOutput();
    sess.execute(new Show("Users"), out);
    Table data2 = new Table(out.toString());
    StringList tmp1 = new StringList();
    for(StringList l : data.contents) {
      tmp1.add(l.get(0));
    }
    StringList tmp2 = new StringList();
    for(StringList l : data2.contents) {
      tmp2.add(l.get(0));
    }
    for(String s : tmp2) {
      if(!tmp1.contains(s) && !s.equals(ADMIN)) {
        addUser.addItem(s);
        for(StringList l : data2.contents) {
          if(l.get(0).equals(s)) {
            StringList tmp = new StringList();
            tmp.add(s);
            tmp.add(l.get(1));
            tmp.add(l.get(2));
            tempP.add(tmp);
          }
        }
      } else if(!s.equals(ADMIN)) {
        removeUser.addItem(s);
      }
    }
    removeUser.setSelectedIndex(-1);
    addUser.setSelectedIndex(-1);
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
      CachedOutput out = new CachedOutput();
      sess.execute(new List(), out);
      Table dbs = new Table(out.toString());
      databases.removeAllItems();
      for(StringList l : dbs.contents) {
        databases.addItem(l.get(0));
      }
      databases.setSelectedIndex(-1);
      databases.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(final ItemEvent e) {
          if(e.getStateChange() == ItemEvent.SELECTED) {
            try {
              sess.execute(new Close());
              sess.execute(new Open(databases.getSelectedItem().toString()));
              setDataL();
            } catch(IOException e1) {
              e1.printStackTrace();
            }
          }
        }
      });
    }
  }

  /**
   * Returns tablepanel.
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
        if(!uname.equals(" ")) {
          final String right = CmdPerm.values()[col - 1].toString();
          if(global) {
            permps.add(value.equals(true) ? new Grant(right, uname)
                : new Revoke(right, uname));
          } else {
            String db = databases.getSelectedItem().toString();
            permps.add(value.equals(true) ? new Grant(right, uname, db)
                : new Revoke(right, uname, db));
          }
          data.contents.get(row).set(value == Boolean.TRUE ? "X" : "", col);
          fireTableCellUpdated(row, col);
          change.setEnabled(true);
      }
    }
  }
}