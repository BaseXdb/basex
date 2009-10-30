package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
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

import org.basex.core.BaseXException;
import org.basex.core.Main;
import org.basex.core.Process;
import org.basex.core.Session;
import org.basex.core.Commands.CmdPerm;
import org.basex.core.proc.AlterUser;
import org.basex.core.proc.CreateUser;
import org.basex.core.proc.DropUser;
import org.basex.core.proc.Grant;
import org.basex.core.proc.InfoUsers;
import org.basex.core.proc.Revoke;
import org.basex.core.proc.Show;
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
public class DialogUser extends BaseXBack {

  /** ArrayList for table. */
  Table data = new Table();
  /** Key listener. */
  final KeyAdapter keys = new KeyAdapter() {
    @Override
    public void keyReleased(final KeyEvent e) {
      action(null);
    }
  };

  /** Change button. */
  final BaseXButton change;
  /** Alter button. */
  final BaseXButton alter;
  /** Create button. */
  BaseXButton create;
  /** Delete button. */
  BaseXButton delete;
  /** Username textfield. */
  BaseXTextField user;
  /** Password textfield. */
  JPasswordField pass;
  /** Password textfield. */
  JPasswordField newpass;
  /** User columns. */
  BaseXCombo userco1;
  /** User columns. */
  BaseXCombo userco2;
  /** User table. */
  JTable table;
  /** Info label. */
  BaseXLabel infop2;
  /** List of permission processes. */
  ArrayList<Process> permps = new ArrayList<Process>();
  /** String for error messages. */
  String err2;
  /** Session. */
  Session cs;
  /** Flag global/lokal. */
  boolean global;

  /**
   * Constructor.
   * @param g global/lokal flag
   * @param d Dialog window
   */
  public DialogUser(final boolean g, final Dialog d) {
    global = g;
    setLayout(new TableLayout(13, 1, 0, 4));
    setBorder(8, 8, 8, 8);
    user = new BaseXTextField("", null, d);
    user.addKeyListener(keys);
    BaseXLayout.setWidth(user, 100);
    create = new BaseXButton(BUTTONCREATE, null, d);
    pass = new JPasswordField();
    pass.addKeyListener(keys);
    BaseXLayout.setWidth(pass, 100);
    userco1 = new BaseXCombo(new String[] {}, null, d);
    delete = new BaseXButton(BUTTONDROP, null, d);
    table = new JTable(new TableModel());
    table.setPreferredScrollableViewportSize(new Dimension(420, 100));
    add(new BaseXLabel(CREATEU + COLS, false, true));

    final BaseXBack p21 = new BaseXBack();
    p21.setLayout(new TableLayout(1, 5, 6, 0));
    p21.setBorder(0, 0, 5, 0);
    p21.add(new BaseXLabel(SERVERUSER));
    p21.add(user);
    p21.add(new BaseXLabel(SERVERPW));
    p21.add(pass);
    p21.add(create);
    add(p21);
    add(new BaseXLabel(ALTERPW + COLS, false, true));

    final BaseXBack p22 = new BaseXBack();
    p22.setLayout(new TableLayout(1, 4, 6, 0));
    p22.setBorder(0, 0, 5, 0);
    newpass = new JPasswordField();
    alter = new BaseXButton(BUTTONALTER, null, d);
    userco2 = new BaseXCombo(new String[] {}, null, d);
    newpass.addKeyListener(keys);
    BaseXLayout.setWidth(newpass, 100);
    p22.add(userco2);
    p22.add(new BaseXLabel(NEWPW));
    p22.add(newpass);
    p22.add(alter);
    add(p22);
    add(new BaseXLabel(PERMS, false, true));
    add(new JScrollPane(table));
    change = new BaseXButton(BUTTONCHANGE, null, d);

    final BaseXBack p23 = new BaseXBack();
    p23.setLayout(new BorderLayout());
    p23.add(new BaseXLabel(DROPU + COLS, false, true), BorderLayout.WEST);
    p23.add(change, BorderLayout.EAST);
    BaseXLayout.setWidth(p23, 420);
    add(p23);

    final BaseXBack p24 = new BaseXBack();
    p24.setLayout(new TableLayout(1, 2, 6, 0));
    p24.setBorder(0, 0, 5, 0);
    p24.add(userco1);
    p24.add(delete);
    add(p24);

    infop2 = new BaseXLabel(" ");
    add(infop2);
    add(Box.createVerticalStrut(16));
    action(null);
  }

  /**
   * Action.
   * @param cmd Command
   */
  public void action(final String cmd) {
    if(BUTTONCHANGE.equals(cmd)) {
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
        if(!cs.execute(new CreateUser(u, p))) {
          throw new BaseXException(cs.info());
        }
        user.setText("");
        pass.setText("");
        setData();
      } catch(final Exception ex) {
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
    }
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
    change.setEnabled(false);
  }

  /**
   * Sets new data.
   * @throws Exception Exception
   */
  public void setData() throws Exception {
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
   * @throws Exception Exception
   */
  void fillLists() throws Exception {
    final CachedOutput out = new CachedOutput();
    if(global) {
      if(!cs.execute(new Show("Users"), out)) {
        throw new BaseXException(cs.info());
      }
    } else {
      if(!cs.execute(new InfoUsers(), out)) {
        throw new BaseXException(cs.info());
      }
    }
    data = new Table(out.toString());
  }

  /**
   * Sets session.
   * @param s session
   */
  public void setCs(final Session s) {
    this.cs = s;
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
      change.setEnabled(true);
    }
  }
}
