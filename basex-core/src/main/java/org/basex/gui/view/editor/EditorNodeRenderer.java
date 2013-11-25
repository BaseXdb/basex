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
public final class EditorNodeRenderer extends DefaultTreeCellRenderer {
  /** Icon for textual files. */
  private final Icon fileText = BaseXLayout.icon("file-text");
  /** Icon for XML/XQuery file types. */
  private final Icon fileXml = BaseXLayout.icon("file-xml");
  /** Icon for unknown file types. */
  private final Icon fileUnknown = BaseXLayout.icon("file-unknown");
  /** Icon for closed directories. */
  private final Icon fileDir1 = BaseXLayout.icon("file-dir1");
  /** Icon for opened directories. */
  private final Icon fileDir2 = BaseXLayout.icon("file-dir2");

  @Override
  public Component getTreeCellRendererComponent(final JTree tree, final Object val,
      final boolean sel, final boolean exp, final boolean leaf, final int row,
      final boolean focus) {

    super.getTreeCellRendererComponent(tree, val, sel, exp, leaf, row, focus);
    if(val instanceof EditorFile) {
      final IOFile file = ((EditorFile) val).file;
      if(file != null) {
        final String mime = MimeTypes.get(file.path());
        setIcon(MimeTypes.isXML(mime) || MimeTypes.isXQuery(mime) ? fileXml :
          MimeTypes.isText(mime) ? fileText : fileUnknown);
      }
    } else {
      setIcon(exp ? fileDir2 : fileDir1);
    }
    return this;
  }
}

