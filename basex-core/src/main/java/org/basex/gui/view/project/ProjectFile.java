package org.basex.gui.view.project;

import org.basex.io.*;
import org.basex.util.*;

/**
 * Single leaf node.
 *
 * @author BaseX Team 2005-13, BSD License
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
    return toString(file);
  }

  /**
   * Returns a string representation for the specified file.
   * @param file file
   * @return string
   */
  public static String toString(final IOFile file) {
    final StringBuilder sb = new StringBuilder();
    if(file != null) {
      sb.append(file.name());
      if(file.exists()) sb.append(" (").append(Performance.format(file.length(), true)).append(')');
    }
    return sb.toString();
  }
}
