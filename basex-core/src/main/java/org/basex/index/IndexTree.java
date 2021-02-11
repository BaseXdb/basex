package org.basex.index;

import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class indexes keys in a balanced binary tree, including their id values.
 * Iterator methods are available to traverse through the tree.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public class IndexTree {
  /** Factor for resize. */
  protected static final double FACTOR = 1.2;

  /** Keys saved in the tree. */
  public final TokenList keys = new TokenList(FACTOR);
  /** Compressed id values. */
  public TokenList ids = new TokenList(FACTOR);

  /** Mapping for using existing tree. */
  protected TokenIntMap maps = new TokenIntMap();
  /** Current iterator node. */
  protected int cn;

  /** Tree structure [left, right, parent]. */
  private final IntList tree = new IntList(FACTOR);
  /** Indicates which nodes have been modified. */
  private final BoolList mod = new BoolList();
  /** Tokenize keys. */
  private final boolean tokenize;
  /** Tree root node. */
  private int root = -1;

  /**
   * Constructor.
   * @param type index type
   */
  public IndexTree(final IndexType type) {
    tokenize = type == IndexType.TOKEN;
  }

  /**
   * Indexes the specified key and id.
   * @param key key to be indexed
   * @param id id to be indexed
   * @param pos token position (only relevant for token index)
   */
  public final void add(final byte[] key, final int id, final int pos) {
    add(key, id, pos, true);
  }

  /**
   * Indexes the specified key and id. If the key has already been
   * indexed, its id is appended to the existing array.
   * Otherwise, a new index entry is created.
   * @param key key to be indexed
   * @param id id to be indexed
   * @param pos token position (only relevant for token index)
   * @param exist flag for using existing index
   * @return int node
   */
  protected final int add(final byte[] key, final int id, final int pos, final boolean exist) {
    // index is empty.. create root node
    if(root == -1) {
      root = newNode(key, id, pos, -1, exist);
      return root;
    }

    int n = root;
    while(true) {
      final int diff = Token.diff(key, keys.get(n));
      if(diff == 0) {
        if(exist) {
          addIds(id, pos, n);
        } else {
          final int i = maps.get(Num.num(n));
          if(i < 0) {
            maps.put(Num.num(n), ids.size());
            addNewIds(id, pos);
          } else {
            addIds(id, pos, i);
          }
        }
        return n;
      }
      int ch = diff < 0 ? left(n) : right(n);
      if(ch == -1) {
        ch = newNode(key, id, pos, n, exist);
        if(diff < 0) {
          setLeft(n, ch);
          adjust(left(n));
        } else {
          setRight(n, ch);
          adjust(right(n));
        }
        return ch;
      }
      n = ch;
    }
  }

  /**
   * Returns the number of entries.
   * @return number of entries
   */
  public final int size() {
    return ids.size();
  }

  /**
   * Initializes the index iterator.
   * will be removed to save memory.
   */
  public final void init() {
    cn = root;
    if(cn != -1) while(left(cn) != -1) cn = left(cn);
  }

  /**
   * Checks if the iterator returns more keys.
   * @return true if more keys exist
   */
  public final boolean more() {
    return cn != -1;
  }

  /**
   * Returns the next pointer.
   * @return next pointer
   */
  public final int next() {
    /* Last iterator node. */
    final int ln = cn;
    if(right(cn) == -1) {
      int t = cn;
      cn = parent(cn);
      while(cn != -1 && t == right(cn)) {
        t = cn;
        cn = parent(cn);
      }
    } else {
      cn = right(cn);
      while(left(cn) != -1) cn = left(cn);
    }
    return ln;
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Creates a new id list and adds an id.
   * @param id id value
   * @param pos token position (only relevant for token index)
   */
  private void addNewIds(final int id, final int pos) {
    byte[] vs = Num.newNum(id);
    if(tokenize) vs = Num.add(vs, pos);
    ids.add(vs);
  }

  /**
   * Appends an id to id list n.
   * @param id id value
   * @param pos token position (only relevant for token index)
   * @param n id list to append to
   */
  private void addIds(final int id, final int pos, final int n) {
    byte[] vs = ids.get(n);
    vs = Num.add(vs, id);
    if(tokenize) vs = Num.add(vs, pos);
    ids.set(n, vs);
  }

  /**
   * Creates a new node.
   * @param key node key
   * @param id id value
   * @param pos token position (only relevant for token index)
   * @param par pointer to parent node
   * @param exist flag for reusing existing tree
   * @return pointer of the new node
   */
  private int newNode(final byte[] key, final int id, final int pos, final int par,
      final boolean exist) {

    tree.add(-1); // left node
    tree.add(-1); // right node
    tree.add(par); // parent node
    mod.add(false);
    keys.add(key);
    addNewIds(id, pos);
    if(!exist) maps.put(Num.num(keys.size() - 1), ids.size() - 1);
    return mod.size() - 1;
  }

  /**
   * Gets the left child.
   * @param nd current node
   * @return left node
   */
  private int left(final int nd) {
    return tree.get((nd << 1) + nd);
  }

  /**
   * Gets the right child.
   * @param nd current node
   * @return right node
   */
  private int right(final int nd) {
    return tree.get((nd << 1) + nd + 1);
  }

  /**
   * Gets the parent node.
   * @param nd current node
   * @return parent node
   */
  private int parent(final int nd) {
    return tree.get((nd << 1) + nd + 2);
  }

  /**
   * Sets the left child.
   * @param nd current node
   * @param val left node
   */
  private void setLeft(final int nd, final int val) {
    tree.set((nd << 1) + nd, val);
  }

  /**
   * Sets the right child.
   * @param nd current node
   * @param val right node
   */
  private void setRight(final int nd, final int val) {
    tree.set((nd << 1) + nd + 1, val);
  }

  /**
   * Sets the parent node.
   * @param nd current node
   * @param val parent node
   */
  private void setParent(final int nd, final int val) {
    tree.set((nd << 1) + nd + 2, val);
  }

  /**
   * Adjusts the tree balance.
   * @param nd node to be adjusted
   */
  private void adjust(final int nd) {
    int n = nd;
    mod.set(n, true);

    while(n != -1 && n != root && mod.get(parent(n))) {
      if(parent(n) == left(parent(parent(n)))) {
        final int y = right(parent(parent(n)));
        if(y != -1 && mod.get(y)) {
          mod.set(parent(n), false);
          mod.set(y, false);
          mod.set(parent(parent(n)), true);
          n = parent(parent(n));
        } else {
          if(n == right(parent(n))) {
            n = parent(n);
            rotateLeft(n);
          }
          mod.set(parent(n), false);
          mod.set(parent(parent(n)), true);
          if(parent(parent(n)) != -1) rotateRight(parent(parent(n)));
        }
      } else {
        final int y = left(parent(parent(n)));
        if(y != -1 && mod.get(y)) {
          mod.set(parent(n), false);
          mod.set(y, false);
          mod.set(parent(parent(n)), true);
          n = parent(parent(n));
        } else {
          if(n == left(parent(n))) {
            n = parent(n);
            rotateRight(n);
          }
          mod.set(parent(n), false);
          mod.set(parent(parent(n)), true);
          if(parent(parent(n)) != -1) rotateLeft(parent(parent(n)));
        }
      }
    }
    mod.set(root, false);
  }

  /**
   * Left rotation.
   * @param n node to be rotated
   */
  private void rotateLeft(final int n) {
    final int r = right(n);
    setRight(n, left(r));
    if(left(r) != -1) setParent(left(r), n);
    setParent(r, parent(n));
    if(parent(n) == -1) root = r;
    else if(left(parent(n)) == n) setLeft(parent(n), r);
    else setRight(parent(n), r);
    setLeft(r, n);
    setParent(n, r);
  }

  /**
   * Right rotation.
   * @param n node to be rotated
   */
  private void rotateRight(final int n) {
    final int l = left(n);
    setLeft(n, right(l));
    if(right(l) != -1) setParent(right(l), n);
    setParent(l, parent(n));
    if(parent(n) == -1) root = l;
    else if(right(parent(n)) == n) setRight(parent(n), l);
    else setLeft(parent(n), l);
    setRight(l, n);
    setParent(n, l);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    tb.add("IndexTree[Root: ").addInt(root).add(Prop.NL);
    final int size = keys.size();
    for(int c = 0; c < size; c++) {
      tb.add("  \"").add(keys.get(c)).add("\": ").add("ids");
      if(tokenize) tb.add("/pos");
      tb.add(": (").add(Num.toString(ids.get(c))).add(')');
      final int left = tree.get(c * 3), right = tree.get(c * 3 + 1);
      if(left >= 0) tb.add(", left:").addInt(left);
      if(right >= 0) tb.add(", right:").addInt(right);
      tb.add(Prop.NL);
    }
    tb.add("]");
    return tb.toString();
  }
}
