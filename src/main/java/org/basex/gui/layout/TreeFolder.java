package org.basex.gui.layout;

import static org.basex.util.Token.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.index.DocIndex;
import org.basex.io.IOFile;
import org.basex.util.hash.TokenSet;
import org.basex.util.list.TokenList;

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
   * @param jtree JTree reference
   * @param d data reference
   */
  public TreeFolder(final byte[] nm, final byte[] pth, final JTree jtree,
      final Data d) {
    super(nm, pth, jtree, d);
  }

  @Override
  void load() {
    if(loaded) return;

    // append new child nodes, folders first ...
    for(final byte[] b : folders(this)) {
      add(new TreeFolder(b, subfolder(), tree, data));
    }

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
    final TokenSet ts = new TokenSet();

    // gather raw file children for this path
    for(final IOFile f : rawChildren(node.subfolder(), node.data)) {
      if(f.isDir()) ts.add(name(token(f.path())));
    }

    // gather document child folders for this path
    // folders
    for(final byte[] c : node.data.docindex.children(node.subfolder(), false)) {
      ts.add(c);
    }

    // sort keys
    return new TokenList(ts.keys()).sort(Prop.WIN).toArray();
  }

  /**
   * Finds all leaf child nodes for the given node.
   * @param node node for which to find leaves for
   * @return raw file / document leaves
   */
  public static TreeLeaf[] leaves(final TreeFolder node) {
    final Set<TreeLeaf> leaves = new HashSet<TreeLeaf>();

    // gather raw file children for this path
    for(final IOFile f : rawChildren(node.subfolder(), node.data)) {
      if(!f.isDir()) leaves.add(new TreeLeaf(token(f.name()),
          node.subfolder(), true, node.tree, node.data));
    }

    // gather document children for this path
    // folders
    final DocIndex di = node.data.docindex;
    for(final byte[] c : di.children(node.subfolder(), true)) {
      leaves.add(new TreeLeaf(c, node.subfolder(), false,
          node.tree, node.data));
    }

    // sort leaves
    final TreeLeaf[] tobesorted = leaves.toArray(new TreeLeaf[leaves.size()]);
    Arrays.sort(tobesorted, new Comparator<TreeLeaf>() {
      @Override
      public int compare(final TreeLeaf l1, final TreeLeaf l2) {
        final byte[] n1 = l1.name;
        final byte[] n2 = l2.name;
        return Prop.WIN ? diff(lc(n1), lc(n2)) : diff(n1, n2);
      }
    });

    return tobesorted;
  }

  /**
   * Returns raw file children for a given path.
   * @param path path
   * @param data Data reference
   * @return children of path
   */
  private static IOFile[] rawChildren(final byte[] path, final Data data) {
    return data.meta.binary(string(path)).children();
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
