package org.basex.gui.layout;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.basex.core.Text;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.Delete;
import org.basex.core.cmd.Rename;
import org.basex.core.cmd.Store;
import org.basex.data.Data;
import org.basex.gui.GUI;
import org.basex.gui.GUICommand;
import org.basex.gui.dialog.Dialog;
import org.basex.gui.dialog.DialogParsing;
import org.basex.gui.dialog.DialogProgress;
import org.basex.io.IOFile;

/**
 * Combination of a JTree and a text field. The tree visualizes the database
 * content including raw files and documents. The search field allows to
 * quickly access specific files/documents.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public class BaseXManageDBContent extends BaseXPanel {
  /** The JTree. */
  final JTree tree;
  /** Search text field. */
  final BaseXTextField searchTxt;
  /** Button to trigger the search. */
  final BaseXButton filterBtn;
  /** Button to clear the search. */
  final BaseXButton clearBtn;
  /** JTree database/root node. */
  final TreeFolder dbnode;
  /** Dialog reference. */
  final Dialog dialog;
  /** XML options panel. */
  final BaseXAddOptions addOptions;
  /** Parsing options. */
  final DialogParsing parsing;

  /**
   * Constructor.
   * @param d dialog reference
   */
  public BaseXManageDBContent(final Dialog d) {
    super(d);
    dialog = d;
    this.setLayout(new BorderLayout(10, 10));
    searchTxt = new BaseXTextField("", gui);

    /* init tree - additional root node necessary to bypass
     * the egg/chicken dilemma
     */
    final DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    tree = new JTree(root);
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
        if(n.equals(dbnode)) return;
        searchTxt.setText(n.completePath());
      }
    });

    // add default children to tree
    final Data data = gui.context.data();
    dbnode = new TreeRootFolder(token(data.meta.name), token("/"), tree, data);
    ((DefaultTreeModel) tree.getModel()).insertNodeInto(dbnode, root, 0);
    tree.expandPath(new TreePath(dbnode.getPath()));

    filterBtn = new BaseXButton(BUTTONFILTER, d);
    clearBtn = new BaseXButton(BUTTONCLEAR, d);
    final JScrollPane treepane = new JScrollPane(tree);

    filterBtn.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        filter();
      }
    });
    clearBtn.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        refreshFolder(dbnode);
        searchTxt.setSelectionStart(0);
        searchTxt.setSelectionEnd(searchTxt.getText().length());
        searchTxt.requestFocus();
      }
    });

    // popup menu for node interaction
    new BaseXPopup(tree, new GUICommand[]
        { new DelCmd(), new RenCmd(), new AddXMLCmd(), new AddBinCmd() },
        gui);

    // *** Layout ***
    final BaseXBack targetPnl = new BaseXBack(new BorderLayout(5, 5));
    targetPnl.add(new BaseXLabel(FILTERPATH, false, true),
        BorderLayout.NORTH);
    targetPnl.add(searchTxt, BorderLayout.CENTER);
    final BaseXBack btnPnl = new BaseXBack(new FlowLayout());
    btnPnl.add(filterBtn);
    btnPnl.add(clearBtn);

    // left panel
    final BaseXBack left = new BaseXBack(new BorderLayout());
    left.setBorder(new CompoundBorder(new EtchedBorder(),
        new EmptyBorder(10, 10, 10, 10)));
    final BaseXBack btn1 = new BaseXBack(new BorderLayout(5, 5));
    btn1.add(targetPnl, BorderLayout.NORTH);
    btn1.add(btnPnl, BorderLayout.CENTER);
    left.add(btn1, BorderLayout.NORTH);
    left.add(treepane, BorderLayout.CENTER);

    // XML options
    final BaseXBack addPnl = new BaseXBack(new TableLayout(2, 1, 13, 0));
    addOptions = new BaseXAddOptions(d);
    parsing = new DialogParsing(dialog);
    final BaseXTabs addTabs = new BaseXTabs(dialog);
    addTabs.addTab(GENERALINFO, addOptions);
    addTabs.addTab(PARSEINFO, parsing);
    addPnl.add(new BaseXLabel(" "));
    addPnl.add(addTabs);

    // right panel
    final BaseXBack right = new BaseXBack(new TableLayout(2, 1));
    right.setBorder(new EmptyBorder(10, 10, 10, 10));
    final Font f = dialog.getFont();
    final BaseXLabel xmlOpt = new BaseXLabel(ADDXMLOPT, true, true);
    xmlOpt.setFont(f.deriveFont(f.getSize2D() + 7));
    right.add(xmlOpt);
    right.add(addPnl);

    this.add(left, BorderLayout.CENTER);
    this.add(right, BorderLayout.EAST);
  }

  /**
   * Returns the current tree node selection.
   * @return selected node
   */
  TreeNode selection() {
    final TreePath t = tree.getSelectionPath();
    if(t == null) return null;
    return (TreeNode) t.getLastPathComponent();
  }

  /**
   * Returns the content of the search field for realtime updates.
   * @return search field text
   */
  public String getSearchText() {
    return searchTxt.getText();
  }

  /**
   * Determines the target node. A target can either be selected via the
   * tree or via typing the path in the target text field.
   * @return target node
   */
  TreeNode target() {
    final String t = searchTxt.getText();
    final TreePath tp = tree.getSelectionPath();
    if(tp != null) {
      final TreeNode n = (TreeNode) tp.getLastPathComponent();
      if(t.equals(n.completePath())) return n;
    }

    return findNode(t);
  }

  /**
   * Finds the node for the given path if it exists.
   * @param path path
   * @return node if it exists
   */
  public TreeNode findNode(final String path) {
    final String p = string(TreeNode.preparePath(token(path)));
    return find(dbnode, p);
  }

  /**
   * Recursive function to find the node with the given path.
   * @param n current node
   * @param p path to look for
   * @return node if found
   */
  private TreeNode find(final TreeNode n, final String p) {
    if(n.completePath().equals(p)) return n;

    TreeNode found = null;
    for(@SuppressWarnings({"unchecked"})
    final Enumeration<TreeNode> e = n.children(); e.hasMoreElements();) {
      if(found != null) return found;
      final TreeNode ch = e.nextElement();
      found = find(ch, p);
    }
    return found;
  }

  /**
   * Searches the tree for nodes that match the given search text.
   */
  void filter() {
    final byte[] path = TreeNode.preparePath(token(searchTxt.getText()));

    if(eq(path, SLASH)) {
      refreshFolder(dbnode);
      return;
    }
    final Data data = gui.context.data();
    // clear tree to append filtered nodes
    dbnode.removeAllChildren();

    // check if there's a folder
    final boolean documentFolder = data.docindex().isDocumentFolder(path);
    final IOFile fol = new IOFile(data.meta.binaries().path() + string(path));
    final boolean rawFolder = fol.exists() && fol.children().length > 0;
    // create a folder if there's either a raw or document folder
    if(documentFolder || rawFolder) {
      final TreeFolder t = new TreeFolder(TreeFolder.name(path),
          TreeFolder.path(path), tree, data);
      dbnode.add(t);
    }

    // now add the actual files (if there are any)
    final byte[] name = TreeFolder.name(path);
    final byte[] sub = TreeFolder.path(path);
    final TreeLeaf[] leafs = TreeFolder.leafsForPath(
        new TreeFolder(TreeFolder.name(sub), TreeFolder.path(sub), tree, data));
    for(final TreeLeaf l : leafs)
      if(name.length == 0 || eq(l.n, name)) {
        dbnode.add(l);
      }

    ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(dbnode);
  }

  /**
   * Refreshes the given folder node. Removes all its children and reloads
   * it afterwards.
   * @param n folder
   */
  public void refreshFolder(final TreeFolder n) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        n.removeChildren();
        tree.collapsePath(new TreePath(n.getPath()));
        tree.expandPath(new TreePath(n.getPath()));
      }
    });
  }

  /**
   * Expands the tree after a node with the given path has been inserted.
   * Due to lazy evaluation of the tree inserted documents/files are only
   * added to the tree after the parent folder has been loaded.
   * @param p path of new node
   * @param start node to start with
   * @return folder to search for
   */
  public TreeFolder retrieveNewFolder(final String p, final TreeNode start) {
    final byte[][] pathComp = split(token(p), '/');

    TreeNode n = start;
    for(final byte[] c : pathComp) {
      // make sure folder is reloaded
      if(n instanceof TreeFolder)
        ((TreeFolder) n).reload();

      // find next child to continue with
      for(int i = 0; i < n.getChildCount(); i++) {
        final TreeNode ch = (TreeNode) n.getChildAt(i);
        if(eq(ch.n, c)) {
          // continue with the child if path component matches
          n = ch;
          break;
        }
      }
    }

    return n instanceof TreeFolder ? (TreeFolder) n :
      (TreeFolder) n.getParent();
  }

  /**
   * Custom tree cell renderer to distinguish between raw and xml leaf nodes.
   * @author BaseX Team 2005-11, BSD License
   * @author Lukas Kircher
   */
  class TreeNodeRenderer extends DefaultTreeCellRenderer {
    /** Icon for xml files. */
    final Icon xmlIcon;
    /** Icon for raw files. */
    final Icon rawIcon;

    /**
     * Constructor.
     * @param xml xml icon
     * @param raw raw icon
     */
    public TreeNodeRenderer(final Icon xml, final Icon raw) {
      xmlIcon = xml;
      rawIcon = raw;
    }

    @Override
    @SuppressWarnings("hiding")
    public Component getTreeCellRendererComponent(final JTree tree,
        final Object value, final boolean sel, final boolean expanded,
        final boolean leaf, final int row, final boolean hasFocus) {
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row,
          hasFocus);

      if(leaf) {
        final TreeLeaf l = (TreeLeaf) value;
        setIcon(l.r ? rawIcon : xmlIcon);
      }
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
  class DelCmd extends BaseCmd {

    @Override
    public void execute(final GUI g) {
      final TreeNode n = selection();
      if(n == null) return;

      if(!Dialog.confirm(gui, Text.DELETECONF)) return;
      DialogProgress.execute(dialog, "", new Delete(n.completePath()));

      // refresh tree
      final TreeFolder par = (TreeFolder) n.getParent();
      if(par != null) refreshFolder(par);
    }

    @Override
    public String label() {
      return BUTTONDELETE + DOTS;
    }

    @SuppressWarnings("hiding")
    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      final TreeNode n = selection();
      if(n == null || n.equals(dbnode)) {
        button.setEnabled(false);
        return;
      }
      button.setEnabled(true);
    }
  }

  /** Rename cmd. */
  class RenCmd extends BaseCmd {

    @Override
    public void execute(final GUI g) {
      final TreeNode n = selection();
      if(n == null) return;

      InputDialog d = new InputDialog(gui, RENAMENODE, n.completePath(),
          RENAMEAS);
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

    @SuppressWarnings("hiding")
    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      final TreeNode n = selection();
      if(n == null || n.equals(dbnode) || n instanceof TreeFolder) {
        button.setEnabled(false);
        return;
      }
      button.setEnabled(true);
    }
  }

  /** Add binaries cmd. */
  class AddBinCmd extends BaseCmd {

    @Override
    public void execute(final GUI g) {
      // add can only be called on folders ...
      final TreeFolder n = (TreeFolder) selection();
      if(n == null) return;

      final TargetDialog d = new TargetDialog(gui, ADDBIN,
          n.equals(dbnode) ? "" : n.completePath());
      if(!d.ok()) return;
      DialogProgress.execute(dialog, "", new Store(d.target(), d.input()));

      // refresh tree
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          if(!d.target().equals(n.completePath()))
            refreshFolder(retrieveNewFolder(d.target(), n));
          else
            refreshFolder(n);
        }
      });
    }

    @Override
    public String label() {
      return ADDBIN + DOTS;
    }

    @SuppressWarnings("hiding")
    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      final TreeNode n = selection();
      if(n == null || n instanceof TreeLeaf) {
        button.setEnabled(false);
        return;
      }
      button.setEnabled(true);
    }
  }

  /** Add XML cmd. */
  class AddXMLCmd extends BaseCmd {

    @Override
    public void execute(final GUI g) {
      // add can only be called on folders ...
      final TreeFolder n = (TreeFolder) selection();
      if(n == null) return;

      final InputDialog d = new InputDialog(gui, ADDXML,
          n.equals(dbnode) ? "" : n.completePath(),
          CREATETARGET);
      if(!d.ok()) return;
      parsing.setOptions();
      addOptions.setOptions();
      DialogProgress.execute(dialog, "", new Add(d.input(),
          addOptions.getInput()));

      // refresh tree
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          if(!d.input().equals(n.completePath()))
            refreshFolder(retrieveNewFolder(d.input(), n));
          else
            refreshFolder(n);
        }
      });
    }

    @Override
    public String label() {
      return ADDXML + DOTS;
    }

    @SuppressWarnings("hiding")
    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      final TreeNode n = selection();
      if(n == null || n instanceof TreeLeaf) {
        button.setEnabled(false);
        return;
      }
      button.setEnabled(true);
    }
  }

  /** Rename dialog. */
  class InputDialog extends Dialog {
    /** New name. */
    final BaseXTextField in;
    /** Ok + cancel buttons. */
    final BaseXBack buttons;

    /**
     * Constructor.
     * @param main gui
     * @param title title
     * @param name current name of node
     * @param lblText text for label
     */
    protected InputDialog(final GUI main, final String title,
        final String name, final String lblText) {
      super(main, title);

      final BaseXBack p = new BaseXBack(new TableLayout(2, 1));
      p.setBorder(new EmptyBorder(8, 8, 8, 8));
      in = new BaseXTextField(gui);
      in.setText(name);
      in.setSelectionStart(0);
      in.setSelectionEnd(in.getText().length());
      p.add(new BaseXLabel(lblText, true, true));
      p.add(in);
      add(p, BorderLayout.NORTH);
      buttons = okCancel(this);
      set(buttons, BorderLayout.SOUTH);

      action(null);
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

  /** Add binary dialog. */
  class TargetDialog extends Dialog {
    /** Input field. */
    final BaseXTextField in;
    /** Target field. */
    final BaseXTextField trg;
    /** Ok / cancel buttons. */
    final BaseXBack buttons;

    /**
     * Constructor.
     * @param main gui
     * @param title lable title
     * @param target initial target string
     */
    protected TargetDialog(final GUI main, final String title,
        final String target) {
      super(main, title);

      final BaseXBack p = new BaseXBack(new TableLayout(5, 1));
      p.setBorder(new EmptyBorder(8, 8, 8, 8));
      in = new BaseXTextField(gui);
      trg = new BaseXTextField(gui);
      trg.setText(target);
      trg.setSelectionStart(0);
      trg.setSelectionEnd(trg.getText().length());
      p.add(new BaseXLabel(CREATETARGET, true, true));
      p.add(trg);
      p.add(new BaseXLabel(INPUTSRC, true, true).border(8, 0, 0, 0));
      p.add(in);
      add(p, BorderLayout.NORTH);
      buttons = okCancel(this);
      set(buttons, BorderLayout.SOUTH);

      action(null);
      finish(null);
    }

    /**
     * Returns the target path string.
     * @return target path
     */
    public String target() {
      return trg.getText().trim();
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