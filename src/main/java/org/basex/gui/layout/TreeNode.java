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
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public abstract class TreeNode extends DefaultMutableTreeNode
    implements TreeWillExpandListener {

  /** Node name. */
  public final byte[] name;
  /** Relative path to this node. Starts with one slash, no trailing slashes.
   *  File / folder name is not part of the path. */
  final byte[] path;
  /** Tree reference for lazy loading. */
  final JTree tree;
  /** Data reference. */
  final Data data;

  /**
   * Constructor.
   * @param nm displayed node name
   * @param pth folder path
   * @param jtree JTree reference
   * @param d data reference
   */
  public TreeNode(final byte[] nm, final byte[] pth, final JTree jtree,
      final Data d) {
    name = nm;
    path = pth;
    tree = jtree;
    data = d;
    tree.addTreeWillExpandListener(this);
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
    String p = MetaData.normPath(string(path));
    if(p.endsWith("/")) p = p.substring(0, p.length() - 1);
    return concat(SLASH, token(p));
  }

  /**
   * Lazy loading.
   */
  abstract void load();

  @Override
  public void treeWillExpand(final TreeExpansionEvent e)
      throws ExpandVetoException {
    if(equals(e.getPath().getLastPathComponent())) load();
  }

  @Override
  public void treeWillCollapse(final TreeExpansionEvent e)
      throws ExpandVetoException { }

  @Override
  public String toString() {
    return string(name);
  }

  /**
   * Returns the complete path (path + name) of this node as a string.
   * @return path string
   */
  public String completePath() {
    return string(preparePath(concat(concat(path, SLASH), name)));
  }
}
