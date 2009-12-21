package org.basex.gui.layout;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import org.basex.gui.dialog.Dialog;
import org.basex.util.Table;
import org.basex.util.Token;

/**
 * Project specific CheckBox implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class BaseXTable extends JTable {
  /** Table data. */
  public Table data;
  /** Table model. */
  final TableModel model;
  /** Dialog instance. */
  final Dialog dialog;

  /**
   * Default constructor.
   * @param t table input
   * @param d dialog reference
   */
  public BaseXTable(final Table t, final Dialog d) {
    super();
    data = t;
    dialog = d;
    model = new TableModel();
    setModel(model);
    getTableHeader().setReorderingAllowed(false);
    getTableHeader().setResizingAllowed(false);

    BaseXLayout.addInteraction(this, null, d);

    getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(final ListSelectionEvent e) {
        if(!e.getValueIsAdjusting() && getSelectedColumn() != -1) {
          dialog.action(e.getSource());
        }
      }
    });
  }

  /**
   * Updates the table.
   * @param t table;
   */
  public void update(final Table t) {
    data = t;
    model.fireTableChanged(null);
    for(int i = 1; i < data.cols(); i++) {
      final TableColumn tc = getColumnModel().getColumn(i);
      tc.setResizable(false);
      tc.setPreferredWidth(30);
    }
  }

  /**
   * Dialog specific table model.
   */
  final class TableModel extends AbstractTableModel {
    public int getColumnCount() {
      return data.cols();
    }

    public int getRowCount() {
      return data.rows();
    }

    @Override
    public String getColumnName(final int col) {
      return Token.string(data.header.get(col));
    }

    public Object getValueAt(final int row, final int col) {
      final String o = data.value(row, col);
      return o.equals("") ? Boolean.FALSE : o.equals("X") ? Boolean.TRUE : o;
    }

    @Override
    public Class<?> getColumnClass(final int c) {
      return getValueAt(0, c).getClass();
    }

    @Override
    public boolean isCellEditable(final int row, final int col) {
      return col != 0 && !data.value(row, 0).equals("admin");
    }

    @Override
    public void setValueAt(final Object value, final int row, final int col) {
      dialog.action(new Object[] { value, row, col });
    }
  }
}
