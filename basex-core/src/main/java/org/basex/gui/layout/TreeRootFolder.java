package org.basex.gui.layout;

import org.basex.data.*;

/**
 * JTree node which represents the root folder. The root folder carries
 * the name of the database and has basically an artificial path ('/')
 * which leads to problems if treated like an ordinary folder node.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public class TreeRootFolder extends TreeFolder {
  /**
   * Constructor.
   * @param nm name
   * @param pth path
   * @param bxt tree reference
   * @param d data reference
   */
  public TreeRootFolder(final byte[] nm, final byte[] pth, final BaseXTree bxt, final Data d) {
    super(nm, pth, bxt, d);
  }

  @Override
  byte[] subfolder() {
    return path;
  }
}