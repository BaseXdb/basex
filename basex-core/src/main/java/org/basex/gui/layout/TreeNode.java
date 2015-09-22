package org.basex.gui.layout;

import static org.basex.util.Token.*;

import javax.swing.event.*;
import javax.swing.tree.*;

import org.basex.data.*;

/**
 * JTree node for representing database content (raw files / documents).
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Lukas Kircher
 */
public abstract class TreeNode extends DefaultMutableTreeNode implements TreeWillExpandListener {
  /** Node name. */
  public final byte[] name;
  /** Relative path to this node. Starts with one slash, no trailing slashes.
   *  File/folder name is not part of the path. */
  final byte[] path;
  /** Tree reference for lazy loading. */
  final BaseXTree tree;
  /** Data reference. */
  final Data data;
  /** Updating. */
  boolean updating;

  /**
   * Constructor.
   * @param name displayed node name
   * @param path folder path
   * @param tree tree reference
   * @param data data reference
   */
  TreeNode(final byte[] name, final byte[] path, final BaseXTree tree, final Data data) {
    this.name = name;
    this.path = path;
    this.tree = tree;
    this.data = data;
    tree.addTreeWillExpandListener(this);
  }

  /**
   * Prepares the given path to be used as a tree node.
   * The returned path looks the following:
   * <ol>
   *   <li> The path is relative to the database root directory</li>
   *   <li> It has one leading slash</li>
   *   <li> It has no trailing slashes, no matter if it represents a folder or file</li>
   * </ol>
   * @param path path to be prepared
   * @return path
   */
  public static byte[] preparePath(final byte[] path) {
    String p = MetaData.normPath(string(path));
    if(p.endsWith("/")) p = p.substring(0, p.length() - 1);
    return concat(SLASH, token(p));
  }

  /**
   * Returns the complete path (path + name) of this node as a string.
   * @return path string
   */
  public String path() {
    return string(preparePath(concat(path, SLASH, name)));
  }

  /**
   * Lazy loading.
   */
  abstract void load();

  @Override
  public void treeWillExpand(final TreeExpansionEvent e) throws ExpandVetoException {
    if(equals(e.getPath().getLastPathComponent()))
      load();
  }

  @Override
  public void treeWillCollapse(final TreeExpansionEvent e) throws ExpandVetoException { }

  @Override
  public String toString() {
    return string(name);
  }
}
