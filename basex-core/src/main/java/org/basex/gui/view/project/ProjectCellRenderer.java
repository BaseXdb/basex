package org.basex.gui.view.project;

import java.awt.*;

import javax.swing.*;
import javax.swing.tree.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * Custom tree cell renderer to distinguish between resource types.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class ProjectCellRenderer extends DefaultTreeCellRenderer {
  @Override
  public Component getTreeCellRendererComponent(final JTree tree, final Object val,
      final boolean select, final boolean expanded, final boolean leaf, final int row,
      final boolean focus) {

    final Color tnsc = getTextNonSelectionColor(), tsc = getTextSelectionColor();
    if(val instanceof final ProjectNode node && node.error) {
      textNonSelectionColor = GUIConstants.RED;
      textSelectionColor = GUIConstants.RED;
    }
    super.getTreeCellRendererComponent(tree, val, select, expanded, leaf, row, focus);
    setIcon(icon(val, expanded));
    textNonSelectionColor = tnsc;
    textSelectionColor = tsc;
    return this;
  }

  /**
   * Returns an icon for the specified value.
   * @param val value
   * @param expanded expanded flag
   * @return icon
   */
  static Icon icon(final Object val, final boolean expanded) {
    return val instanceof final ProjectFile file ? BaseXImages.file(file.file) :
      BaseXImages.dir(expanded);
  }
}
