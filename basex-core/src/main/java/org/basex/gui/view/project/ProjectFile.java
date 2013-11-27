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
    final StringBuilder sb = new StringBuilder();
    if(file != null) {
      sb.append(file.name()).append(" (");
      sb.append(Performance.format(file.length(), true)).append(')');
    }
    return sb.toString();
  }
}
