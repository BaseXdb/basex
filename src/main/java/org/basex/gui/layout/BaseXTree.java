package org.basex.gui.layout;

import java.awt.Window;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Project specific tree implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class BaseXTree extends JTree {
  /**
   * Constructor.
   * @param root root node
   * @param win parent window
   */
  public BaseXTree(final DefaultMutableTreeNode root, final Window win) {
    super(root);
    BaseXLayout.addInteraction(this, win);
  }
}
