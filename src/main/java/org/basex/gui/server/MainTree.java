package org.basex.gui.server;

import java.awt.Component;

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
  private String serverTitle;

  /**
   * Standard constructor.
   * @param t root title
   */
  public MainTree(final String t) {
    serverTitle = t;
    init();
  }

  /**
   * Initializes the tree.
   */
  private void init() {
    TreeNode server = new TreeNode(serverTitle, 1);
    TreeNode dbs = new TreeNode("Databases", 0);
    TreeNode db1 = new TreeNode("Database 1", 2);
    TreeNode db2 = new TreeNode("Database 2", 2);
    db1.add(new TreeNode("Content", 3));
    db1.add(new TreeNode("Users", 4));
    db1.add(new TreeNode("Properties", 5));
    db2.add(new TreeNode("Content", 3));
    db2.add(new TreeNode("Users", 4));
    db2.add(new TreeNode("Properties", 5));
    dbs.add(db1);
    dbs.add(db2);
    server.add(dbs);
    mtree = new JTree(server);
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

        super.getTreeCellRendererComponent(tree, value, sel, expanded,
            leaf, row, hf);

        TreeNode node = (TreeNode) value;
        if(node.getType() == 1) {
          setIcon(BaseXLayout.icon("server_database"));
        } else if(node.getType() == 2) {
          setIcon(BaseXLayout.icon("database"));
        } else if(node.getType() == 3) {
          setIcon(BaseXLayout.icon("cmd-showtext"));
        } else if(node.getType() == 4) {
          setIcon(BaseXLayout.icon("user"));
        } else if(node.getType() == 5) {
          setIcon(BaseXLayout.icon("cmd-showinfo"));
        }
        return this;
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
