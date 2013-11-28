package org.basex.gui.view.project;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import org.basex.gui.layout.*;
import org.basex.io.*;

/**
 * Custom tree cell renderer to distinguish between raw and xml leaf nodes.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class ProjectCellRenderer extends DefaultTreeCellRenderer {
  /** File chooser (used for retrieving system icons). */
  private static final JFileChooser CHOOSER = new JFileChooser();
  /** Icon cache. */
  private static final HashMap<String, Icon> ICONS = new HashMap<String, Icon>();
  /** Icon for closed directories. */
  private static final Icon DIR1 = BaseXLayout.icon("file-dir1");
  /** Icon for opened directories. */
  private static final Icon DIR2 = BaseXLayout.icon("file-dir2");
  /** Icon for unknown file types. */
  private static final Icon UNKNOWN = BaseXLayout.icon("file-unknown");

  @Override
  public Component getTreeCellRendererComponent(final JTree tree, final Object val,
      final boolean select, final boolean expanded, final boolean leaf, final int row,
      final boolean focus) {

    super.getTreeCellRendererComponent(tree, val, select, expanded, leaf, row, focus);
    setIcon(icon(val, expanded));
    return this;
  }

  /**
   * Returns an icon for the specified value.
   * @param val value
   * @param expanded expanded flag
   * @return icon
   */
  static Icon icon(final Object val, final boolean expanded) {
    return val instanceof ProjectFile ? fileIcon(((ProjectFile) val).file) : expanded ? DIR2 : DIR1;
  }

  /**
   * Returns an icon for the specified value.
   * @param file file reference
   * @return icon
   */
  static Icon fileIcon(final IOFile file) {
    if(file == null) return UNKNOWN;
    final String path = file.path();
    final int i = path.lastIndexOf(path, '.');
    final String suffix = i != -1 ? path.substring(i + 1) : null;
    Icon icon = null;
    if(suffix != null) icon = ICONS.get(suffix);
    if(icon == null) {
      icon = CHOOSER.getIcon(file.file());
      if(suffix != null) ICONS.put(suffix, icon);
    }
    return icon;
  }
}
