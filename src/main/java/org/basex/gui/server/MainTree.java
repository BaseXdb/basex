package org.basex.gui.server;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.basex.core.BaseXException;
import org.basex.core.cmd.List;
import org.basex.gui.layout.BaseXLayout;
import org.basex.server.ClientSession;
import org.basex.util.Table;
import org.basex.util.Token;
import org.basex.util.TokenList;

/**
 * Tree on the left side of the GUI.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */
public final class MainTree {
  /** JTree. */
  private JTree mtree;
  /** ClientSession. */
  private ClientSession client;

  /**
   * Default constructor.
   * @param t root title
   * @param cs clientSession
   * @throws BaseXException database exception
   */
  public MainTree(final String t, final ClientSession cs)
    throws BaseXException {
    client = cs;
    init(t);
  }

  /**
   * Initializes the tree.
   * @param t root title
   * @throws BaseXException database exception
   */
  private void init(final String t) throws BaseXException {
    TreeNode server = new TreeNode(t, 1, 0);
    TreeNode dbs = new TreeNode("Databases", 0, 0);
    final Table dbnames = new Table(client.execute(new List()));
    for(final TokenList l : dbnames.contents) {
      TreeNode db = new TreeNode(Token.string(l.get(0)), 2, 0);
      db.add(new TreeNode("Content", 3, 1));
      db.add(new TreeNode("Users", 4, 1));
      db.add(new TreeNode("Properties", 5, 1));
      dbs.add(db);
    }
    server.add(dbs);
    TreeNode security = new TreeNode("Security", 0, 0);
    security.add(new TreeNode("Logins", 6, 1));
    server.add(security);
    TreeNode management = new TreeNode("Management", 0, 0);
    TreeNode logs = new TreeNode("Server Logs", 0, 0);
    logs.add(new TreeNode("Current", 7, 1));
    management.add(logs);
    management.add(new TreeNode("Active Sessions", 8, 1));
    server.add(management);
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
        ImageIcon ico = node.getIcon();
        if(ico != null) setIcon(ico);
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
  public final class TreeNode extends DefaultMutableTreeNode {
    /** Flag for icontype. */
    private final int icon;
    /** Flag for type. */
    private final int type;

    /**
     * Standard constructor.
     * @param s name
     * @param i icon
     * @param t type
     */
    TreeNode(final String s, final int i, final int t) {
      setUserObject(s);
      icon = i;
      type = t;
    }

    /**
     * Getter for the type.
     * @return value of type
     */
    public int getType() {
      return type;
    }

    /**
     * Getter for the icon.
     * @return value of icon
     */
    public ImageIcon getIcon() {
      if(icon == 1) {
        return BaseXLayout.icon("server_database");
      } else if(icon == 2) {
        return BaseXLayout.icon("database");
      } else if(icon == 3) {
        return BaseXLayout.icon("cmd-showtext");
      } else if(icon == 4) {
        return BaseXLayout.icon("user");
      } else if(icon == 5) {
        return BaseXLayout.icon("cmd-showinfo");
      } else if(icon == 6) {
        return BaseXLayout.icon("logins");
      } else if(icon == 7) {
        return BaseXLayout.icon("page");
      } else if(icon == 8) {
        return BaseXLayout.icon("active");
      }
      return null;
    }
  }
}
