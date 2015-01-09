package org.basex.gui.view.project;

import org.basex.io.*;
import org.basex.util.*;

/**
 * Single leaf node.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class ProjectFile extends ProjectNode {
  /**
   * Constructor.
   * @param io file
   * @param proj project view
   */
  ProjectFile(final IOFile io, final ProjectView proj) {
    super(io, proj);
  }

  @Override
  void expand() { }

  @Override
  void collapse() { }

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
      if(file.exists()) sb.append(" (").append(Performance.format(file.length(), true)).append(')');
    }
    return sb.toString();
  }
}
