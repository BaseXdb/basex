package org.basex.gui.layout;

import static org.basex.util.Token.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import org.basex.data.Data;
import org.basex.index.DocIndex;
import org.basex.io.IOFile;
import org.basex.util.Array;
import org.basex.util.Token;
import org.basex.util.hash.TokenSet;

/**
 * JTree node which represents a folder.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public class TreeFolder extends TreeNode {

  /** Children of node have been loaded. */
  private boolean loaded;

  /**
   * Constructor.
   * @param name displayed node name
   * @param path folder path
   * @param jtree JTree reference
   * @param data data reference
   */
  public TreeFolder(final byte[] name, final byte[] path, final JTree jtree,
      final Data data) {
    super(name, path, jtree, data);
  }

  @Override
  void load() {
    if(loaded) return;

    // append new child nodes, folders first ...
    final byte[][] folders = foldersForPath(this);
    for(final byte[] b : folders) {
      add(new TreeFolder(b, subfolder(), t, d));
    }

    // ... then leafs.
    final TreeLeaf[] leafs = leafsForPath(this);
    for(final TreeLeaf l : leafs) add(l);

    loaded = true;
    ((DefaultTreeModel) t.getModel()).nodeStructureChanged(this);
  }

  /**
   * Finds all raw and document folders on the child axis of the given node.
   * @param node node for which to find child folders for
   * @return folders
   */
  static byte[][] foldersForPath(final TreeFolder node) {
    final TokenSet ts = new TokenSet();

    // gather raw file childs for this path
    for(final IOFile f : rawChildren(node.subfolder(), node.d))
      if(f.isDir()) ts.add(name(token(f.path())));

    // gather document child folders for this path
    // folders
    final DocIndex di = node.d.docindex();
    for(final byte[] c : di.childDocuments(node.subfolder(), false))
      ts.add(c);

    // sort keys
    final byte[][] unsorted = ts.keys();
    final byte[][] sorted = new byte[unsorted.length][];
    int is = 0;
    for(final int i : Array.createOrder(unsorted, false, true))
      sorted[is++] = unsorted[i];

    return sorted;
  }

  /**
   * Finds all leaf child nodes for the given node.
   * @param node node for which to find leafs for
   * @return raw file / document leafs
   */
  static TreeLeaf[] leafsForPath(final TreeFolder node) {
    final Set<TreeLeaf> leafs = new HashSet<TreeLeaf>();

    // gather raw file childs for this path
    for(final IOFile f : rawChildren(node.subfolder(), node.d)) {
      if(!f.isDir()) leafs.add(new TreeLeaf(token(f.name()),
          node.subfolder(), true, node.t, node.d));
    }

    // gather document childs for this path
    // folders
    final DocIndex di = node.d.docindex();
    for(final byte[] c : di.childDocuments(node.subfolder(), true)) {
      leafs.add(new TreeLeaf(c, node.subfolder(), false, node.t, node.d));
    }

    // sort leafs
    final TreeLeaf[] tobesorted = leafs.toArray(new TreeLeaf[leafs.size()]);
    Arrays.sort(tobesorted, new Comparator<TreeLeaf>() {
      @Override
      public int compare(final TreeLeaf l1, final TreeLeaf l2) {
        if(Token.eq(l1.n, l2.n)) return 0;

        final int[] o = Array.createOrder(new byte[][] { l1.n, l2.n },
            false, true);
        return o[0] - o[1];
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
  static IOFile[] rawChildren(final byte[] path, final Data data) {
    final byte[] dbPath = token(data.meta.binaries().path());
    final IOFile raw = new IOFile(string(concat(
        concat(dbPath, SLASH), path)));
    return raw.children();
  }

  /**
   * Returns the path for a child node of this folder.
   * @return subfolder path
   */
  byte[] subfolder() {
    return p.length > 1 ? concat(concat(p, SLASH), n) :
      concat(p, n);
  }

  /**
   * Extracts the file/document/folder name from the given path.
   * @param path path
   * @return name
   */
  static byte[] name(final byte[] path) {
    final byte[][] s = split(path, '/');
    if(s.length == 0) return EMPTY;
    return s[s.length - 1];
  }

  /**
   * Returns the path without folder / file name for the given path.
   * @param path path
   * @return path without file / folder name
   */
  public static byte[] path(final byte[] path) {
    if(path.length == 0) return path;
    final byte[] r = substring(path, 0, lastIndexOf(path, '/'));
    return r.length == 0 ? SLASH : r;
  }

  /**
   * Removes all children from this node.
   */
  void removeChildren() {
    removeAllChildren();
    loaded = false;
  }

  /**
   * Reloads this node without repainting the tree.
   */
  void reload() {
    removeChildren();
    load();
  }

  @Override
  public boolean isLeaf() {
    return false;
  }
}