package org.basex.gui.layout;

import static org.basex.util.Token.*;

import java.util.*;

import javax.swing.tree.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * JTree node which represents a folder.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public class TreeFolder extends TreeNode {
  /** Children of node have been loaded. */
  private boolean loaded;

  /**
   * Constructor.
   * @param nm displayed node name
   * @param pth folder path
   * @param t tree reference
   * @param d data reference
   */
  public TreeFolder(final byte[] nm, final byte[] pth, final BaseXTree t, final Data d) {
    super(nm, pth, t, d);
  }

  @Override
  void load() {
    if(loaded) return;

    // append new child nodes, folders first ...
    for(final byte[] b : folders(this)) add(new TreeFolder(b, subfolder(), tree, data));
    // ... then leaves.
    for(final TreeLeaf l : leaves(this)) add(l);

    loaded = true;
    ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(this);
  }

  /**
   * Finds all raw and document folders on the child axis of the given node.
   * @param node node for which to find child folders for
   * @return folders
   */
  private static byte[][] folders(final TreeFolder node) {
    final Resources res = node.data.resources;
    final TokenBoolMap ts = res.children(node.subfolder(), true);
    return new TokenList(ts.keys()).sort(Prop.CASE).toArray();
  }

  /**
   * Finds all leaf child nodes for the given node.
   * @param node node for which to find leaves for
   * @return raw file / document leaves
   */
  public static TreeLeaf[] leaves(final TreeFolder node) {
    // get child resources
    final Resources res = node.data.resources;
    final TokenBoolMap tbm = res.children(node.subfolder(), false);

    // create leaf nodes
    final int ts = tbm.size();
    final TreeLeaf[] leaves = new TreeLeaf[ts];
    for(int t = 0; t < ts; t++) {
      leaves[t] = new TreeLeaf(tbm.key(t + 1), node.subfolder(),
          tbm.value(t + 1), node.tree, node.data);
    }

    // sort and return leaves
    Arrays.sort(leaves, new Comparator<TreeLeaf>() {
      @Override
      public int compare(final TreeLeaf l1, final TreeLeaf l2) {
        final byte[] n1 = l1.name;
        final byte[] n2 = l2.name;
        return Prop.CASE ? diff(n1, n2) : diff(lc(n1), lc(n2));
      }
    });
    return leaves;
  }

  /**
   * Returns the path for a child node of this folder.
   * @return sub folder path
   */
  byte[] subfolder() {
    return path.length > 1 ? concat(concat(path, SLASH), name) :
      concat(path, name);
  }

  /**
   * Extracts the file/document/folder name from the given path.
   * @param path path
   * @return name
   */
  public static byte[] name(final byte[] path) {
    final byte[][] s = split(path, '/');
    if(s.length == 0) return EMPTY;
    return s[s.length - 1];
  }

  /**
   * Returns the path without folder/file name for the given path.
   * @param path path
   * @return path without file/folder name
   */
  public static byte[] path(final byte[] path) {
    if(path.length == 0) return path;
    final byte[] r = substring(path, 0, lastIndexOf(path, '/'));
    return r.length == 0 ? SLASH : r;
  }

  /**
   * Removes all children from this node.
   */
  public void removeChildren() {
    removeAllChildren();
    loaded = false;
  }

  /**
   * Reloads this node without repainting the tree.
   */
  public void reload() {
    removeChildren();
    load();
  }

  @Override
  public boolean isLeaf() {
    return false;
  }
}
