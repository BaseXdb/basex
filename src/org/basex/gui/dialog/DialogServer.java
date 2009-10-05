package org.basex.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.proc.List;
import org.basex.gui.GUI;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTabs;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;

/**
 * Dialog window for displaying information about the server.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public class DialogServer extends Dialog {
  
  /** Context. */
  private Context ctx = new Context();
  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogServer(final GUI main) {
    super(main, "Server...");
    // create panels
    final BaseXBack p1 = new BaseXBack();
    p1.setLayout(new TableLayout(7, 1));
    p1.setBorder(8, 8, 8, 8);
    p1.add(new BaseXLabel("Message window:"));
    BaseXTextField t = new BaseXTextField(null, this);
    p1.add(t);
    // create panels
    final BaseXBack p2 = new BaseXBack();
    p2.setLayout(new TableLayout(7, 1));
    p2.setBorder(8, 8, 8, 8);
    final BaseXBack p21 = new BaseXBack();
    p21.setLayout(new TableLayout(2, 1, 6, 0));
    final BaseXBack p22 = new BaseXBack();
    p22.setLayout(new TableLayout(2, 5, 6, 0));
    p22.add(new BaseXLabel("Create User:", false, true));
    p22.add(new BaseXLabel(""));
    p22.add(new BaseXLabel(""));
    p22.add(new BaseXLabel(""));
    p22.add(new BaseXLabel(""));
    p22.add(new BaseXLabel("Username:"));
    p22.add(new BaseXTextField("Enter username", null, this));
    p22.add(new BaseXLabel("Password:"));
    p22.add(new BaseXTextField("Enter password", null, this));
    p22.add(new BaseXButton("Create User", null, this));
    p21.setLayout(new TableLayout(2, 1, 6, 0));
    final BaseXBack p23 = new BaseXBack();
    p23.setLayout(new TableLayout(2, 2, 6, 0));
    p23.add(new BaseXLabel("Delete User:", false, true));
    p23.add(new BaseXLabel(""));
    p23.add(new BaseXCombo(new String[] {
        "Christian", "Andreas", "Sebastian" }, null, this));
    p23.add(new BaseXButton("Delete User", null, this));
    p21.add(p22);
    p21.add(p23);
    p2.add(p21);
    p2.add(new BaseXLabel("                      "));
    p2.add(new BaseXLabel("User rights management:", false, true));
    JTable table = new JTable(new TableModel());
    table.setPreferredScrollableViewportSize(new Dimension(500, 70));
    table.setFillsViewportHeight(true);
    // Create the scroll pane and add the table to it.
    JScrollPane scrollPane = new JScrollPane(table);
    setUpColumn(table.getColumnModel().getColumn(0), 0);
    setUpColumn(table.getColumnModel().getColumn(1), 1);
    p2.add(scrollPane);

    // create panels
    final BaseXBack p3 = new BaseXBack();
    p3.setLayout(new TableLayout(7, 1));
    p3.setBorder(8, 8, 8, 8);
    final BaseXBack p31 = new BaseXBack();
    p31.setLayout(new TableLayout(4, 2, 6, 0));
    p31.add(new BaseXLabel("Port:"));
    p31.add(new BaseXTextField(String.valueOf(ctx.prop.num(Prop.PORT)),
        null, this));
    p31.add(new BaseXLabel("Host:"));
    p31.add(new BaseXTextField(ctx.prop.get(Prop.HOST), null, this));
    p31.add(new BaseXLabel(" "));
    p31.add(new BaseXLabel(" "));
    p31.add(new BaseXLabel("        "));
    p31.add(new BaseXButton("Change", null, this));
    p3.add(p31);
    final BaseXTabs tabs = new BaseXTabs(this);
    tabs.add("Messages", p1);
    tabs.add("Users", p2);
    tabs.add("Properties", p3);
    set(tabs, BorderLayout.CENTER);
    finish(null);
  }

  /**
   * Sets up the choosebox column.
   * @param column TableColumn
   * @param wh int what column
   */
  public void setUpColumn(final TableColumn column, final int wh) {
    // Set up the editor for the cells.
    JComboBox comboBox = new JComboBox();
    if(wh == 0) {
    comboBox.addItem("Christian");
    comboBox.addItem("Lukas");
    comboBox.addItem("Andreas");
    comboBox.addItem("Sebastian");
    comboBox.addItem("Alex");
    } else if(wh == 1) {
      final String[] list = List.list(ctx).finish();
      for(final String name : list) {
        comboBox.addItem(name);
      }
    }
    column.setCellEditor(new DefaultCellEditor(comboBox));

    // Set up tool tips for cells.
    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    renderer.setToolTipText("Click for combo box");
    column.setCellRenderer(renderer);
  }

  /**
   * Class of own tablemodel.
   * 
   * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
   * @author Andreas Weiler
   */
  class TableModel extends AbstractTableModel {

    /** Columnnames. */
    private String[] columnNames = { "Username", "Database",
        "Read", "Write"};
    /** Data. */
    private Object[][] data = {
        { "Christian", "input", new Boolean(true), new Boolean(false)},
        { "Andreas", "factbook", new Boolean(false), new Boolean(true)},
        { "Sebastian", "factbook", new Boolean(true), new Boolean(false)},
        { "Lukas", "input", new Boolean(true), new Boolean(true)},
        { "Alex", "factbook", new Boolean(true), new Boolean(false)}};

    public int getColumnCount() {
      return columnNames.length;
    }

    public int getRowCount() {
      return data.length;
    }

    @Override
    public String getColumnName(final int col) {
      return columnNames[col];
    }

    public Object getValueAt(final int row, final int col) {
      return data[row][col];
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class getColumnClass(final int c) {
      return getValueAt(0, c).getClass();
    }

    @Override
    public boolean isCellEditable(final int row, final int col) {
      return true;
    }

    @Override
    public void setValueAt(final Object value, final int row, final int col) {
      data[row][col] = value;
      fireTableCellUpdated(row, col);
    }
  }
}
