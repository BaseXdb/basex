package org.basex.gui.view.editor;

import javax.swing.tree.*;

import org.basex.io.*;

/**
 * Single folder node.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class EditorFolder extends EditorNode {
  /** Model. */
  private final DefaultTreeModel model;

  /**
   * Constructor.
   * @param io file
   * @param tm tree model
   */
  EditorFolder(final IOFile io, final DefaultTreeModel tm) {
    super(io);
    add(new EditorFile(null));
    model = tm;
  }

  @Override
  void expand() {
    removeAllChildren();
    final IOFile[] files = file.children();
    for(final IOFile f : files) if(f.isDir()) add(new EditorFolder(f, model));
    for(final IOFile f : files) if(!f.isDir()) add(new EditorFile(f));
    model.nodeStructureChanged(this);
  }

  @Override
  void collapse() {
    removeAllChildren();
    add(new EditorFile(null));
    model.nodeStructureChanged(this);
  }

  @Override
  public String toString() {
    final String n = file.name();
    return n.isEmpty() ? file.file().getAbsolutePath() : n;
  }
}
