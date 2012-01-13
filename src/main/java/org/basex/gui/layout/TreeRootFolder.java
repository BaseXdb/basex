package org.basex.gui.layout;

import javax.swing.JTree;

import org.basex.data.Data;

/**
 * JTree node which represents the root folder. The root folder carries
 * the name of the database and has basically an artificial path ('/')
 * which leads to problems if treated like an ordinary folder node.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public class TreeRootFolder extends TreeFolder {

  /**
   * Constructor.
   * @param name name
   * @param path path
   * @param jtree JTree
   * @param data Data
   */
  TreeRootFolder(final byte[] name, final byte[] path, final JTree jtree,
      final Data data) {
    super(name, path, jtree, data);
  }

  @Override
  byte[] subfolder() {
    return p;
  }
}