package org.basex.gui.layout;

import javax.swing.*;
import javax.swing.tree.*;

import org.basex.gui.listener.*;

/**
 * Project specific tree implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class BaseXTree extends JTree {
  /**
   * Constructor.
   * @param win reference to the main window
   * @param root root node
   */
  public BaseXTree(final BaseXWindow win, final DefaultMutableTreeNode root) {
    super(root);
    BaseXLayout.addInteraction(this, win);
    setLargeModel(true);
    addMouseListener((MouseClickedListener) e -> {
      if(!e.isShiftDown()) setSelectionRow(getClosestRowForLocation(e.getX(), e.getY()));
    });
  }

  /**
   * Sets the label borders.
   * @param t top distance
   * @param l left distance
   * @param b bottom distance
   * @param r right distance
   * @return self reference
   */
  public final BaseXTree border(final int t, final int l, final int b, final int r) {
    setBorder(BaseXLayout.border(t, l, b, r));
    return this;
  }
}
