package org.basex.gui.view.editor;

import org.basex.io.*;

/**
 * Single directory node.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class EditorDir extends EditorNode {
  /**
   * Constructor.
   * @param io file
   * @param tr tree
   */
  EditorDir(final IOFile io, final EditorTree tr) {
    super(io, tr);
    addDummy();
  }

  @Override
  void expand() {
    removeAllChildren();
    final IOFile[] files = file.children();
    for(final IOFile f : files) if(f.isDir())  add(new EditorDir(f, tree));
    for(final IOFile f : files) if(!f.isDir()) add(new EditorFile(f, tree));
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
    if(file.children().length != 0) add(new EditorFile(null, tree));
  }

  @Override
  public String toString() {
    final String n = file.name();
    return n.isEmpty() ? file.file().getAbsolutePath() : n;
  }
}
