package org.basex.gui.layout;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;

/**
 * Project specific tree implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public class BaseXTree extends JTree {
  /**
   * Constructor.
   * @param root root node
   * @param w window reference
   */
  public BaseXTree(final DefaultMutableTreeNode root, final Window w) {
    super(root);
    BaseXLayout.addInteraction(this, w);
    setLargeModel(true);
    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(final MouseEvent e) {
        if(!e.isShiftDown()) setSelectionRow(getClosestRowForLocation(e.getX(), e.getY()));
      }
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
  public BaseXTree border(final int t, final int l, final int b, final int r) {
    setBorder(BaseXLayout.border(t, l, b, r));
    return this;
  }
}
