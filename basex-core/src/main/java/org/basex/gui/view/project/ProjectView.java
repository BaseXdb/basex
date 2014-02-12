package org.basex.gui.view.project;

import static org.basex.core.Text.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.Fill;
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
public final class ProjectView extends BaseXPanel {
  /** Root directory. */
  final ProjectDir root;
  /** Tree. */
  final ProjectTree tree;
  /** Editor view. */
  final EditorView editor;
  /** Filter field. */
  final ProjectFilter filter;
  /** Filter list. */
  final ProjectList list;
  /** Root path. */
  final BaseXTextField path;
  /** Splitter. */
  private final BaseXSplit split;
  /** Last focused component. */
  private Component last;

  /** Remembers the last focused component. */
  final FocusAdapter lastfocus = new FocusAdapter() {
    @Override
    public void focusLost(final FocusEvent ev) {
      last = ev.getComponent();
    }
  };

  /**
   * Constructor.
   * @param ev editor view
   */
  public ProjectView(final EditorView ev) {
    super(ev.gui);
    editor = ev;
    setLayout(new BorderLayout());

    tree = new ProjectTree(this);
    root = new ProjectDir(new IOFile(root()), this);
    tree.init(root);

    filter = new ProjectFilter(this);
    list = new ProjectList(this);
    BaseXLayout.addInteraction(list, gui);

    final BaseXBack back = new BaseXBack().layout(new BorderLayout(2, 2));
    back.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, GUIConstants.GRAY),
        new EmptyBorder(3, 1, 3, 2)));

    path = new BaseXTextField(gui);
    path.setText(root.file.path());
    path.setEnabled(false);

    final BaseXButton browse = new BaseXButton(DOTS, gui);
    browse.setMargin(new Insets(0, 2, 0, 2));
    browse.setToolTipText(CHOOSE_DIR);
    browse.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        change();
      }
    });

    back.add(path, BorderLayout.CENTER);
    back.add(browse, BorderLayout.EAST);
    back.add(filter, BorderLayout.SOUTH);

    // add scroll bars
    final JScrollPane lscroll = new JScrollPane(list);
    lscroll.setBorder(new EmptyBorder(0, 0, 0, 0));
    final JScrollPane tscroll = new JScrollPane(tree);
    tscroll.setBorder(new EmptyBorder(0, 0, 0, 0));

    split = new BaseXSplit(false);
    split.mode(Fill.NONE);
    split.add(lscroll);
    split.add(tscroll);
    split.init(new double[] { 0.3, 0.7}, new double[] { 0, 1});
    split.visible(false);
    showList(false);

    add(back, BorderLayout.NORTH);
    add(split, BorderLayout.CENTER);

    last = tree;
    tree.addFocusListener(lastfocus);
    list.addFocusListener(lastfocus);
  }

  /**
   * Makes the list (in)visible.
   * @param vis visibility flag
   */
  public void showList(final boolean vis) {
    split.visible(vis);
  }

  /**
   * Refreshes the specified file node.
   * @param file file to be opened
   * @param tr refresh tree
   */
  public void refresh(final IOFile file, final boolean tr) {
    if(tr) refresh(file);
    filter.refresh(true);
  }

  /**
   * Jumps to the specified file.
   * @param file file to be focused
   */
  public void jump(final IOFile file) {
    final IOFile fl = new IOFile(canonical(file.file()));
    if(fl.path().startsWith(root.file.path())) tree.expand(root, fl.path());
  }

  /**
   * Refreshes the specified file node, or its parent.
   * @param file file to be refreshed
   */
  private void refresh(final IOFile file) {
    final ProjectNode node = find(file);
    if(node != null) {
      node.refresh();
    } else {
      final File parent = file.file().getParentFile();
      if(parent == null) return;
      refresh(new IOFile(parent));
    }
  }

  /**
   * Returns the node for the specified file.
   * @param file file to be found
   * @return node, or {@code null}
   */
  private ProjectNode find(final IOFile file) {
    final IOFile fl = new IOFile(canonical(file.file()));
    if(fl.path().startsWith(root.file.path())) {
      final Enumeration<?> en = root.depthFirstEnumeration();
      while(en.hasMoreElements()) {
        final ProjectNode node = (ProjectNode) en.nextElement();
        if(node.file != null && node.file.path().equals(fl.path())) return node;
      }
    }
    return null;
  }

  /**
   * Focuses the project filter.
   * @param string string to be found
   */
  public void focusFilter(final String string) {
    filter.focus(string);
  }

  /**
   * Focuses the project view.
   */
  public void focus() {
    last.requestFocusInWindow();
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
    final String proj = gui.gopts.get(GUIOptions.PROJECTPATH);
    if(!proj.isEmpty()) return proj;

    final File io1 = new File(gopts.get(GlobalOptions.REPOPATH));
    final File io2 = new File(gopts.get(GlobalOptions.WEBPATH));
    final File fl = new File(gopts.get(GlobalOptions.RESTXQPATH));
    final File io3 = fl.isAbsolute() ? fl : new File(io2, fl.getPath());
    final StringList sl = new StringList();
    for(final File f : new File[] { io1, io2, io3}) {
      final String p = canonical(f).getParent();
      if(!sl.contains(p)) sl.add(p);
    }
    return sl.unique().get(0);
  }

  /**
   * Returns the canonical or (if not possible) the absolute file reference.
   * @param f file reference
   * @return file
   */
  private File canonical(final File f) {
    try {
      return f.getCanonicalFile();
    } catch(final IOException ex) {
      return f.getAbsoluteFile();
    }
  }

  /**
   * Opens the selected file.
   * @param file file to be opened
   * @param search search string
   */
  void open(final IOFile file, final String search) {
    final EditorArea ea = editor.open(file);
    if(ea == null) return;

    // delay search and focus request (avoid keyTyped event to be handled in editor)
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        ea.jump(search);
      }
    });
  }

  /**
   * Changes the root directory.
   */
  void change() {
    final ProjectNode child = tree.selectedNode();
    final BaseXFileChooser fc = new BaseXFileChooser(CHOOSE_DIR, child.file.path(), gui);
    final IOFile io = fc.select(Mode.DOPEN);
    if(io != null) {
      root.file = io;
      root.refresh();
      filter.reset();
      path.setText(io.path());
      gui.gopts.set(GUIOptions.PROJECTPATH, io.path());
    }
  }
}
