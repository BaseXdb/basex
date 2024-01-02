package org.basex.gui.view.project;

import org.basex.gui.layout.*;
import org.basex.io.*;

/**
 * Single leaf node.
 *
 * @author BaseX Team 2005-24, BSD License
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
    return BaseXLayout.info(file, false);
  }
}
