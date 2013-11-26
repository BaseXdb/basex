package org.basex.gui.view.editor;

import java.awt.*;

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
final class EditorCellRenderer extends DefaultTreeCellRenderer {
  /** Icon for textual files. */
  private static final Icon TEXT = BaseXLayout.icon("file-text");
  /** Icon for XML/XQuery file types. */
  private static final Icon XML = BaseXLayout.icon("file-xml");
  /** Icon for unknown file types. */
  private static final Icon UNKNOWN = BaseXLayout.icon("file-unknown");
  /** Icon for closed directories. */
  private static final Icon DIR1 = BaseXLayout.icon("file-dir1");
  /** Icon for opened directories. */
  private static final Icon DIR2 = BaseXLayout.icon("file-dir2");

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
    if(val instanceof EditorFile) {
      final IOFile file = ((EditorFile) val).file;
      if(file != null) {
        final String mime = MimeTypes.get(file.path());
        return MimeTypes.isXML(mime) || MimeTypes.isXQuery(mime) ? XML :
          MimeTypes.isText(mime) ? TEXT : UNKNOWN;
      }
    }
    return expanded ? DIR2 : DIR1;
  }
}
