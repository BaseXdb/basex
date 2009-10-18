package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.proc.CreateUser;
import org.basex.core.proc.DropUser;
import org.basex.core.proc.Grant;
import org.basex.core.proc.IntStop;
import org.basex.core.proc.Revoke;
import org.basex.core.proc.Show;
import org.basex.gui.GUI;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTabs;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.CachedOutput;
import org.basex.server.ClientSession;

/**
 * Dialog window for displaying information about the server.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class DialogServer extends Dialog {
  /** ArrayList for table. */
  ArrayList<Object[]> data = new ArrayList<Object[]>();
  /** Context. */
  final Context ctx = gui.context;
  /** ClientSession. */
  ClientSession cs;
  /** Vector for combobox. */
  Vector<String> usernames = new Vector<String>();
  /** Server panel. */
  BaseXBack p1 = new BaseXBack();
  /** User panel. */
  BaseXBack p2 = new BaseXBack();

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogServer(final GUI main) {
    super(main, "Server...");
    final BaseXTabs tabs = new BaseXTabs(this);
    tabs.setPreferredSize(new Dimension(600, 300));
    // Server panel
    p1.setLayout(new TableLayout(3, 1));
    // User management panel
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
      cs = new ClientSession(ctx, ADMIN, ADMIN);
      start.setEnabled(false);
      host.setEnabled(false);
      port.setEnabled(false);
      change.setEnabled(false);
      fillLists();
      fillUserTab();
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
        startServer();
        fillLists();
        fillUserTab();
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
        try {
          cs.execute(new IntStop(), null);
        } catch(final IOException e1) {
          e1.printStackTrace();
        }
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
        } catch(final NumberFormatException n) {
          port.setText(String.valueOf(p));
        }
      }
    });
    p12.add(change);
    p12.setBorder(8, 8, 8, 8);
    p11.setBorder(8, 8, 8, 8);

    // adding to main panel
    p1.add(p11);
    p1.add(p12);
    set(tabs, BorderLayout.CENTER);
    finish(null);
  }

  /**
   * Fills the user tab with components.
   */
  void fillUserTab() {
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
    final BaseXCombo userco = new BaseXCombo(usernames, null, this);
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
          try {
            cs.execute(new CreateUser(u, p));
            user.setText("");
            pass.setText("");
            ((TableModel) table.getModel()).setData();
            userco.addItem(u);
            delete.setEnabled(true);
          } catch(IOException e1) {
            e1.printStackTrace();
          }
        }
      }
    });
    p21.add(create);
    final BaseXBack p22 = new BaseXBack();
    p22.setLayout(new TableLayout(2, 2, 6, 0));
    p22.add(new BaseXLabel("Drop User:", false, true));
    p22.add(new BaseXLabel(""));
    if(usernames.size() == 0) delete.setEnabled(false);
    delete.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final String test = (String) userco.getSelectedItem();
        try {
          cs.execute(new DropUser(test));
          userco.removeItem(test);
          ((TableModel) table.getModel()).setData();
          if(usernames.size() == 0) delete.setEnabled(false);
        } catch(IOException e1) {
          e1.printStackTrace();
        }
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
    p23.add(scrollPane);
    p2.add(p21);
    p2.add(p22);
    p2.add(p23);
  }

  /**
   * Starts a server as new process.
   */
  void startServer() {
    try {
      final ProcessBuilder pb = new ProcessBuilder("java", "-cp", Prop.WORK
          + "bin", "org.basex.BaseXServer");
      final Process p = pb.start();
      cs = new ClientSession(ctx, ADMIN, ADMIN);
      final BufferedReader in = new BufferedReader(new InputStreamReader(
          p.getInputStream()));
      new Thread() {
        @Override
        public void run() {
          String line = "";
          try {
            while((line = in.readLine()) != null) {
              Main.outln(line);
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
   * Fills all lists.
   */
  void fillLists() {
    final CachedOutput out = new CachedOutput();
    try {
      cs.execute(new Show("Users"), out);
    } catch(final IOException ex) {
      ex.printStackTrace();
    }
    String info = out.toString();
    info = info.substring(info.lastIndexOf("-") + 3);
    final Scanner s = new Scanner(info);
    int i = 0;
    data = new ArrayList<Object[]>();
    usernames = new Vector<String>();
    while(s.hasNextLine()) {
      final String line = s.nextLine();
      if(line.isEmpty()) break;
      String username = line.substring(0, 10).trim();
      usernames.addElement(username);
      Object[] user = new Object[5];
      user[0] = username;
      user[1] = line.substring(10, 16).contains("X");
      user[2] = line.substring(16, 23).contains("X");
      ;
      user[3] = line.substring(23, 31).contains("X");
      ;
      user[4] = line.substring(31, 38).contains("X");
      ;
      data.add(user);
      i++;
    }
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
      String right = "";
      if(col == 1) right = "read";
      if(col == 2) right = "write";
      if(col == 3) right = "create";
      if(col == 4) right = "admin";
      String uname = (String) data.get(row)[0];
      if(value.equals(true)) {
        try {
          cs.execute(new Grant(right, uname));
        } catch(IOException e) {
          e.printStackTrace();
        }
      } else {
        try {
          cs.execute(new Revoke(right, uname));
        } catch(IOException e) {
          e.printStackTrace();
        }
      }
      data.get(row)[col] = value;
      fireTableCellUpdated(row, col);
    }

    /**
     * Sets new data.
     */
    public void setData() {
      fillLists();
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
