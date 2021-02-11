package org.basex.gui.layout;

import static org.basex.util.Token.*;

import java.util.*;

import javax.swing.tree.*;

import org.basex.core.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * JTree node which represents a folder.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
public class ResourceFolder extends ResourceNode {
  /** Comparator. */
  private static final Comparator<byte[]> CMP =
      (o1, o2) -> Prop.CASE ? diff(o1, o2) : diff(lc(o1), lc(o2));

  /** Children of node have been loaded. */
  private boolean loaded;
  /** Maximum number of displayed/processed children for a node. */
  public static final int MAXC = 10000;

  /**
   * Constructor.
   * @param name displayed node name
   * @param path folder path
   * @param tree tree reference
   * @param context database context
   */
  public ResourceFolder(final byte[] name, final byte[] path, final BaseXTree tree,
      final Context context) {
    super(name, path, tree, context);
  }

  @Override
  void load() {
    if(loaded || updating) return;

    updating = true;
    int cmax = MAXC;
    // add folders
    final byte[] sub = subfolder();
    final TokenSet set = context.data().resources.children(subfolder(), true);
    for(final byte[] f : new TokenList(set).sort(Prop.CASE)) {
      add(new ResourceFolder(f, sub, tree, context));
      if(--cmax == 0) break;
    }
    // add leaves
    cmax = addLeaves(EMPTY, cmax, this);
    // add dummy node if not all nodes are displayed
    if(cmax <= 0)
      add(new ResourceLeaf(token(Text.DOTS), sub, false, true, tree, context));

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
  public final int addLeaves(final byte[] filter, final int cmax, final ResourceFolder target) {
    final TokenBoolMap tbm = context.data().resources.children(subfolder(), false);
    final List<byte[]> keys = new ArrayList<>(tbm.size());

    // get desired leaves, depending on the given filter
    for(final byte[] b : tbm) {
      if(filter.length == 0 || eq(b, filter)) keys.add(b);
    }
    keys.sort(CMP);

    // finally add the necessary leaves
    final byte[] sub = subfolder();
    int k = 0, m = cmax;
    final int ks = keys.size();
    while(k < ks && m-- > 0) {
      final byte[] nm = keys.get(k++);
      target.add(new ResourceLeaf(nm, sub, tbm.get(nm), false, tree, context));
    }

    return m;
  }

  /**
   * Returns the path for a child node of this folder.
   * @return sub folder path
   */
  byte[] subfolder() {
    return concat(path.length > 1 ? concat(path, SLASH) : path, name);
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
  public final void removeChildren() {
    removeAllChildren();
    loaded = false;
  }

  /**
   * Reloads this node without repainting the tree.
   */
  public final void reload() {
    removeChildren();
    load();
  }

  @Override
  public boolean isLeaf() {
    return false;
  }
}
