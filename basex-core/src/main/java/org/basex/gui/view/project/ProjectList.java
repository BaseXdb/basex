package org.basex.gui.view.project;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.gui.layout.*;
import org.basex.io.*;
import org.basex.util.list.*;

/**
 * List of filtered file entries.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public class ProjectList extends JList {
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
    setCellRenderer(new CellRenderer());
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyTyped(final KeyEvent e) {
        if(BaseXKeys.ENTER.is(e)) open();
      }
    });
    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(final MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) open();
      }
    });
  }

  /**
   * Assigns the specified list entries and selects the first entry.
   * @param list entries to set
   * @param srch content search string
   */
  void setElements(final StringList list, final String srch) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if(changed(list)) {
          // check which old values had been selected
          final Object[] old = getSelectedValues();
          final IntList il = new IntList();
          final int is = list.size();
          for(final Object o : old) {
            for(int i = 0; i < is; i++) {
              if(o.equals(list.get(i))) {
                il.add(i);
                break;
              }
            }
          }
          if(il.isEmpty()) il.add(0);
          // set new values and selections
          setListData(list.toArray());
          setSelectedIndices(il.toArray());
        }
        search = srch;
      }
    });
  }

  /**
   * Checks if the list needs to be updated.
   * @param list entries to set
   * @return result of check
   */
  boolean changed(final StringList list) {
    final int sl = list.size(), el = getModel().getSize();
    if(sl != el) return true;
    for(int i = 0; i < sl; i++) {
      if(!list.get(i).equals(getModel().getElementAt(i))) return true;
    }
    return false;
  }

  /**
   * Open all selected files.
   */
  void open() {
    System.out.println("Open " + search);
    for(final Object o : getSelectedValues()) {
      project.open(new IOFile(o.toString()), search);
    }
  }

  /** List cell renderer. */
  class CellRenderer extends DefaultListCellRenderer {
    /** Label. */
    private final BaseXLabel label;

    /**
     * Constructor.
     */
    CellRenderer() {
      label = new BaseXLabel();
      label.setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(final JList list, final Object value,
        final int index, final boolean selected, final boolean expanded) {

      final IOFile file = new IOFile(value.toString());
      label.setIcon(ProjectCellRenderer.fileIcon(file));
      label.setText(ProjectFile.toString(file));
      label.setToolTipText(file.path());

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
}
