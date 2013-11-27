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
  private ProjectView project;
  /** Model. */
  final DefaultListModel model;

  /**
   * Constructor.
   * @param view project view
   */
  ProjectList(final ProjectView view) {
    project = view;
    model = new DefaultListModel();
    setModel(model);
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
   */
  void addElements(final StringList list) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        model.removeAllElements();
        for(final String file : list) model.addElement(file);
        setSelectedIndex(0);
      }
    });
  }

  /**
   * Open all selected files.
   */
  void open() {
    for(final Object o : getSelectedValues()) project.open(new IOFile(o.toString()));
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
