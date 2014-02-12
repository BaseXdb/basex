package org.basex.gui.view.project;

import java.awt.*;

import javax.swing.*;
import javax.swing.tree.*;

import org.basex.gui.layout.*;

/**
 * Custom tree cell renderer to distinguish between raw and xml leaf nodes.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class ProjectCellRenderer extends DefaultTreeCellRenderer {
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
    return val instanceof ProjectFile ? BaseXImages.file(((ProjectFile) val).file) :
      BaseXImages.dir(expanded);
  }
}
