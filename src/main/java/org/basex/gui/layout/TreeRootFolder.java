package org.basex.gui.layout;

import javax.swing.JTree;

import org.basex.data.Data;

/**
 * JTree node which represents the root folder. The root folder carries
 * the name of the database and has basically an artificial path ('/')
 * which leads to problems if treated like an ordinary folder node.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public class TreeRootFolder extends TreeFolder {
  /**
   * Constructor.
   * @param nm name
   * @param pth path
   * @param jtree JTree
   * @param d Data
   */
  public TreeRootFolder(final byte[] nm, final byte[] pth, final JTree jtree,
      final Data d) {
    super(nm, pth, jtree, d);
  }

  @Override
  byte[] subfolder() {
    return path;
  }
}