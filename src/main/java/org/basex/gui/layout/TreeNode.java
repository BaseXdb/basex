package org.basex.gui.layout;

import static org.basex.util.Token.*;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;

import org.basex.data.Data;
import org.basex.data.MetaData;

/**
 * JTree node for representing database content (raw files / documents).
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public abstract class TreeNode extends DefaultMutableTreeNode implements
  TreeWillExpandListener {

  /** Relative path to this node. Starts with one slash, no trailing slashes.
   *  File / folder name is not part of the path. */
  final byte[] p;
  /** Node name. */
  final byte[] n;
  /** Tree reference for lazy loading. */
  final JTree t;
  /** Data reference. */
  final Data d;

  /**
   * Constructor.
   * @param name displayed node name
   * @param path folder path
   * @param jtree JTree reference
   * @param data data reference
   */
  public TreeNode(final byte[] name, final byte[] path, final JTree jtree,
      final Data data) {
    n = name;
    p = path;
    t = jtree;
    d = data;
    t.addTreeWillExpandListener(this);
  }

  /**
   * Prepares the given path to be used as a tree node. The returned path looks
   * the following:
   * the path ...
   * 1. is relative to the BaseXData directory
   * 2. has one leading slash
   * 3. has no trailing slashes, no matter if it represents folder or file
   * @param path path to be prepared
   * @return path
   */
  public static byte[] preparePath(final byte[] path) {
    return concat(SLASH, token(MetaData.normPath2(string(path))));
  }

  /**
   * Lazy loading.
   */
  abstract void load();

  @Override
  public void treeWillExpand(final TreeExpansionEvent e)
      throws ExpandVetoException {
    final Object o = e.getPath().getLastPathComponent();
    if(this.equals(o)) {
      load();
    }
  }

  @Override
  public void treeWillCollapse(final TreeExpansionEvent e)
      throws ExpandVetoException { }

  @Override
  public String toString() {
    return string(n);
  }

  /**
   * Returns the complete path (path + name) of this node as a string.
   * @return path string
   */
  public String completePath() {
    return string(preparePath(concat(concat(p, SLASH), n)));
  }
}
