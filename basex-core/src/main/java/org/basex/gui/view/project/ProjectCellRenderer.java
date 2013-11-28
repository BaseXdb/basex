package org.basex.gui.view.project;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.tree.*;

import org.basex.gui.layout.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Custom tree cell renderer to distinguish between raw and xml leaf nodes.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class ProjectCellRenderer extends DefaultTreeCellRenderer {
  /** Icon cache. */
  private static final HashMap<String, Icon> ICONS = new HashMap<String, Icon>();
  /** System icons. */
  private static final FileSystemView FS = FileSystemView.getFileSystemView();
  /** Icon for closed directories. */
  private static final Icon DIR1 = BaseXLayout.icon("file-dir1");
  /** Icon for opened directories. */
  private static final Icon DIR2 = BaseXLayout.icon("file-dir2");
  /** Icon for textual files. */
  private static final Icon TEXT = BaseXLayout.icon("file-text");
  /** Icon for XML/XQuery file types. */
  private static final Icon XML = BaseXLayout.icon("file-xml");
  /** Icon for XML/XQuery file types. */
  private static final Icon XQUERY = BaseXLayout.icon("file-xquery");
  /** Icon for BaseX file types. */
  private static final Icon BASEX = BaseXLayout.icon("file-basex");
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

    // fallback code for displaying icons
    final String path = file.path();
    final String mime = MimeTypes.get(path);
    if(MimeTypes.isXML(mime)) return XML;
    if(MimeTypes.isXQuery(mime)) return XQUERY;
    if(path.contains(IO.BASEXSUFFIX)) return BASEX;

    if(Prop.WIN) {
      // retrieve system icons (only supported on Windows)
      final int p = path.lastIndexOf(path, '.');
      final String suffix = p != -1 ? path.substring(p + 1) : null;
      Icon icon = null;
      if(suffix != null) icon = ICONS.get(suffix);
      if(icon == null) {
        icon = FS.getSystemIcon(file.file());
        if(suffix != null) ICONS.put(suffix, icon);
      }
      return icon;
    }
    // default icon chooser
    return MimeTypes.isText(mime) ? TEXT : UNKNOWN;
  }
}
