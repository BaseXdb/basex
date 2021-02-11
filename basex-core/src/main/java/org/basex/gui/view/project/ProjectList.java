package org.basex.gui.view.project;

import static org.basex.core.Text.*;

import java.awt.*;
import java.io.*;
import java.util.List;

import javax.swing.*;

import org.basex.core.cmd.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.listener.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * List of filtered file entries.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class ProjectList extends JList<String> {
  /** Font metrics. */
  private static FontMetrics fm;

  /** Popup commands. */
  final GUIPopupCmd[] commands = {
    new GUIPopupCmd(OPEN, BaseXKeys.ENTER) {
      @Override public void execute() { open(); }
    },
    new GUIPopupCmd(OPEN_EXTERNALLY, BaseXKeys.SHIFT_ENTER) {
      @Override public void execute() { openExternal(); }
      @Override public boolean enabled(final GUI main) { return selectedValue() != null; }
    }, null,
    new GUIPopupCmd(RUN_TESTS, BaseXKeys.UNIT) {
      @Override public void execute() { test(); }
      @Override public boolean enabled(final GUI main) { return selectedValue() != null; }
    }, null,
    new GUIPopupCmd(REFRESH, BaseXKeys.REFRESH) {
      @Override public void execute() { view.refresh(); }
    }, null,
    new GUIPopupCmd(COPY_PATH, BaseXKeys.COPYPATH) {
      @Override public void execute() { BaseXLayout.copyPath(selectedValue()); }
      @Override public boolean enabled(final GUI main) { return selectedValue() != null; }
    }
  };

  /** Project view. */
  private final ProjectView view;
  /** Content search string. */
  private String search = "";

  /**
   * Constructor.
   * @param view project view
   */
  ProjectList(final ProjectView view) {
    this.view = view;
    setBorder(BaseXLayout.border(4, 4, 4, 4));
    setCellRenderer(new CellRenderer());
    addMouseListener((MouseClickedListener) e -> {
      if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) open();
    });
    new BaseXPopup(this, view.gui, commands);
  }

  /**
   * Assigns the specified list entries and selects the first entry.
   * @param list result elements
   * @param srch content search string
   */
  void setElements(final String[] list, final String srch) {
    // only update list if values have changed (preserves selections)
    final ListModel<String> model = getModel();
    final int ll = list.length, ms = model.getSize();
    boolean same = ll == ms;
    for(int l = 0; same && l < ll; l++) same = list[l].equals(model.getElementAt(l));
    if(!same) {
      setListData(list);
      if(ll > 0) setSelectedIndex(0);
    }

    // remember search string
    search = srch;
  }

  /**
   * Open all selected files.
   */
  private void open() {
    for(final String file : selectedValues()) view.open(new IOFile(file), search);
  }

  /**
   * Open all selected files externally.
   */
  private void openExternal() {
    final StringList files = selectedValues();
    if(files.isEmpty()) return;
    final IOFile file = new IOFile(files.get(0));
    try {
      file.open();
    } catch(final IOException ex) {
      Util.debug(ex);
      BaseXDialog.error(view.gui, Util.info(FILE_NOT_OPENED_X, file));
    }
  }

  /**
   * Tests all files.
   */
  private void test() {
    for(final String file : selectedValues())  {
      view.gui.execute(new Test(file));
    }
  }

  /** List cell renderer. */
  private class CellRenderer extends DefaultListCellRenderer {
    /** Label. */
    private final BaseXLabel label;
    /** Current file. */
    private IOFile file = new IOFile(".");

    /**
     * Constructor.
     */
    CellRenderer() {
      label = new BaseXLabel() {
        @Override
        public void paintComponent(final Graphics g) {
          super.paintComponent(g);
          BaseXLayout.hints(g);

          if(fm == null) fm = g.getFontMetrics(label.getFont());
          final int y = Math.min(fm.getHeight(), (int) label.getPreferredSize().getHeight()) - 2;
          int x = (int) label.getPreferredSize().getWidth() + 2;

          final String s = file.name();
          g.setColor(GUIConstants.TEXT);
          g.drawString(s, x, y);
          x += fm.stringWidth(s);

          final String[] names = file.file().getParent().split("[/\\\\]");
          final StringBuilder sb = new StringBuilder(" \u00b7 ");
          for(int n = names.length - 1; n >= 0; n--) {
            sb.append(names[n]);
            if(n > 0) sb.append('/');
          }
          g.setColor(GUIConstants.dgray);
          g.drawString(sb.toString(), x, y);
        }
      };
      label.setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(final JList<?> list, final Object value,
        final int index, final boolean selected, final boolean expanded) {

      file = new IOFile(value.toString());
      label.setIcon(BaseXImages.file(file));
      label.setText("");
      label.setToolTipText(ProjectFile.toString(file, true));

      if(selected) {
        label.setBackground(getSelectionBackground());
        label.setForeground(getSelectionForeground());
      } else {
        label.setBackground(GUIConstants.BACK);
        label.setForeground(GUIConstants.TEXT);
      }
      return label;
    }
  }

  /**
   * Returns the selected value, or {@code null} if zero or more than one value is selected.
   * @return selected value
   */
  private String selectedValue() {
    final List<String> vals = getSelectedValuesList();
    return vals.size() == 1 ? vals.get(0) : null;
  }

  /**
   * Returns all selected values.
   * @return selected values
   */
  private StringList selectedValues() {
    // nothing selected: select first entry
    if(isSelectionEmpty() && getModel().getSize() != 0) setSelectedIndex(0);
    final StringList sl = new StringList();
    for(final String val : getSelectedValuesList()) sl.add(val);
    return sl;
  }
}
