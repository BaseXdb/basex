package org.basex.gui.view.editor;

import javax.swing.tree.*;

import org.basex.io.*;

/**
 * Single tree node.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class EditorNode extends DefaultMutableTreeNode {
  /** Path. */
  final IOFile file;

  /**
   * Constructor.
   * @param io file reference
   */
  EditorNode(final IOFile io) {
    super(io == null ? null : io.name());
    file = io;
  }

  /**
   * Expands the current node.
   */
  abstract void expand();

  /**
   * Collapses the current node.
   */
  abstract void collapse();
}
