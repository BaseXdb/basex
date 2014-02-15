package org.basex.gui.view.project;

import java.util.*;

import org.basex.io.*;

/**
 * Single directory node.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class ProjectDir extends ProjectNode {
  /** Comparator. */
  private static final Comparator<IOFile> COMP = new Comparator<IOFile>() {
    @Override
    public int compare(final IOFile a, final IOFile b) {
      return a.path().compareToIgnoreCase(b.path());
    }
  };

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
    // cache and sort directories and files
    final ArrayList<IOFile> dirs = new ArrayList<IOFile>();
    final ArrayList<IOFile> files = new ArrayList<IOFile>();
    for(final IOFile f : file.children()) (f.isDir() ? dirs : files).add(f);
    Collections.sort(dirs, COMP);
    Collections.sort(files, COMP);
    // create child nodes
    for(final IOFile f : dirs) add(new ProjectDir(f, project));
    for(final IOFile f : files) add(new ProjectFile(f, project));
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
