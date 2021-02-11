package org.basex.gui.view.project;

import org.basex.io.*;
import org.basex.util.*;

/**
 * Single leaf node.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class ProjectFile extends ProjectNode {
  /**
   * Constructor.
   * @param file file ({@code null} for dummy)
   * @param view project view
   */
  ProjectFile(final IOFile file, final ProjectView view) {
    super(file, view);
  }

  @Override
  void expand() { }

  @Override
  void collapse() { }

  @Override
  void refresh() {
    view.refreshHighlight(this);
  }

  @Override
  public String toString() {
    return toString(file, false);
  }

  /**
   * Returns a string representation for the specified file.
   * @param file file
   * @param full full path
   * @return string
   */
  static String toString(final IOFile file, final boolean full) {
    final StringBuilder sb = new StringBuilder();
    if(file != null) {
      sb.append(full ? file.path() : file.name());
      if(file.exists()) sb.append(" (").append(Performance.format(file.length())).append(')');
    }
    return sb.toString();
  }
}
