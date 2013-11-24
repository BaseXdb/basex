package org.basex.gui.view.editor;

import static org.basex.core.Text.*;
import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Project file tree.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class EditorTree extends BaseXPanel implements TreeWillExpandListener {
  /** Root node. */
  final DefaultMutableTreeNode root;
  /** Root paths. */
  final StringList roots;
  /** Tree. */
  final BaseXTree tree;
  /** Editor view. */
  final EditorView view;

  /**
   * Constructor.
   * @param ev editor view
   */
  EditorTree(final EditorView ev) {
    super(ev.gui);
    view = ev;
    setLayout(new BorderLayout());

    root = new DefaultMutableTreeNode();
    tree = new BaseXTree(root, gui).border(4, 4, 4, 4);
    tree.setCellRenderer(new EditorNodeRenderer());
    tree.addTreeWillExpandListener(this);
    tree.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(REFRESH.is(e)) {
          refresh();
        } else if(DELNEXT.is(e)) {
          delete();
        }
      }
      @Override
      public void keyTyped(final KeyEvent e) {
        if(ENTER.is(e)) open();
      }
    });
    tree.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(final MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) open();
      }
    });

    // choose common parent directories of project folders
    roots = roots();

    // expand root and child directories
    init();
    for(int c = 0; c <= roots.size(); c++) tree.expandRow(c);
    tree.setRootVisible(false);
    tree.setSelectionRow(0);
    add(new JScrollPane(tree), BorderLayout.CENTER);

    new BaseXPopup(tree, gui, new DeleteCmd(), null, new ChangeCmd());
  }

  /**
   * Initializes the tree structure.
   */
  private void init() {
    final DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
    root.removeAllChildren();
    for(final String s : roots) root.add(new EditorFolder(new IOFile(s), model));
  }

  /**
   * Returns the common parent directories of the project folders.
   * @return root directories
   */
  private StringList roots() {
    final GlobalOptions gopts = gui.context.globalopts;
    final File io1 = new File(gopts.get(GlobalOptions.REPOPATH));
    final File io2 = new File(gopts.get(GlobalOptions.WEBPATH));
    final File fl = new File(gopts.get(GlobalOptions.RESTXQPATH));
    final File io3 = fl.isAbsolute() ? fl : new File(io2, fl.getPath());
    final StringList sl = new StringList();
    for(final File f : new File[] { io1, io2, io3 }) {
      String p;
      try {
        p = f.getCanonicalFile().getParent();
      } catch(final IOException ex) {
        p = f.getParent();
      }
      if(!sl.contains(p)) sl.add(p);
    }
    return sl.sort(true).unique();
  }

  /**
   * Deletes selected files.
   */
  private void delete() {
    final ArrayList<EditorNode> selected = selected();
    if(BaseXDialog.confirm(gui, Util.info(DELETE_FILE_X, selected.size()))) {
      for(final EditorNode node : selected) view.delete(node.file);
      tree.setSelectionRow(0);
      refresh();
    }
  }

  /**
   * Opens the selected file.
   */
  private void open() {
    boolean opened = false;
    for(final EditorNode node : selected()) {
      final IOFile file = node.file;
      if(!file.isDir()) {
        view.open(file);
        opened = true;
      }
    }
    if(opened) view.getEditor().requestFocusInWindow();
  }

  /**
   * Refreshes the tree structure.
   */
  private void refresh() {
    boolean refreshed = false;
    for(final EditorNode node : selected()) {
      if(node.file.isDir()) {
        node.collapse();
        node.expand();
        refreshed = true;
      }
    }
    if(refreshed) repaint();
  }

  /**
   * Returns the selected nodes.
   * @return selected node
   */
  private ArrayList<EditorNode> selected() {
    final ArrayList<EditorNode> nodes = new ArrayList<EditorNode>();
    for(final TreePath tp : tree.getSelectionPaths()) {
      final Object node = tp.getLastPathComponent();
      if(node instanceof EditorNode) nodes.add((EditorNode) node);
    }
    return nodes;
  }

  @Override
  public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException {
    final Object node = event.getPath().getLastPathComponent();
    if(node instanceof EditorNode) ((EditorNode) node).expand();
  }

  @Override
  public void treeWillCollapse(final TreeExpansionEvent event) throws ExpandVetoException {
    final Object node = event.getPath().getLastPathComponent();
    if(node instanceof EditorNode) ((EditorNode) node).collapse();
  }

  /** Delete command. */
  final class DeleteCmd extends GUIBaseCmd {
    @Override
    public void execute(final GUI g) { delete(); }
    @Override
    public String label() { return DELETE + DOTS; }
    @Override
    public void refresh(final GUI g, final AbstractButton button) {
      boolean rt = false;
      for(final int r : tree.getSelectionRows()) rt |= r == 0;
      button.setEnabled(!rt);
    }
  }

  /** Delete command. */
  final class ChangeCmd extends GUIBaseCmd {
    @Override
    public void execute(final GUI g) {
      final EditorNode child = (EditorNode) root.getChildAt(0);
      final BaseXFileChooser fc = new BaseXFileChooser(CHOOSE_DIR, child.file.path(), gui);
      final IOFile io = fc.select(Mode.DOPEN);
      if(io != null) {
        tree.collapseRow(0);
        roots.set(0, io.path());
        root.removeAllChildren();
        init();
        for(int c = 0; c < roots.size(); c++) tree.expandRow(c);
      }
    }
    @Override
    public String label() { return "Directory" + DOTS; }
    @Override
    public void refresh(final GUI g, final AbstractButton button) {
      boolean rt = false;
      for(final int r : tree.getSelectionRows()) rt |= r == 0;
      button.setEnabled(rt);
    }
  }
}
