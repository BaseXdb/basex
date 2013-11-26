package org.basex.gui.view.editor;

import static org.basex.core.Text.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.Mode;
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
  /** Root node. */
  final EditorCellRenderer renderer;
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
    setLayout(new GridLayout(1, 1));

    root = new DefaultMutableTreeNode();
    tree = new BaseXTree(root, gui).border(4, 4, 4, 4);
    renderer = new EditorCellRenderer();
    tree.setExpandsSelectedPaths(true);
    tree.setCellRenderer(renderer);
    tree.addTreeWillExpandListener(this);
    tree.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(BaseXKeys.REFRESH.is(e)) {
          new RefreshCmd().execute(gui);
        } else if(BaseXKeys.DELNEXT.is(e)) {
          new DeleteCmd().execute(gui);
        } else if(BaseXKeys.NEWDIR.is(e)) {
          new NewCmd().execute(gui);
        }
      }
      @Override
      public void keyTyped(final KeyEvent e) {
        if(BaseXKeys.ENTER.is(e)) open();
      }
    });
    tree.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(final MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) open();
      }
    });

    tree.setCellEditor(new EditorCellEditor(tree, renderer));
    tree.setEditable(true);

    // choose common parent directories of project directories
    final StringList roots = roots();
    root.removeAllChildren();
    for(final String s : roots) root.add(new EditorDir(new IOFile(s), this));

    // expand root and child directories
    for(int c = 0; c <= roots.size(); c++) tree.expandRow(c);
    tree.setRootVisible(false);
    tree.setSelectionRow(0);

    // add scroll bar
    final JScrollPane jsp = new JScrollPane(tree);
    jsp.setBorder(new EmptyBorder(0, 0, 0, 0));
    add(jsp);

    new BaseXPopup(tree, gui, new DeleteCmd(), new RenameCmd(), new NewCmd(), null,
        new ChangeCmd(), new RefreshCmd());
  }

  @Override
  public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException {
    final Object obj = event.getPath().getLastPathComponent();
    if(obj instanceof EditorNode) {
      final EditorNode node = (EditorNode) obj;
      node.expand();
      node.updateTree();
    }
  }

  @Override
  public void treeWillCollapse(final TreeExpansionEvent event) throws ExpandVetoException {
    final Object obj = event.getPath().getLastPathComponent();
    if(obj instanceof EditorNode) {
      final EditorNode node = (EditorNode) obj;
      node.collapse();
      node.updateTree();
    }
  }

  /**
   * Renames a file or directory in the tree.
   * @param node source node
   * @param name new name of file or directory
   * @return new file reference, or {@code null} if operation failed
   */
  IOFile rename(final EditorNode node, final String name) {
    // check if chosen file name is valid
    if(IOFile.isValidName(name)) {
      final IOFile old = node.file;
      final IOFile updated = new IOFile(old.file().getParent(), name);
      // rename file or show error dialog
      if(!old.rename(updated)) {
        BaseXDialog.error(gui, Util.info(FILE_NOT_RENAMED_X, old));
      } else {
        // update tab references if file or directory could be renamed
        gui.editor.rename(old, updated);
        return updated;
      }
    }
    return null;
  }

  /**
   * Returns the common parent directories of the project directories.
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
   * Opens the selected file.
   */
  private void open() {
    boolean opened = false;
    for(final EditorNode node : selectedNodes()) {
      final IOFile file = node.file;
      if(!file.isDir()) {
        view.open(file);
        opened = true;
      }
    }
    if(opened) view.getEditor().requestFocusInWindow();
  }

  /**
   * Returns the selected nodes.
   * @return selected node
   */
  private ArrayList<EditorNode> selectedNodes() {
    final ArrayList<EditorNode> nodes = new ArrayList<EditorNode>();
    final TreePath[] paths = tree.getSelectionPaths();
    if(paths != null) {
      for(final TreePath path : paths) {
        final Object node = path.getLastPathComponent();
        if(node instanceof EditorNode) nodes.add((EditorNode) node);
      }
    }
    return nodes;
  }

  /**
   * Returns a single selected node, or {@code null} if zero or more than node is selected.
   * @return selected node
   */
  private EditorNode selectedNode() {
    final TreePath path = selectedPath();
    if(path != null) {
      final Object node = path.getLastPathComponent();
      if(node instanceof EditorNode) return (EditorNode) node;
    }
    return null;
  }

  /**
   * Returns the selected path, or returns {@code null} if zero or more than paths are selected.
   * @return path
   */
  private TreePath selectedPath() {
    final TreePath[] tps = tree.getSelectionPaths();
    return tps == null || tps.length > 1 ? null : tps[0];
  }

  // COMMANDS =====================================================================================

  /** Refresh command. */
  final class RefreshCmd extends GUIBaseCmd {
    @Override
    public void execute(final GUI main) { if(enabled(main)) selectedNode().refresh(); }
    @Override
    public boolean enabled(final GUI main) { return selectedNode() != null; }
    @Override
    public String label() { return REFRESH; }
    @Override
    public String key() { return BaseXKeys.REFRESH.toString(); }
  }

  /** New directory command. */
  final class NewCmd extends GUIBaseCmd {
    @Override
    public void execute(final GUI main) {
      if(!enabled(gui)) return;
      EditorNode parent = selectedNode();
      if(parent instanceof EditorFile) parent = (EditorDir) parent.getParent();

      // choose free name
      String name = '(' + NEW_DIR + ')';
      IOFile dir = new IOFile(parent.file, name);
      int c = 1;
      while(dir.exists()) {
        name = '(' + NEW_DIR + ' ' + ++c + ')';
        dir = new IOFile(parent.file, name);
      }
      if(dir.md()) {
        tree.setSelectionPaths(null);
        parent.refresh();
        final int cl = parent.getChildCount();
        for(int i = 0; i < cl; i++) {
          final EditorNode node = (EditorNode) parent.getChildAt(i);
          if(node.file.name().equals(name)) {
            final TreePath path = node.path();
            tree.setSelectionPath(path);
            tree.startEditingAtPath(path);
            break;
          }
        }
      }
    }
    @Override
    public boolean enabled(final GUI main) { return selectedNode() != null; }
    @Override
    public String label() { return NEW_DIR; }
    @Override
    public String key() { return "% shift N"; }
  }

  /** Delete command. */
  final class DeleteCmd extends GUIBaseCmd {
    @Override
    public void execute(final GUI main) {
      if(!enabled(gui)) return;

      final EditorNode node = selectedNode();
      if(BaseXDialog.confirm(gui, Util.info(DELETE_FILE_X, node.file))) {
        final EditorNode parent = (EditorNode) node.getParent();
        // delete file or show error dialog
        if(!view.delete(node.file)) {
          BaseXDialog.error(gui, Util.info(FILE_NOT_DELETED_X, node.file));
        } else {
          parent.refresh();
          tree.setSelectionPath(parent.path());
        }
      }
    }
    @Override
    public boolean enabled(final GUI main) {
      final EditorNode node = selectedNode();
      return node != null && !node.root();
    }
    @Override
    public String label() { return DELETE + DOTS; }
    @Override
    public String key() { return "DELETE"; }
  }

  /** Rename command. */
  final class RenameCmd extends GUIBaseCmd {
    @Override
    public void execute(final GUI main) {
      if(!enabled(gui)) return;
      tree.startEditingAtPath(selectedNode().path());
    }
    @Override
    public boolean enabled(final GUI main) {
      final EditorNode node = selectedNode();
      return node != null && !node.root();
    }
    @Override
    public String label() { return RENAME; }
    @Override
    public String key() { return "F2"; }
  }

  /** Delete command. */
  final class ChangeCmd extends GUIBaseCmd {
    @Override
    public void execute(final GUI main) {
      if(!enabled(gui)) return;
      final EditorNode child = selectedNode();
      final BaseXFileChooser fc = new BaseXFileChooser(CHOOSE_DIR, child.file.path(), gui);
      final IOFile io = fc.select(Mode.DOPEN);
      if(io != null) {
        child.file = io;
        child.refresh();
      }
    }
    @Override
    public boolean enabled(final GUI main) {
      final EditorNode node = selectedNode();
      return node != null && node.root();
    }
    @Override
    public String label() { return CHOOSE_DIR + DOTS; }
  }
}
