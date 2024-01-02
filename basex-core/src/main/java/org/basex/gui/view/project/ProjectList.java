package org.basex.gui.view.project;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.listener.*;
import org.basex.io.*;

/**
 * List of filtered file entries.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
final class ProjectList extends JList<String> implements ProjectCommands {
  /** Font metrics. */
  private static FontMetrics fm;

  /** Project view. */
  private final ProjectView view;
  /** Popup menu commands. */
  final GUIPopupCmd[] commands;

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
      if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
        view.open(selectedFile(), search);
      }
    });

    commands = commands();
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
          g.setColor(GUIConstants.gray);
          g.drawString(" \u00b7 " + BaseXLayout.reversePath(file), x, y);
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
      label.setToolTipText(BaseXLayout.info(file, true));

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

  @Override
  public IOFile selectedFile() {
    final List<IOFile> files = selectedFiles();
    return files.size() == 1 ? files.get(0) : null;
  }

  @Override
  public List<IOFile> selectedFiles() {
    // nothing selected: select first entry
    if(isSelectionEmpty() && getModel().getSize() != 0) setSelectedIndex(0);

    final ArrayList<IOFile> files = new ArrayList<>();
    for(final String value : getSelectedValuesList()) files.add(new IOFile(value));
    return files;
  }

  @Override
  public ProjectView view() {
    return view;
  }

  @Override
  public String search() {
    return search;
  }

  @Override
  public void refresh() {
    view.refresh();
  }
}
