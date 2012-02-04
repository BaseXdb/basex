package org.basex.gui.layout;

import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.*;
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
   * @param w window reference
   */
  public BaseXTree(final DefaultMutableTreeNode root, final Window w) {
    super(root);
    BaseXLayout.addInteraction(this, w);
    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(final MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e))
          setSelectionRow(getClosestRowForLocation(e.getX(), e.getY()));
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
    setBorder(new EmptyBorder(t, l, b, r));
    return this;
  }
}
