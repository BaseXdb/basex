package org.basex.gui.view.project;

import org.basex.io.*;

/**
 * Single directory node.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class ProjectDir extends ProjectNode {
  /**
   * Constructor.
   * @param io file
   * @param proj project view
   */
  ProjectDir(final IOFile io, final ProjectView proj) {
    super(io, proj);
    addDummy();
  }

  @Override
  void expand() {
    removeAllChildren();
    final IOFile[] files = file.children();
    for(final IOFile f : files) if(f.isDir())  add(new ProjectDir(f, project));
    for(final IOFile f : files) if(!f.isDir()) add(new ProjectFile(f, project));
  }

  @Override
  void collapse() {
    removeAllChildren();
    addDummy();
  }

  /**
   * Adds a dummy node if a directory contains entries.
   */
  private void addDummy() {
    if(file.children().length != 0) add(new ProjectFile(null, project));
  }

  @Override
  public String toString() {
    final String n = file.name();
    return n.isEmpty() ? file.file().getAbsolutePath() : n;
  }
}
