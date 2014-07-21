package org.basex.gui.view.project;

import static org.basex.core.Text.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;

import org.basex.core.cmd.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * List of filtered file entries.
 *
 * @author BaseX Team 2005-14, BSD License
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
    new GUIPopupCmd(OPEN_EXTERNALLY, BaseXKeys.OPEN) {
      @Override public void execute() { openExternal(); }
    }, null,
    new GUIPopupCmd(RUN_TESTS, BaseXKeys.UNIT) {
      @Override public void execute() { test(); }
      @Override public boolean enabled(final GUI main) { return selectedValue() != null; }
    }, null,
    new GUIPopupCmd(REFRESH, BaseXKeys.REFRESH) {
      @Override public void execute() { project.refresh(); }
    },
    new GUIPopupCmd(COPY_PATH, BaseXKeys.COPYPATH) {
      @Override public void execute() {
        if(enabled(null)) BaseXLayout.copy(selectedValue());
      }
      @Override public boolean enabled(final GUI main) { return selectedValue() != null; }
    }
  };

  /** Project view. */
  private final ProjectView project;
  /** Content search string. */
  private String search;

  /**
   * Constructor.
   * @param view project view
   */
  ProjectList(final ProjectView view) {
    project = view;
    setBorder(new EmptyBorder(4, 4, 4, 4));
    setCellRenderer(new CellRenderer());
    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(final MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) open();
      }
    });
    new BaseXPopup(this, view.gui, commands);
  }

  /**
   * Assigns the specified list entries and selects the first entry.
   * @param elements result elements
   * @param srch content search string
   */
  void setElements(final TokenSet elements, final String srch) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        // set new values and selections
        final int is = elements.size();
        final String[] list = new String[is];
        for(int i = 0; i < is; i++) list[i] = Token.string(elements.key(i + 1));
        if(changed(list)) {
          // check which old values had been selected
          final List<String> values = getSelectedValuesList();
          final IntList il = new IntList();
          for(final String value : values) {
            final byte[] val = Token.token(value);
            for(int i = 0; i < is; i++) {
              if(Token.eq(val, elements.key(i + 1))) {
                il.add(i);
                break;
              }
            }
          }
          setListData(list);
          setSelectedIndices(il.finish());
        }
        search = srch;
      }
    });
  }

  /**
   * Checks if the list contents have changed.
   * @param list entries to set
   * @return result of check
   */
  private boolean changed(final String[] list) {
    final int sl = list.length, el = getModel().getSize();
    if(sl != el) return true;
    for(int i = 0; i < sl; i++) {
      if(!list[i].equals(getModel().getElementAt(i))) return true;
    }
    return false;
  }

  /**
   * Open all selected files.
   */
  private void open() {
    for(final IOFile file : selectedValues()) project.open(file, search);
  }

  /**
   * Open all selected files externally.
   */
  private void openExternal() {
    for(final IOFile file : selectedValues())  {
      try {
        file.open();
      } catch(final IOException ex) {
        BaseXDialog.error(project.gui, Util.info(FILE_NOT_OPENED_X, file));
      }
    }
  }

  /**
   * Tests all files.
   */
  private void test() {
    for(final IOFile file : selectedValues())  {
      project.gui.execute(new Test(file.path()));
    }
  }

  /** List cell renderer. */
  class CellRenderer extends DefaultListCellRenderer {
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
          g.setColor(label.getForeground());
          g.drawString(s, x, y);
          x += fm.stringWidth(s);

          final String[] names = file.file().getParent().split("/|\\\\");
          final StringBuilder sb = new StringBuilder(" ");
          for(int n = names.length - 1; n >= 0; n--) sb.append('/').append(names[n]);
          g.setColor(GUIConstants.GRAY);
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
        label.setBackground(Color.WHITE);
        label.setForeground(getForeground());
      }
      return label;
    }
  }

  /**
   * Returns a single selected node, or {@code null} if zero or more than node is selected.
   * @return selected node
   */
  private String selectedValue() {
    final List<String> vals = getSelectedValuesList();
    return vals.size() == 1 ? vals.get(0) : null;
  }

  /**
   * Returns a single selected node, or {@code null} if zero or more than node is selected.
   * @return selected node
   */
  private IOFile[] selectedValues() {
    // nothing selected: select first entry
    if(isSelectionEmpty() && getModel().getSize() != 0) setSelectedIndex(0);
    final ArrayList<IOFile> list = new ArrayList<>();
    for(final String val : getSelectedValuesList()) list.add(new IOFile(val));
    return list.toArray(new IOFile[list.size()]);
  }
}
