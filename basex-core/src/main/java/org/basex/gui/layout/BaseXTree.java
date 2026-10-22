package org.basex.gui.layout;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;

/**
 * Project specific tree implementation.
 *
 * @author BaseX Team, BSD License
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
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        // select the row of a click next to the node label
        if(!SwingUtilities.isLeftMouseButton(e) || e.isShiftDown() || BaseXKeys.sc(e)) return;
        final int row = row(e);
        if(row != -1) setSelectionRow(row);
      }

      @Override
      public void mousePressed(final MouseEvent e) {
        // select the row before a popup menu is shown
        if(!SwingUtilities.isRightMouseButton(e)) return;
        final int row = row(e);
        if(row != -1 && !isRowSelected(row)) setSelectionRow(row);
      }
    });
  }

  /**
   * Returns the row at the vertical position of a mouse event.
   * @param e mouse event
   * @return row or {@code -1}
   */
  private int row(final MouseEvent e) {
    final int row = getClosestRowForLocation(e.getX(), e.getY());
    final Rectangle bounds = getRowBounds(row);
    return bounds != null && e.getY() >= bounds.y && e.getY() < bounds.y + bounds.height ? row : -1;
  }

  /**
   * Sets the tree borders.
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
