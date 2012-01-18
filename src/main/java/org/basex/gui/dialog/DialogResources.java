package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.basex.core.Text;
import org.basex.core.cmd.Delete;
import org.basex.core.cmd.Rename;
import org.basex.data.Data;
import org.basex.gui.GUI;
import org.basex.gui.GUICommand;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXPopup;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.BaseXTree;
import org.basex.gui.layout.TableLayout;
import org.basex.gui.layout.TreeFolder;
import org.basex.gui.layout.TreeLeaf;
import org.basex.gui.layout.TreeNode;
import org.basex.gui.layout.TreeRootFolder;
import org.basex.io.IOFile;

/**
 * Combination of a JTree and a text field. The tree visualizes the database
 * content including raw files and documents. The search field allows to
 * quickly access specific files/documents.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public class DialogResources extends BaseXBack {
  /** Resource tree. */
  final BaseXTree tree;
  /** Search text field. */
  final BaseXTextField filterText;
  /** Database/root node. */
  final TreeFolder dbnode;
  /** Dialog reference. */
  final DialogProps dialog;

  /** Filter button. */
  private final BaseXButton filter;
  /** Clear button. */
  private final BaseXButton clear;

  /**
   * Constructor.
   * @param d dialog reference
   */
  public DialogResources(final DialogProps d) {
    setLayout(new BorderLayout(0, 5));
    dialog = d;

    filterText = new BaseXTextField("", d);
    BaseXLayout.setWidth(filterText, 220);

    // init tree - additional root node necessary to bypass
    // the egg/chicken dilemma
    final DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    tree = new BaseXTree(root, d);
    tree.setBorder(new EmptyBorder(4, 4, 4, 4));
    tree.setRootVisible(false);
    tree.getSelectionModel().setSelectionMode(
        TreeSelectionModel.SINGLE_TREE_SELECTION);

    final ImageIcon xml = BaseXLayout.icon("file_xml");
    final ImageIcon raw = BaseXLayout.icon("file_raw");
    tree.setCellRenderer(new TreeNodeRenderer(xml, raw));
    tree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(final TreeSelectionEvent e) {
        final TreeNode n = (TreeNode) e.getPath().getLastPathComponent();
        String path = n.equals(dbnode) ? "" : n.completePath();
        if(!n.isLeaf()) {
          path += "/";
          dialog.add.target.setText(path);
        }
        filterText.setText(path);
      }
    });

    // add default children to tree
    final Data data = d.gui.context.data();
    dbnode = new TreeRootFolder(token(data.meta.name), token("/"), tree, data);
    ((DefaultTreeModel) tree.getModel()).insertNodeInto(dbnode, root, 0);
    tree.expandPath(new TreePath(dbnode.getPath()));

    filter = new BaseXButton(BUTTONFILTER, d);
    clear = new BaseXButton(BUTTONCLEAR, d);

    // popup menu for node interaction
    new BaseXPopup(tree, d.gui, new DeleteCmd(), new RenameCmd());

    // button panel
    final BaseXBack buttons = new BaseXBack(Fill.NONE);
    buttons.add(filter);
    buttons.add(clear);
    final BaseXBack btn = new BaseXBack(Fill.NONE).layout(new BorderLayout());
    btn.add(buttons, BorderLayout.EAST);

    // left panel
    final BaseXBack panel = new BaseXBack(new BorderLayout());
    panel.add(filterText, BorderLayout.CENTER);
    panel.add(btn, BorderLayout.SOUTH);

    add(new JScrollPane(tree), BorderLayout.CENTER);
    add(panel, BorderLayout.SOUTH);
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
   * Searches the tree for nodes that match the given search text.
   */
  void filter() {
    final byte[] path = TreeNode.preparePath(token(filterText.getText()));
    if(eq(path, SLASH)) {
      refreshFolder(dbnode);
      return;
    }

    final Data data = dialog.gui.context.data();
    // clear tree to append filtered nodes
    dbnode.removeAllChildren();

    // check if there's a folder
    final boolean documentFolder = data.docindex.isDir(path);
    final IOFile fol = data.meta.binary(string(path));
    final boolean rawFolder = fol.exists() && fol.children().length > 0;
    // create a folder if there's either a raw or document folder
    if(documentFolder || rawFolder) {
      dbnode.add(new TreeFolder(TreeFolder.name(path),
          TreeFolder.path(path), tree, data));
    }

    // now add the actual files (if there are any)
    final byte[] name = TreeFolder.name(path);
    final byte[] sub = TreeFolder.path(path);
    final TreeLeaf[] leaves = TreeFolder.leaves(
        new TreeFolder(TreeFolder.name(sub), TreeFolder.path(sub), tree, data));

    for(final TreeLeaf l : leaves) {
      if(name.length == 0 || eq(l.name, name)) dbnode.add(l);
    }

    ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(dbnode);
  }

  /**
   * Refreshes the given folder node. Removes all its children and reloads
   * it afterwards.
   * @param n folder
   */
  void refreshFolder(final TreeFolder n) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        n.removeChildren();
        final TreePath path = new TreePath(n.getPath());
        tree.collapsePath(path);
        tree.expandPath(path);
      }
    });
  }

  /**
   * Reacts on user input.
   * @param comp the action component
   */
  void action(final Object comp) {
    if(comp == filter) {
      filter();
    } else {
      refreshFolder(dbnode);
      if(comp == clear) filterText.requestFocus();
    }
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
      if(leaf) setIcon(((TreeLeaf) val).raw ? rawIcon : xmlIcon);
      return this;
    }
  }

  /** GUI Commands for popup menu. */
  abstract class BaseCmd implements GUICommand {
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

  /** Delete cmd. */
  final class DeleteCmd extends BaseCmd {
    @Override
    public void execute(final GUI g) {
      final TreeNode n = selection();
      if(n == null) return;

      if(!Dialog.confirm(dialog.gui, Text.DELETECONF)) return;
      DialogProgress.execute(dialog, "", new Delete(n.completePath()));

      // refresh tree
      final TreeFolder par = (TreeFolder) n.getParent();
      if(par != null) refreshFolder(par);
    }

    @Override
    public String label() {
      return BUTTONDELETE + DOTS;
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      final TreeNode n = selection();
      button.setEnabled(n != null && !n.equals(dbnode));
    }
  }

  /** Rename cmd. */
  final class RenameCmd extends BaseCmd {
    @Override
    public void execute(final GUI g) {
      final TreeNode n = selection();
      if(n == null) return;

      final InputDialog d = new InputDialog(BUTTONRENAME,
          n.completePath(), RENAMEAS);
      if(!d.ok()) return;

      DialogProgress.execute(dialog, "",
          new Rename(n.completePath(), d.input()));

      // refresh tree
      final TreeFolder par = (TreeFolder) n.getParent();
      if(par != null) refreshFolder(par);
    }

    @Override
    public String label() {
      return BUTTONRENAME;
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      final TreeNode n = selection();
      button.setEnabled(n != null && !n.equals(dbnode) &&
          !(n instanceof TreeFolder));
    }
  }

  /** Rename dialog. */
  private final class InputDialog extends Dialog {
    /** New name. */
    private final BaseXTextField in;

    /**
     * Constructor.
     * @param title title
     * @param name current name of node
     * @param lblText text for label
     */
    protected InputDialog(final String title, final String name,
        final String lblText) {

      super(dialog.gui, title);
      final BaseXBack p = new BaseXBack(new TableLayout(2, 1)).border(8);
      in = new BaseXTextField(name, this);
      p.add(new BaseXLabel(lblText, true, true));
      p.add(in);
      set(p, BorderLayout.NORTH);
      set(okCancel(), BorderLayout.SOUTH);
      finish(null);
    }

    /**
     * Returns the new name for the target.
     * @return name
     */
    String input() {
      return in.getText().trim();
    }
  }
}
