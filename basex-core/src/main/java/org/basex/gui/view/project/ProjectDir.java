package org.basex.gui.view.project;

import java.util.*;

import org.basex.gui.*;
import org.basex.io.*;

/**
 * Single directory node.
 *
 * @author BaseX Team 2005-17, BSD License
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
   * @param file file
   * @param project project view
   */
  ProjectDir(final IOFile file, final ProjectView project) {
    super(file, project);
    addDummy();
  }

  @Override
  void expand() {
    removeAllChildren();
    // cache and sort directories and files
    final ArrayList<IOFile> dirs = new ArrayList<>(), files = new ArrayList<>();
    final boolean hidden = project.gui.gopts.get(GUIOptions.HIDDENFILES);
    for(final IOFile child : file.children()) {
      if(hidden || !child.file().isHidden()) (child.isDir() ? dirs : files).add(child);
    }
    Collections.sort(dirs, COMP);
    Collections.sort(files, COMP);
    // create child nodes
    for(final IOFile f : dirs) add(new ProjectDir(f, project));
    for(final IOFile f : files) add(new ProjectFile(f, project));
    project.refreshTree(this);
  }

  @Override
  void collapse() {
    removeAllChildren();
    addDummy();
    project.refreshTree(this);
  }

  @Override
  void refresh() {
    collapse();
    expand();
    updateTree();
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
