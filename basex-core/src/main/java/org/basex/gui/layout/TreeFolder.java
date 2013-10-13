package org.basex.gui.layout;

import static org.basex.util.Token.*;

import java.util.*;

import javax.swing.tree.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * JTree node which represents a folder.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
public class TreeFolder extends TreeNode {
  /** Children of node have been loaded. */
  private boolean loaded;
  /** Maximum number of displayed/processed children for a node. */
  public static final int MAXC = 10000;

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
    if(loaded || updating) return;

    updating = true;
    int cmax = MAXC;
    // add folders
    final byte[] sub = subfolder();
    final TokenSet set = data.resources.children(subfolder(), true);
    for(final byte[] f : new TokenList(set).sort(Prop.CASE)) {
      add(new TreeFolder(f, sub, tree, data));
      if(--cmax == 0) break;
    }
    // add leaves
    cmax = addLeaves(EMPTY, cmax, this);
    // add dummy node if not all nodes are displayed
    if(cmax <= 0)
      add(new TreeLeaf(token(Text.DOTS), sub, false, true, tree, data));

    loaded = true;
    ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(this);
    updating = false;
  }

  /**
   * Filters child nodes by the given filter expression and adds the remaining children
   * to the given node.
   * @param filter filter expression
   * @param cmax counter for the maximum number of children to add
   * @param target node to append filtered nodes
   * @return number of remaining nodes that can be added
   */
  public int addLeaves(final byte[] filter, final int cmax, final TreeFolder target) {
    final TokenBoolMap tbm = data.resources.children(subfolder(), false);
    final List<byte[]> keys = new ArrayList<byte[]>(tbm.size());

    // get desired leaves, depending on the given filter
    for(final byte[] b : tbm) {
      if(filter.length == 0 || eq(b, filter)) keys.add(b);
    }
    Collections.sort(keys, new Comparator<byte[]>() {
      @Override
      public int compare(final byte[] o1, final byte[] o2) {
        return Prop.CASE ? diff(o1, o2) : diff(lc(o1), lc(o2));
      }
    });

    // finally add the necessary leaves
    final byte[] sub = subfolder();
    int i = 0, m = cmax;
    while(i < keys.size() && m-- > 0) {
      final byte[] nm = keys.get(i++);
      target.add(new TreeLeaf(nm, sub, tbm.get(nm), false, tree, data));
    }

    return m;
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
