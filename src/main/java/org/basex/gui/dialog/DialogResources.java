package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Token.*;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.TreeNode;

/**
 * Combination of a JTree and a text field. The tree visualizes the database
 * content including raw files and documents. The search field allows to
 * quickly access specific files/documents.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public class DialogResources extends BaseXBack {
  /** Search text field. */
  final BaseXTextField filterText;
  /** Database/root node. */
  final TreeFolder root;
  /** Dialog reference. */
  final BaseXDialog dialog;
  /** Resource tree. */
  final BaseXTree tree;
  /** Filter button. */
  final BaseXButton filter;
  /** Clear button. */
  final BaseXButton clear;

  /**
   * Constructor.
   * @param dp dialog reference
   */
  DialogResources(final DialogProps dp) {
    setLayout(new BorderLayout(0, 5));
    dialog = dp;

    // init tree - additional root node necessary to bypass
    // the egg/chicken dilemma
    final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
    tree = new BaseXTree(rootNode, dp).border(4, 4, 4, 4);
    tree.setRootVisible(false);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    final ImageIcon xml = BaseXLayout.icon("file_xml");
    final ImageIcon raw = BaseXLayout.icon("file_raw");
    tree.setCellRenderer(new TreeNodeRenderer(xml, raw));
    tree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(final TreeSelectionEvent e) {
        TreeNode n = (TreeNode) e.getPath().getLastPathComponent();
        String filt = n.equals(root) ? "" : n.path();
        String trgt = filt + '/';

        if(n.isLeaf()) {
          n = (TreeNode) n.getParent();
          trgt = (n == null || n.equals(root) ? "" : n.path()) + '/';
        } else {
          filt = trgt;
        }
        filterText.setText(filt);
        dp.add.target.setText(trgt);
      }
    });

    // add default children to tree
    final Data data = dp.gui.context.data();
    final String label = data.meta.name + " (/)";
    root = new TreeRootFolder(token(label), token("/"), tree, data);
    ((DefaultTreeModel) tree.getModel()).insertNodeInto(root, rootNode, 0);

    filter = new BaseXButton(FILTER, dp);
    clear = new BaseXButton(CLEAR, dp);
    filter.setEnabled(false);
    clear.setEnabled(false);

    // popup menu for node interaction
    new BaseXPopup(tree, dp.gui, new DeleteCmd(), new RenameCmd());

    // button panel
    final BaseXBack buttons = new BaseXBack();
    buttons.add(filter);
    buttons.add(clear);
    final BaseXBack btn = new BaseXBack().layout(new BorderLayout());
    btn.add(buttons, BorderLayout.EAST);

    filterText = new BaseXTextField(PLEASE_WAIT_D, dp);
    filterText.setEnabled(false);
    BaseXLayout.setWidth(filterText, 250);

    // left panel
    final BaseXBack panel = new BaseXBack(new BorderLayout());
    panel.add(filterText, BorderLayout.CENTER);
    panel.add(btn, BorderLayout.SOUTH);

    final JScrollPane sp = new JScrollPane(tree);
    BaseXLayout.setWidth(sp, 250);
    add(sp, BorderLayout.CENTER);
    add(panel, BorderLayout.SOUTH);

    new Thread() {
      @Override
      public void run() {
        tree.setCursor(CURSORWAIT);
        tree.expandPath(new TreePath(root.getPath()));
        filterText.setText("/");
        filterText.setEnabled(true);
        tree.setCursor(CURSORARROW);
        filter.setEnabled(true);
        clear.setEnabled(true);
      }
    }.start();
  }

  /**
   * Returns the current tree node selection.
   * @return selected node
   */
  TreeNode selection() {
    final TreePath t = tree.getSelectionPath();
    return t == null ? null : (TreeNode) t.getLastPathComponent();
  }

  /**
   * Refreshes the given folder node. Removes all its children and reloads
   * it afterwards.
   * @param n folder
   */
  private void refreshFolder(final TreeFolder n) {
    if(n == null) return;
    n.removeChildren();
    final TreePath path = new TreePath(n.getPath());
    tree.collapsePath(path);
    tree.expandPath(path);
  }

  /**
   * Reacts on user input.
   * @param comp the action component
   */
  void action(final Object comp) {
    if(comp == filter) {
      filter();
    } else if(comp == clear) {
      filterText.setText("/");
      filterText.requestFocus();
      refreshFolder(root);
    }
  }

  /**
   * Searches the tree for nodes that match the given search text.
   */
  private void filter() {
    final byte[] filterPath = TreeNode.preparePath(token(filterText.getText()));
    if(eq(filterPath, SLASH)) {
      refreshFolder(root);
      return;
    }

    final Data data = dialog.gui.context.data();
    // clear tree to append filtered nodes
    root.removeAllChildren();

    int cmax = TreeFolder.MAXC;
    // check if there's a directory
    // create a folder if there's either a raw or document folder
    if(data.resources.isDir(filterPath)) {
      root.add(new TreeFolder(TreeFolder.name(filterPath),
          TreeFolder.path(filterPath), tree, data));
      cmax--;
    }

    // now add the actual files (if there are any)
    final byte[] name = TreeFolder.name(filterPath);
    final byte[] sub = TreeFolder.path(filterPath);
    final TreeFolder f = new TreeFolder(TreeFolder.name(sub), TreeFolder.path(sub),
        tree, data);
    cmax = f.addLeaves(name, cmax, root);

    // add dummy node if maximum number of nodes is exceeded
    if(cmax <= 0)
      root.add(new TreeLeaf(token("..."), sub, false, true, tree, data));

    ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(root);
  }

  /**
   * Expands the tree after a node with the given path has been inserted.
   * Due to lazy evaluation of the tree inserted documents/files are only
   * added to the tree after the parent folder has been reloaded.
   * @param p path of new node
   */
  void refreshNewFolder(final String p) {
    final byte[][] pathComp = split(token(p), '/');

    TreeNode n = root;
    for(final byte[] c : pathComp) {
      // make sure folder is reloaded
      if(n instanceof TreeFolder)
        ((TreeFolder) n).reload();

      // find next child to continue with
      for(int i = 0; i < n.getChildCount(); i++) {
        final TreeNode ch = (TreeNode) n.getChildAt(i);
        if(eq(ch.name, c)) {
          // continue with the child if path component matches
          n = ch;
          break;
        }
      }
    }

    refreshFolder(n instanceof TreeFolder ? (TreeFolder) n : (TreeFolder) n.getParent());
  }

  /**
   * Custom tree cell renderer to distinguish between raw and xml leaf nodes.
   * @author BaseX Team 2005-12, BSD License
   * @author Lukas Kircher
   */
  private static final class TreeNodeRenderer extends DefaultTreeCellRenderer {
    /** Icon for xml files. */
    private final Icon xmlIcon;
    /** Icon for raw files. */
    private final Icon rawIcon;

    /**
     * Constructor.
     * @param xml xml icon
     * @param raw raw icon
     */
    TreeNodeRenderer(final Icon xml, final Icon raw) {
      xmlIcon = xml;
      rawIcon = raw;
    }

    @Override
    public Component getTreeCellRendererComponent(final JTree tree,
        final Object val, final boolean sel, final boolean exp,
        final boolean leaf, final int row, final boolean focus) {

      super.getTreeCellRendererComponent(tree, val, sel, exp, leaf, row, focus);
      if(leaf) {
        final TreeLeaf l = (TreeLeaf) val;
        setIcon(l.raw ? rawIcon : l.abbr ? null : xmlIcon);
      }
      return this;
    }
  }

  /** GUI commands for popup menu. */
  abstract static class BaseCmd implements GUICommand {
    @Override
    public boolean checked() {
      return false;
    }
    @Override
    public String help() {
      return null;
    }
    @Override
    public String key() {
      return null;
    }
  }

  /** Delete command. */
  final class DeleteCmd extends BaseCmd {
    @Override
    public void execute(final GUI g) {
      final TreeNode n = selection();
      if(n == null || !BaseXDialog.confirm(dialog.gui, DELETE_NODES)) return;

      final Runnable run = new Runnable() {
        @Override
        public void run() {
          refreshNewFolder(n.path());
        }
      };
      DialogProgress.execute(dialog, run, new Delete(n.path()));
    }

    @Override
    public String label() {
      return DELETE + DOTS;
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      final TreeNode n = selection();
      button.setEnabled(n != null && !n.equals(root));
    }
  }

  /** Rename command. */
  final class RenameCmd extends BaseCmd {
    @Override
    public void execute(final GUI g) {
      final TreeNode n = selection();
      if(n == null) return;

      final DialogInput d = new DialogInput(n.path(), RENAME, dialog, 0);
      if(!d.ok()) return;

      final String p = string(TreeNode.preparePath(token(d.input())));
      final Runnable run = new Runnable() {
        @Override
        public void run() {
          refreshNewFolder(p);
        }
      };
      DialogProgress.execute(dialog, run, new Rename(n.path(), p));
    }

    @Override
    public String label() {
      return RENAME + DOTS;
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      final TreeNode n = selection();
      button.setEnabled(n != null && !n.equals(root));
    }
  }
}
