package org.basex.gui.layout;

import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
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
    final BaseXTree t = this;
    BaseXLayout.addInteraction(this, w);
    this.addMouseListener(new MouseListener() {
      @Override
      public void mouseReleased(final MouseEvent e) {
      }
      @Override
      public void mousePressed(final MouseEvent e) {
      }
      @Override
      public void mouseExited(final MouseEvent e) {
      }
      @Override
      public void mouseEntered(final MouseEvent e) {
      }
      @Override
      public void mouseClicked(final MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e))
          t.setSelectionRow(t.getClosestRowForLocation(e.getX(), e.getY()));
      }
    });
  }
}
