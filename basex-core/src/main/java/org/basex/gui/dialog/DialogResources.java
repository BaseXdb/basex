package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Token.*;

import java.awt.*;

import javax.swing.*;
import javax.swing.tree.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * Combination of a JTree and a text field. The tree visualizes the database content including raw
 * files and documents. The search field allows to quickly access specific files/documents.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
final class DialogResources extends BaseXBack {
  /** Search text field. */
  private final BaseXTextField filterText;
  /** Database/root node. */
  private final ResourceFolder root;
  /** Dialog reference. */
  private final BaseXDialog dialog;
  /** Resource tree. */
  private final BaseXTree tree;
  /** Filter button. */
  private final BaseXButton filter;
  /** Clear button. */
  private final BaseXButton clear;

  /** Avoids superfluous filtering steps. */
  private boolean filtered;

  /**
   * Constructor.
   * @param dialog dialog reference
   */
  DialogResources(final DialogProps dialog) {
    setLayout(new BorderLayout(0, 5));
    this.dialog = dialog;

    // init tree - additional root node necessary to bypass
    // the egg/chicken dilemma
    final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
    tree = new BaseXTree(dialog, rootNode).border(4, 4, 4, 4);
    tree.setRootVisible(false);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.setRowHeight(getFontMetrics(getFont()).getHeight());
    tree.setCellRenderer(new TreeNodeRenderer());

    // add default children to tree
    final Context context = dialog.gui.context;
    final Data data = context.data();
    final String label = data.meta.name + " (/)";
    root = new ResourceRootFolder(token(label), token("/"), tree, context);
    ((DefaultTreeModel) tree.getModel()).insertNodeInto(root, rootNode, 0);

    filter = new BaseXButton(dialog, FILTER);
    clear = new BaseXButton(dialog, CLEAR);
    filter.setEnabled(false);
    clear.setEnabled(false);

    // popup menu for node interaction
    new BaseXPopup(tree, dialog.gui, new DeleteCmd(), new RenameCmd());

    // button panel
    final BaseXBack buttons = new BaseXBack();
    buttons.add(filter);
    buttons.add(clear);
    final BaseXBack btn = new BaseXBack().layout(new BorderLayout());
    btn.add(buttons, BorderLayout.EAST);

    filterText = new BaseXTextField(dialog, PLEASE_WAIT_D);
    filterText.setEnabled(false);
    BaseXLayout.setWidth(filterText, 300);

    // left panel
    final BaseXBack panel = new BaseXBack(new BorderLayout());
    panel.add(filterText, BorderLayout.CENTER);
    panel.add(btn, BorderLayout.SOUTH);

    final JScrollPane sp = new JScrollPane(tree);
    add(sp, BorderLayout.CENTER);
    add(panel, BorderLayout.SOUTH);

    tree.addTreeSelectionListener(e -> {
      ResourceNode n = (ResourceNode) e.getPath().getLastPathComponent();
      String filt = n.equals(root) ? "" : n.path();
      String trgt = filt + '/';

      if(n.isLeaf()) {
        n = (ResourceNode) n.getParent();
        trgt = (n == null || n.equals(root) ? "" : n.path()) + '/';
      } else {
        filt = trgt;
      }
      filterText.setText(filt);
      dialog.addPanel.target.setText(trgt);
      filtered = false;
    });

    new Thread(() -> {
      tree.setCursor(CURSORWAIT);
      tree.expandPath(new TreePath(root.getPath()));
      filterText.setText("/");
      filterText.setEnabled(true);
      tree.setCursor(CURSORARROW);
      filter.setEnabled(true);
      clear.setEnabled(true);
    }).start();
  }

  /**
   * Returns the current tree node selection.
   * @return selected node
   */
  private ResourceNode selection() {
    final TreePath t = tree.getSelectionPath();
    return t == null ? null : (ResourceNode) t.getLastPathComponent();
  }

  /**
   * Refreshes the given folder node. Removes all its children and reloads
   * it afterwards.
   * @param n folder
   */
  private void refreshFolder(final ResourceFolder n) {
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
    if(comp == filter && !filtered) {
      filter();
      filtered = true;
    } else if(comp == clear) {
      filterText.setText("/");
      filterText.requestFocusInWindow();
      refreshFolder(root);
      filtered = false;
    } else {
      tree.repaint();
    }
  }

