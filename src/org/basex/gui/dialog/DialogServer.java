package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.gui.GUI;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTabs;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.server.ClientSession;

/**
 * Dialog window for displaying information about the server.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public class DialogServer extends Dialog {

  /** Context. */
  Context ctx = gui.context;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogServer(final GUI main) {
    super(main, "Server...");
    final BaseXTabs tabs = new BaseXTabs(this);
    // Server panel
    final BaseXBack p1 = new BaseXBack();
    p1.setLayout(new TableLayout(3, 1));
    // User management panel
    final BaseXBack p2 = new BaseXBack();
    p2.setLayout(new TableLayout(3, 1));
    p2.setBorder(8, 8, 8, 8);
    tabs.add("Server...", p1);
    tabs.add("Users...", p2);
    // Start-stop server panel
    final BaseXBack p11 = new BaseXBack();
    p11.setLayout(new TableLayout(1, 3));
    final BaseXButton start = new BaseXButton("Start server...", null, this);
    final BaseXButton stop = new BaseXButton("Stop server", null, this);
    final BaseXTextField host = new BaseXTextField(ctx.prop.get(Prop.HOST),
        null, this);
    final BaseXTextField port = new BaseXTextField(
        String.valueOf(ctx.prop.num(Prop.PORT)), null, this);
    final BaseXButton change = new BaseXButton("Change", null, this);
    try {
      new ClientSession(ctx, ADMIN, ADMIN);
      start.setEnabled(false);
      host.setEnabled(false);
      port.setEnabled(false);
      change.setEnabled(false);
    } catch(final IOException e1) {
      stop.setEnabled(false);
      tabs.setEnabledAt(1, false);
    }
    start.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        stop.setEnabled(true);
        start.setEnabled(false);
        start.setEnabled(false);
        host.setEnabled(false);
        port.setEnabled(false);
        change.setEnabled(false);
        tabs.setEnabledAt(1, true);
      }
    });
    stop.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        stop.setEnabled(false);
        start.setEnabled(true);
        host.setEnabled(true);
        port.setEnabled(true);
        change.setEnabled(true);
        tabs.setEnabledAt(1, false);
      }
    });
    p11.add(start);
    p11.add(new BaseXLabel("        "));
    p11.add(stop);

    // Server preferences panel.
    final BaseXBack p12 = new BaseXBack();
    p12.setLayout(new TableLayout(3, 2));
    p12.add(new BaseXLabel("Port:"));
    p12.add(port);
    p12.add(new BaseXLabel("Host:"));
    p12.add(host);
    p12.add(new BaseXLabel("        "));
    change.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        ctx.prop.set(Prop.HOST, host.getText());
        int p = ctx.prop.num(Prop.PORT);
        try {
          p = Integer.parseInt(port.getText());
          ctx.prop.set(Prop.PORT, p);
        } catch(NumberFormatException n) {
          port.setText(String.valueOf(p));
        }
      }
    });
    p12.add(change);
    p12.setBorder(8, 8, 8, 8);
    p11.setBorder(8, 8, 8, 8);

    // Top panel
    final BaseXBack p21 = new BaseXBack();
    p21.setLayout(new TableLayout(4, 5, 6, 0));
    p21.add(new BaseXLabel("Create User:", false, true));
    p21.add(new BaseXLabel(""));
    p21.add(new BaseXLabel(""));
    p21.add(new BaseXLabel(""));
    p21.add(new BaseXLabel(""));
    p21.add(new BaseXLabel("Username:"));
    final BaseXTextField user = new BaseXTextField("", null, this);
    p21.add(user);
    p21.add(new BaseXLabel("Password:"));
    final JPasswordField pass = new JPasswordField(10);
    final JTable table = new JTable(new TableModel());
    // Create the scroll pane and add the table to it.
    final JScrollPane scrollPane = new JScrollPane(table);
    final BaseXCombo userco = new BaseXCombo(getUsers(), null, this);
    p21.add(pass);
    // create
    final BaseXButton create = new BaseXButton("Create User", null, this);
    // delete
    final BaseXButton delete = new BaseXButton("Drop User", null, this);
    create.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final String u = user.getText();
        final String p = new String(pass.getPassword());
        if(!u.equals("") && !p.equals("")) {
          ctx.users.create(u, p);
          user.setText("");
          pass.setText("");
          ((TableModel) table.getModel()).setData();
          userco.addItem(u);
          delete.setEnabled(true);
        }
      }
    });
    p21.add(create);
    final BaseXBack p22 = new BaseXBack();
    p22.setLayout(new TableLayout(2, 2, 6, 0));
    p22.add(new BaseXLabel("Drop User:", false, true));
    p22.add(new BaseXLabel(""));
    // if(getUsers().length == 0) delete.setEnabled(false);
    delete.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final String test = (String) userco.getSelectedItem();
        ctx.users.drop(test);
        userco.removeItem(test);
        ((TableModel) table.getModel()).setData();
        if(getUsers().length == 0) delete.setEnabled(false);
      }
    });

    p22.add(userco);
    p22.add(delete);
    final BaseXBack p23 = new BaseXBack();
    p23.setLayout(new TableLayout(5, 1, 6, 0));
    p23.add(new BaseXLabel("                      "));
    p23.add(new BaseXLabel("User rights management:", false, true));
    // create JTable
    table.setPreferredScrollableViewportSize(new Dimension(500, 70));
    table.setFillsViewportHeight(true);
    p23.add(scrollPane);
    final BaseXButton save = new BaseXButton("Save", null, this);
    save.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        ((TableModel) table.getModel()).setData();
      }
    });
    p23.add(save);
    p2.add(p21);
    p2.add(p22);
    p2.add(p23);
    // adding to main panel
    p1.add(p11);
    p1.add(p12);
    set(tabs, BorderLayout.CENTER);
    finish(null);
  }

  /**
   * Returns all users from the list.
   * @return users
   */
  String[] getUsers() {
    final ArrayList<Object[]> list = ctx.users.getUsers();
    if(list != null) {
      final String[] t = new String[list.size()];
      int i = 0;
      for(final Object[] s : list) {
        t[i] = (String) s[0];
        i++;
      }
      return t;
    }
    return new String[] {};
  }

  /**
   * Class of own table model.
   * 
   * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
   * @author Andreas Weiler
   */
  class TableModel extends AbstractTableModel {
    /** Column names. */
    String[] columnNames = { "Username", "Read", "Write", "Create", "Admin"};
    /** Data. */
    private ArrayList<Object[]> data = ctx.users.getUsers();

    public int getColumnCount() {
      return columnNames.length;
    }

    public int getRowCount() {
      return data.size();
    }

    @Override
    public String getColumnName(final int col) {
      return columnNames[col];
    }

    public Object getValueAt(final int row, final int col) {
      return data.get(row)[col];
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class getColumnClass(final int c) {
      return getValueAt(0, c).getClass();
    }

    @Override
    public boolean isCellEditable(final int row, final int col) {
      return col != 0;
    }

    @Override
    public void setValueAt(final Object value, final int row, final int col) {
      data.get(row)[col] = value;
      fireTableCellUpdated(row, col);
    }

    /**
     * Sets new data.
     */
    public void setData() {
      data = ctx.users.getUsers();
      fireTableDataChanged();
    }

    /**
     * Returns the data after updating.
     * @return object array
     */
    public ArrayList<Object[]> getData() {
      return data;
    }
  }
}
