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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class ProjectList extends JList<String> implements ProjectCommands {
  /** Font metrics (can be {@code null}). */
  private static FontMetrics fm;

  /** Project view. */
  private final ProjectView view;
  /** Popup menu commands. */
  final GUIPopupCmd[] commands;

  /** Content search string. */
  private String search = "";
  /** Number of content hits per file ({@code -1}: unknown; missing: not counted yet). */
  private final HashMap<String, Integer> counts = new HashMap<>();
  /** Indicates that the list shows all matching files. */
  private boolean complete;

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
   * Assigns the specified list entries and selects the first one.
   * @param list result elements
   * @param srch content search string
   * @param all list shows all matching files
   */
  void setElements(final String[] list, final String srch, final boolean all) {
    // rebuild the list only if the entries changed (preserves the selection otherwise)
    final ListModel<String> model = getModel();
    final int ll = list.length, ms = model.getSize();
    boolean same = ll == ms;
    for(int l = 0; same && l < ll; l++) same = list[l].equals(model.getElementAt(l));
    if(!same) {
      setListData(list);
      if(ll > 0) setSelectedIndex(0);
    }

    // discard the hit counts if the entries or the search string have changed
    if(!same || !search.equals(srch)) counts.clear();
    search = srch;
    complete = all;
  }

  /**
   * Assigns the number of content hits of files and refreshes the list.
   * @param hits number of hits per file path ({@code -1} if a file could not be read)
   */
  void count(final Map<String, Integer> hits) {
    counts.putAll(hits);
    repaint();
  }

  /**
   * Indicates if the list shows all matching files.
   * @return result of check
   */
  boolean complete() {
    return complete;
  }


  /**
   * Returns all listed files.
   * @return files
   */
  List<IOFile> allFiles() {
    final ListModel<String> model = getModel();
    final int ms = model.getSize();
    final ArrayList<IOFile> files = new ArrayList<>(ms);
    for(int m = 0; m < ms; m++) files.add(new IOFile(model.getElementAt(m)));
    return files;
  }

  /**
   * Returns the number of counted hits in the specified files.
   * @param files files
   * @return number of hits, or {@code -1} if a file has not been counted or is too large
   */
  int hits(final List<IOFile> files) {
    int hits = 0;
    for(final IOFile file : files) {
      final Integer count = counts.get(file.path());
      if(count == null || count < 0) return -1;
      hits += count;
    }
    return hits;
  }

  /** List cell renderer. */
  private class CellRenderer extends DefaultListCellRenderer {
    /** Label. */
    private final BaseXLabel label;
    /** Current file. */
    private IOFile file = new IOFile(".");
    /** Content hits of the current file ({@code null} if not counted yet). */
    private Integer count;

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
          g.setColor(GUIConstants.textColor);
          g.drawString(s, x, y);
          x += fm.stringWidth(s);
          g.setColor(GUIConstants.gray);
          // files that have not been counted yet have no marker at all
          if(count != null) {
            final String c = " \u00b7 " + (count < 0 ? "?" : BaseXLayout.format(count));
            g.drawString(c, x, y);
            x += fm.stringWidth(c);
          }
          g.drawString(" \u00b7 " + BaseXLayout.reversePath(file), x, y);
        }
      };
      label.setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(final JList<?> list, final Object value,
        final int index, final boolean selected, final boolean expanded) {

      file = new IOFile(value.toString());
      count = counts.get(file.path());
      label.setIcon(BaseXImages.file(file));
      label.setText("");
      label.setToolTipText(BaseXLayout.info(file, true));

      if(selected) {
        label.setBackground(getSelectionBackground());
        label.setForeground(getSelectionForeground());
      } else {
        label.setBackground(GUIConstants.backColor);
        label.setForeground(GUIConstants.textColor);
      }
      return label;
    }
  }

  @Override
  public IOFile selectedFile() {
    final List<IOFile> files = selectedFiles();
    return files.size() == 1 ? files.getFirst() : null;
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
