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
public final class DialogUser extends BaseXBack {
  /** ArrayList for table. */
  Table data = new Table();
  /** Session. */
  Session cs;

  /** List of permission processes. */
  final ArrayList<Process> permps = new ArrayList<Process>();
  /** Change button. */
  final BaseXButton change;

  /** Alter button. */
  private final BaseXButton alter;
  /** Create button. */
  private final BaseXButton create;
  /** Delete button. */
  private final BaseXButton drop;
  /** Username textfield. */
  private final BaseXTextField user;
  /** Password textfield. */
  private final JPasswordField pass;
  /** Password textfield. */
  private final JPasswordField newpass;
  /** User columns. */
  private final BaseXCombo userco1;
  /** User columns. */
  private final BaseXCombo userco2;
  /** User table. */
  private final JTable table;
  /** Info label. */
  private final BaseXLabel info;
  /** Flag global/local. */
  private final boolean global;

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
   */
  public DialogUser(final boolean g, final Dialog d) {
    global = g;

    setLayout(new TableLayout(11, 1, 0, 4));
    setBorder(8, 8, 8, 8);

    user = new BaseXTextField("", d);
    user.addKeyListener(keys);
    BaseXLayout.setWidth(user, 100);
    create = new BaseXButton(BUTTONCREATE, d);
    pass = new JPasswordField();
    pass.addKeyListener(keys);
    BaseXLayout.setWidth(pass, 100);
    table = new JTable(new TableModel());
    table.setPreferredScrollableViewportSize(new Dimension(420, 100));
    newpass = new JPasswordField();
    newpass.addKeyListener(keys);
    BaseXLayout.setWidth(newpass, 100);
    alter = new BaseXButton(BUTTONALTER, d);
    userco2 = new BaseXCombo(new String[] {}, d);
    change = new BaseXButton(BUTTONCHANGE, d);
    userco1 = new BaseXCombo(new String[] {}, d);
    drop = new BaseXButton(BUTTONDROP, d);
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
    p2.add(userco2);
    p2.add(new BaseXLabel(NEWPW));
    p2.add(newpass);
    p2.add(alter);
    add(p2);
    add(new BaseXLabel(PERMS, false, true));
    add(new JScrollPane(table));

    final BaseXBack p3 = new BaseXBack();
    p3.setLayout(new BorderLayout());
    p3.add(new BaseXLabel(DROPU + COLS, false, true), BorderLayout.WEST);
    p3.add(change, BorderLayout.EAST);
    BaseXLayout.setWidth(p3, 420);
    add(p3);

    final BaseXBack p4 = new BaseXBack();
    p4.setLayout(new TableLayout(1, 2, 6, 0));
    p4.setBorder(0, 0, 5, 0);
    p4.add(userco1);
    p4.add(drop);
    add(p4);

    add(info);
    add(Box.createVerticalStrut(38));
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
        for(final Process p : permps) if(!cs.execute(p)) msg = cs.info();
        permps.clear();
        setData();
      } else if(BUTTONCREATE.equals(cmd)) {
        final String u = user.getText();
        final String p = new String(pass.getPassword());
        if(!cs.execute(new CreateUser(u, p))) msg = cs.info();
        user.setText("");
        pass.setText("");
        setData();
      } else if(BUTTONDROP.equals(cmd)) {
        final String u = userco1.getSelectedItem().toString();
        if(Dialog.confirm(this, Main.info(DRQUESTION, u))) {
          if(!cs.execute(new DropUser(u))) msg = cs.info();
        }
        setData();
      } else if(BUTTONALTER.equals(cmd)) {
        final String u = userco2.getSelectedItem().toString();
        final String p = new String(newpass.getPassword());
        if(!cs.execute(new AlterUser(u, p))) msg = cs.info();
        newpass.setText("");
      }
    } catch(final IOException ex) {
      Main.debug(ex);
      if(ex instanceof BindException) msg = SERVERBIND;
      else if(ex instanceof LoginException) msg = SERVERLOGIN;
      else msg = ex.getMessage(); //SERVERERR;
    }

    final boolean valuname = user.getText().matches("[\\w]*");
    final boolean valpass = new String(pass.getPassword()).matches("[\\w]*");
    final boolean valnewpass = new String(
        newpass.getPassword()).matches("[\\w]*");
    boolean disname = !user.getText().equals(ADMIN);
    for(int i = 0; i < userco1.getItemCount(); i++) {
      disname &= !user.getText().equals(userco1.getItemAt(i).toString());
    }

    boolean warn = true;
    if(msg != null) {
      warn = false;
    } else if(!(valuname && valpass && valnewpass && disname)) {
      msg = !disname ? Main.info(USERKNOWN, user.getText()) :
        Main.info(INVALID, !valuname ? SERVERUSER : SERVERPW);
    }
    info.setError(msg, warn);

    alter.setEnabled(valnewpass && newpass.getPassword().length != 0 &&
        userco2.getSelectedIndex() != -1);
    create.setEnabled(valuname && valpass && disname &&
        !user.getText().isEmpty() && pass.getPassword().length != 0);
    drop.setEnabled(userco1.getSelectedIndex() != -1);
    change.setEnabled(false);
  }

  /**
   * Sets new data.
   * @throws IOException I/O Exception
   */
  public void setData() throws IOException {
    final CachedOutput out = new CachedOutput();
    if(!cs.execute(global ? new Show("Users") : new InfoUsers(), out)) {
      throw new IOException(cs.info());
    }
    data = new Table(out.toString());
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
    userco1.setSelectedIndex(-1);
    userco2.setSelectedIndex(-1);
    data.contents.remove(tmp);
    ((TableModel) table.getModel()).fireTableChanged(null);
  }

  /**
   * Sets session.
   * @param s session
   * @throws IOException I/O Exception
   */
  public void setCs(final Session s) throws IOException {
    cs = s;
    setData();
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
