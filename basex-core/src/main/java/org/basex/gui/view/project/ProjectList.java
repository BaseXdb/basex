package org.basex.gui.view.project;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.gui.layout.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * List of filtered file entries.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public class ProjectList extends JList {
  /** Model. */
  final DefaultListModel model;

  /**
   * Constructor.
   * @param view view
   */
  ProjectList(final ProjectView view) {
    model = new DefaultListModel();
    setModel(model);
    setCellRenderer(new CellRenderer());
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyTyped(final KeyEvent e) {
        if(BaseXKeys.ENTER.is(e)) open(view);
      }
    });
    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(final MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) open(view);
      }
    });
  }

  /**
   * Open all selected files.
   * @param view view
   */
  private void open(final ProjectView view) {
    for(final Object o : getSelectedValues()) view.open(new IOFile(o.toString()));
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

      final StringBuilder sb = new StringBuilder();
      sb.append(file.name()).append(" (");
      sb.append(Performance.format(file.length(), true)).append(')');
      label.setText(sb.toString());
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
