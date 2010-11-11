package org.basex.gui.server;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.basex.gui.layout.BaseXLayout;

/**
 * Tree on the left side of the GUI.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */
public class MainTree {

  /** JTree. */
  private JTree mtree;
  /** Name of root. */
  private String rootTitle;
  
  /**
   * Standard constructor.
   * @param t root title
   */
  public MainTree(final String t) {
    rootTitle = t;
    init();
  }

  /**
   * Initializes the tree.
   */
  private void init() {
    TreeNode root = new TreeNode(rootTitle, 0);
    mtree = new JTree(root);
    setRenderer();
  }
  
  /**
   * Sets the renderer.
   */
  private void setRenderer() {
    // renderer for own icons
    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
      @Override
      public Component getTreeCellRendererComponent(final JTree tree,
          final Object value, final boolean sel, final boolean expanded,
          final boolean leaf, final int row, final boolean hf) {
        TreeNode node = (TreeNode) value;
        if(node.getType() == 0) {
          setLeafIcon(new ImageIcon(BaseXLayout.image("server_database")));
        }
        return super.getTreeCellRendererComponent(tree, value, sel, expanded,
            leaf, row, hf);
      }
    };
    mtree.setCellRenderer(renderer);
  }
  
  /**
   * Returns the jtree.
   * @return jtree
   */
  public JTree getTree() {
    return mtree;
  }
  
  
  /**
   * Inner class for treenode.
   */
  private final class TreeNode extends DefaultMutableTreeNode {
    /** Flag for nodetype. */
    private final int type;
    
    /**
     * Standard constructor.
     * @param t type
     * @param s name
     */
    TreeNode(final String s, final int t) {
      setUserObject(s);
      type = t;
    }
    
    /**
     * Getter for the type.
     * @return value of type
     */
    public int getType() {
      return type;
    }
  }
}
