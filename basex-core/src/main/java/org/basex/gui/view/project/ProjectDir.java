package org.basex.gui.view.project;

import java.util.*;

import javax.swing.tree.*;

import org.basex.gui.*;
import org.basex.io.*;

/**
 * Single directory node.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class ProjectDir extends ProjectNode {
  /** Comparator. */
  private static final Comparator<IOFile> COMP = (a, b) -> a.path().compareToIgnoreCase(b.path());

  /** Expanded directories. */
  private final ArrayList<IOFile> dirs = new ArrayList<>();
  /** Expanded files. */
  private final ArrayList<IOFile> files = new ArrayList<>();

  /**
   * Constructor.
   * @param file file
   * @param view project view
   */
  ProjectDir(final IOFile file, final ProjectView view) {
    super(file, view);
    addDummy();
  }

  @Override
  void expand() {
    final ArrayList<IOFile> newDirs = new ArrayList<>(), newFiles = new ArrayList<>();
    final boolean showHidden = view.gui.gopts.get(GUIOptions.SHOWHIDDEN);
    for(final IOFile child : file.children()) {
      if(child.ignore()) {
        newDirs.clear();
        newFiles.clear();
        newFiles.add(child);
        break;
      }
      if(showHidden || !child.isHidden()) {
        (child.isDir() ? newDirs : newFiles).add(child);
      }
    }
    newDirs.sort(COMP);
    newFiles.sort(COMP);

    if(!newDirs.equals(dirs) || !newFiles.equals(files)) {
      removeChildren();
      dirs.addAll(newDirs);
      files.addAll(newFiles);
      for(final IOFile child : dirs) add(new ProjectDir(child, view));
      for(final IOFile child : files) add(new ProjectFile(child, view));
      updateTree();
    }
    view.refreshHighlight(this);
  }

  @Override
  void collapse() {
    removeChildren();
    addDummy();
    updateTree();
  }

  @Override
  void refresh() {
    if(view.tree.isExpanded(path())) {
      expand();
    } else {
      view.refreshHighlight(this);
    }
    if(children != null) {
      for(final Object child : children) ((ProjectNode) child).refresh();
    }
  }

  /**
   * Adds a dummy node if a directory contains entries.
   */
  private void addDummy() {
    if(file.children().length != 0) add(new ProjectFile(null, view));
  }

  /**
   * Removes all children.
   */
  private void removeChildren() {
    dirs.clear();
    files.clear();
    removeAllChildren();
  }

  /**
   * Updates the tree structure.
   */
  private void updateTree() {
    ((DefaultTreeModel) view.tree.getModel()).nodeStructureChanged(this);
  }

  @Override
  public String toString() {
    final String name = file.name();
    return name.isEmpty() ? file.file().getAbsolutePath() : name;
  }
}
