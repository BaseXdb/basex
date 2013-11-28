package org.basex.gui.view.project;

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
import org.basex.gui.view.editor.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Project file tree.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class ProjectView extends BaseXPanel implements TreeWillExpandListener {
  /** Root directory. */
  final ProjectDir root;
  /** Root node. */
  final ProjectCellRenderer renderer;
  /** Tree. */
  final BaseXTree tree;
  /** Editor view. */
  final EditorView view;
  /** Filter field. */
  final ProjectFilter filter;
  /** Filter list. */
  final ProjectList list;
  /** Scroll pane. */
  final JScrollPane scroll;

  /**
   * Constructor.
   * @param ev editor view
   */
  public ProjectView(final EditorView ev) {
    super(ev.gui);
    view = ev;
    setLayout(new BorderLayout());

    final DefaultMutableTreeNode invisible = new DefaultMutableTreeNode();
    tree = new BaseXTree(invisible, gui).border(4, 4, 4, 4);
    renderer = new ProjectCellRenderer();
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

    tree.setCellEditor(new ProjectCellEditor(tree, renderer));
    tree.setEditable(true);

    // choose common parent directories of project directories
    root = new ProjectDir(new IOFile(root()), this);
    invisible.add(root);

    // expand root and child directories
    for(int r = 0; r < 2; r++) tree.expandRow(r);
    tree.setRootVisible(false);
    tree.setSelectionRow(0);

    // add popup
    new BaseXPopup(tree, gui, new DeleteCmd(), new RenameCmd(), new NewCmd(), null,
        new ChangeCmd(), new RefreshCmd());

    // add scroll bar
    scroll = new JScrollPane(tree);
    scroll.setBorder(new EmptyBorder(0, 0, 0, 0));

    list = new ProjectList(this);
    BaseXLayout.addInteraction(list, gui);

    final BaseXBack back = new BaseXBack().layout(new GridLayout(1, 1));
    back.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, GUIConstants.GRAY),
        new EmptyBorder(3, 1, 3, 2)));
    filter = new ProjectFilter(this);

    back.add(filter);

    add(back, BorderLayout.NORTH);
    add(scroll, BorderLayout.CENTER);
  }

  @Override
  public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException {
    final Object obj = event.getPath().getLastPathComponent();
    if(obj instanceof ProjectNode) {
      final ProjectNode node = (ProjectNode) obj;
      node.expand();
      node.updateTree();
    }
  }

  @Override
  public void treeWillCollapse(final TreeExpansionEvent event) throws ExpandVetoException {
    final Object obj = event.getPath().getLastPathComponent();
    if(obj instanceof ProjectNode) {
      final ProjectNode node = (ProjectNode) obj;
      node.collapse();
      node.updateTree();
    }
  }

  /**
   * Focuses the project view.
   * @param filt focus filter or content
   */
  public void focus(final boolean filt) {
    if(filt) {
      filter.requestFocusInWindow();
    } else {
      scroll.getViewport().getView().requestFocusInWindow();
    }
  }

  /**
   * Renames a file or directory in the tree.
   * @param node source node
   * @param name new name of file or directory
   * @return new file reference, or {@code null} if operation failed
   */
  IOFile rename(final ProjectNode node, final String name) {
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
   * Returns a common parent directory.
   * @return root directory
   */
  private String root() {
    final GlobalOptions gopts = gui.context.globalopts;
    final String path = gui.gopts.get(GUIOptions.PROJECTPATH);
    if(!path.isEmpty()) return path;

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
    return sl.unique().get(0);
  }

  /**
   * Opens the selected file.
   */
  void open() {
    for(final ProjectNode node : selectedNodes()) open(node.file);
  }

  /**
   * Opens the selected file.
   * @param file file to be opened
   */
  void open(final IOFile file) {
    if(!file.isDir() && view.open(file) != null) view.getEditor().requestFocusInWindow();
  }

  /**
   * Returns the selected nodes.
   * @return selected node
   */
  private ArrayList<ProjectNode> selectedNodes() {
    final ArrayList<ProjectNode> nodes = new ArrayList<ProjectNode>();
    final TreePath[] paths = tree.getSelectionPaths();
    if(paths != null) {
      for(final TreePath path : paths) {
        final Object node = path.getLastPathComponent();
        if(node instanceof ProjectNode) nodes.add((ProjectNode) node);
      }
    }
    return nodes;
  }

  /**
   * Returns a single selected node, or {@code null} if zero or more than node is selected.
   * @return selected node
   */
  private ProjectNode selectedNode() {
    final TreePath path = selectedPath();
    if(path != null) {
      final Object node = path.getLastPathComponent();
      if(node instanceof ProjectNode) return (ProjectNode) node;
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
    public void execute(final GUI main) {
      filter.reset();
      if(enabled(main)) selectedNode().refresh();
    }
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
      ProjectNode parent = selectedNode();
      if(parent instanceof ProjectFile) parent = (ProjectDir) parent.getParent();

      // choose free name
      String name = '(' + NEW_DIR + ')';
      IOFile file = new IOFile(parent.file, name);
      int c = 1;
      while(file.exists()) {
        name = '(' + NEW_DIR + ' ' + ++c + ')';
        file = new IOFile(parent.file, name);
      }
      if(file.md()) {
        tree.setSelectionPaths(null);
        parent.refresh();
        final int cl = parent.getChildCount();
        for(int i = 0; i < cl; i++) {
          final ProjectNode node = (ProjectNode) parent.getChildAt(i);
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

      final ProjectNode node = selectedNode();
      if(BaseXDialog.confirm(gui, Util.info(DELETE_FILE_X, node.file))) {
        final ProjectNode parent = (ProjectNode) node.getParent();
        // delete file or show error dialog
        if(!view.delete(node.file)) {
          BaseXDialog.error(gui, Util.info(FILE_NOT_DELETED_X, node.file));
        } else {
          parent.refresh();
          tree.setSelectionPath(parent.path());
          filter.reset();
        }
      }
    }
    @Override
    public boolean enabled(final GUI main) {
      final ProjectNode node = selectedNode();
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
      filter.reset();
    }
    @Override
    public boolean enabled(final GUI main) {
      final ProjectNode node = selectedNode();
      return node != null && !node.root();
    }
    @Override
    public String label() { return RENAME; }
    @Override
    public String key() { return "F2"; }
  }

  /** Change directory command. */
  final class ChangeCmd extends GUIBaseCmd {
    @Override
    public void execute(final GUI main) {
      if(!enabled(gui)) return;
      final ProjectNode child = selectedNode();
      final BaseXFileChooser fc = new BaseXFileChooser(CHOOSE_DIR, child.file.path(), gui);
      final IOFile io = fc.select(Mode.DOPEN);
      if(io != null) {
        root.file = io;
        root.refresh();
        filter.reset();
        gui.gopts.set(GUIOptions.PROJECTPATH, io.path());
      }
    }
    @Override
    public boolean enabled(final GUI main) {
      return true;
    }
    @Override
    public String label() { return CHOOSE_DIR + DOTS; }
  }
}
