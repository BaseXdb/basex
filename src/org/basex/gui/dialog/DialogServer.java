package org.basex.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.basex.BaseXServer;
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
public final class DialogServer extends Dialog {

  /** Context. */
  Context ctx = gui.context;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogServer(final GUI main) {
    super(main, "Server...");
    // Server panel
    final BaseXBack p1 = new BaseXBack();
    p1.setLayout(new TableLayout(1, 3));
    final BaseXButton stop = new BaseXButton("Stop server", null, this);
    final BaseXButton start = new BaseXButton("Start server...", null, this);
    stop.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        new BaseXServer("stop");
        stop.setEnabled(false);
        start.setEnabled(true);
      }
    });
    start.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        startServer();
        stop.setEnabled(true);
        start.setEnabled(false);
      }
    });
    try {
      new ClientSession(ctx);
      start.setEnabled(false);
    } catch(final IOException e1) {
      stop.setEnabled(false);
    }
    p1.add(start);
    p1.add(new BaseXLabel("    "));
    p1.add(stop);

    // User main panel
    final BaseXBack p2 = new BaseXBack();
    p2.setLayout(new TableLayout(3, 1));
    p2.setBorder(8, 8, 8, 8);
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
    if(getUsers().length == 0) delete.setEnabled(false);
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

    // Properties panel
    final BaseXBack p3 = new BaseXBack();
    p3.setLayout(new TableLayout(7, 1));
    p3.setBorder(8, 8, 8, 8);
    final BaseXBack p31 = new BaseXBack();
    p31.setLayout(new TableLayout(4, 2, 6, 0));
    p31.add(new BaseXLabel("Port:"));
    final BaseXTextField port = new BaseXTextField(
        String.valueOf(ctx.prop.num(Prop.PORT)), null, this);
    p31.add(port);
    p31.add(new BaseXLabel("Host:"));
    final BaseXTextField host = new BaseXTextField(ctx.prop.get(Prop.HOST),
        null, this);
    p31.add(host);
    p31.add(new BaseXLabel(" "));
    p31.add(new BaseXLabel(" "));
    p31.add(new BaseXLabel("        "));
    final BaseXButton change = new BaseXButton("Change", null, this);
    change.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        ctx.prop.set(Prop.HOST, host.getText());
        ctx.prop.set(Prop.PORT, Integer.parseInt(port.getText()));
      }
    });
    p31.add(change);
    p3.add(p31);
    final BaseXTabs tabs = new BaseXTabs(this);
    tabs.add("Server", p1);
    tabs.add("Users", p2);
    tabs.add("Properties", p3);
    set(tabs, BorderLayout.CENTER);
    finish(null);
  }

  /**
   * Starts a server as new process.
   */
  void startServer() {
    try {
      final ProcessBuilder pb = new ProcessBuilder(
          "java", "-cp", Prop.WORK + "bin", "org.basex.BaseXServer");
      final Process p = pb.start();
      final BufferedReader in = new BufferedReader(new InputStreamReader(
          p.getInputStream()));
      new Thread() {
        @Override
        public void run() {
          String line = "";
          try {
            while((line = in.readLine()) != null) {
              System.out.println(line);
            }
          } catch(final IOException ex) {
            ex.printStackTrace();
          }
        }
      }.start();
    } catch(final IOException ex) {
      ex.printStackTrace();
    }
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