  /**
   * Searches the tree for nodes that match the given search text.
   */
  private void filter() {
    final byte[] filterPath = ResourceNode.preparePath(token(filterText.getText()));
    if(eq(filterPath, SLASH)) {
      refreshFolder(root);
      return;
    }

    final Context context = dialog.gui.context;
    final Data data = context.data();
    // clear tree to append filtered nodes
    root.removeAllChildren();

    int cmax = ResourceFolder.MAXC;
    // check if there's a directory
    // create a folder if there's either a raw or document folder
    if(data.resources.isDir(filterPath)) {
      root.add(new ResourceFolder(ResourceFolder.name(filterPath), ResourceFolder.path(filterPath),
          tree, context));
      cmax--;
    }

    // now add the actual files (if there are any)
    final byte[] name = ResourceFolder.name(filterPath);
    final byte[] sub = ResourceFolder.path(filterPath);
    cmax = new ResourceFolder(ResourceFolder.name(sub), ResourceFolder.path(sub), tree, context).
        addLeaves(name, cmax, root);

    // add dummy node if maximum number of nodes is exceeded
    if(cmax <= 0) root.add(new ResourceLeaf(token(DOTS), sub, false, true, tree, context));

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

    ResourceNode n = root;
    for(final byte[] c : pathComp) {
      // make sure folder is reloaded
      if(n instanceof ResourceFolder)
        ((ResourceFolder) n).reload();

      // find next child to continue with
      for(int i = 0; i < n.getChildCount(); i++) {
        final ResourceNode ch = (ResourceNode) n.getChildAt(i);
        if(eq(ch.name, c)) {
          // continue with the child if path component matches
          n = ch;
          break;
        }
      }
    }

    refreshFolder((ResourceFolder) (n instanceof ResourceFolder ? n : n.getParent()));
  }

  /**
   * Custom tree cell renderer to distinguish between raw and xml leaf nodes.
   * @author BaseX Team 2005-21, BSD License
   * @author Lukas Kircher
   */
  private static final class TreeNodeRenderer extends DefaultTreeCellRenderer {
    @Override
    public Component getTreeCellRendererComponent(final JTree tree, final Object val,
        final boolean sel, final boolean exp, final boolean leaf, final int row,
        final boolean focus) {

      super.getTreeCellRendererComponent(tree, val, sel, exp, leaf, row, focus);
      if(leaf) setIcon(BaseXImages.text(((ResourceLeaf) val).raw));
      return this;
    }
  }

  /** Delete command. */
  private final class DeleteCmd extends GUIPopupCmd {
    /** Constructor. */
    DeleteCmd() { super(DELETE + DOTS, BaseXKeys.DELNEXT); }

    @Override
    public void execute() {
      final ResourceNode n = selection();
      if(n == null || !BaseXDialog.confirm(dialog.gui, DELETE_NODES)) return;

      final Runnable run = () -> refreshNewFolder(n.path());
      DialogProgress.execute(dialog, run, new Delete(n.path()));
    }

    @Override
    public boolean enabled(final GUI main) {
      final ResourceNode n = selection();
      return n instanceof ResourceLeaf ? !((ResourceLeaf) n).abbr : n != null && !n.equals(root);
    }
  }

  /** Rename command. */
  private final class RenameCmd extends GUIPopupCmd {
    /** Constructor. */
    RenameCmd() { super(RENAME + DOTS, BaseXKeys.RENAME); }

    @Override
    public void execute() {
      final ResourceNode n = selection();
      if(n == null) return;

      final DialogInput d = new DialogInput(n.path(), RENAME, dialog, 0);
      if(!d.ok()) return;

      final String p = string(ResourceNode.preparePath(token(d.input())));
      final Runnable run = () -> refreshNewFolder(p);
      DialogProgress.execute(dialog, run, new Rename(n.path(), p));
    }

    @Override
    public boolean enabled(final GUI main) {
      final ResourceNode n = selection();
      return n instanceof ResourceLeaf ? !((ResourceLeaf) n).abbr : n != null && !n.equals(root);
    }
  }
}
