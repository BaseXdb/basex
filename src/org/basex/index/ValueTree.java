package org.basex.index;

import org.basex.util.BoolList;
import org.basex.util.IntList;
import org.basex.util.Num;
import org.basex.util.Token;
import org.basex.util.TokenList;

/**
 * This class indexes all the XML Tokens in a balanced binary tree.
 * The iterator returns all compressed pre values in a sorted manner.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
class ValueTree {
  /** Compressed pre values. */
  public TokenList pres = new TokenList(1.2);
  /** Tree structure [left, right, parent]. */
  public  IntList tree = new IntList(1.2);
  /** Tokens saved in the tree. */
  public  TokenList tokens = new TokenList(1.2);
  /** Flag if a node has been modified. */
  public  BoolList mod = new BoolList();
  /** Tree root node. */
  protected int root = -1;
  /** Mapping for usage of existing tree. */ 
  public IntList map = new IntList(1.1);

  /** Current iterator node. */
  protected int cn;
  /** Last iterator node. */
  private int ln;


  /**
   * Check if specified token was already indexed; if yes, its pre
   * value is added to the existing values. otherwise, create new index entry.
   * @param tok token to be indexed
   * @param pre pre value for the token
   * @return int node
   */
  int index(final byte[] tok, final int pre) {
    return index(tok, pre, true);
  }
  
  /**
   * Check if specified token was already indexed; if yes, its pre
   * value is added to the existing values. otherwise, create new index entry.
   * @param tok token to be indexed
   * @param pre pre value for the token
   * @param f flag for usage of existing index
   * @return int node
   */
  int index(final byte[] tok, final int pre, final boolean f) {
    // index is empty.. create root node
    if(root == -1) {
      root = n(tok, pre, -1, f);
      return root;
    }

    int n = root;
    while(true) {
      final int c = Token.diff(tok, tokens.get(n));
      if(c == 0) {
        if (f) pres.set(Num.add(pres.get(n), pre), n);
        else {
          final int i = map.containsAtPos(n);
          if (i < 0) {
            map.add(n);
            pres.add(Num.newNum(pre));
          } else pres.set(Num.add(pres.get(i), pre), i);
//          return i;
        }
        return n;
      }
      int ch = c < 0 ? l(n) : r(n);
      if(ch != -1) {
        n = ch;
      } else {
        ch = n(tok, pre, n, f);
        if(c < 0) {
          l(n, ch);
          a(l(n));
        } else {
          r(n, ch);
          a(r(n));
        }
        return ch;
      }
    }
  }

  /**
   * Returns the number of entries.
   * @return number of entries
   */
  int size() {
    return pres.size();
  }

  /**
   * Initializes the index iterator.
   * Note that the iterator can only be called once; index structures
   * will be removed to save memory.
   */
  void init() {
    cn = root;
    if(cn != -1) while(l(cn) != -1) cn = l(cn);
  }

  /**
   * Checks if the iterator returns more tokens.
   * @return true if more tokens exist
   */
  boolean more() {
    return cn != -1;
  }

  
//  /**
//   * Returns the next pre values.
//   * @return next iterator token
//   */
//  int next() {
//    ln = nextPoi();
//    final byte[] pp = pres.get(ln);
//    pres.set(null, ln);
//    return pp;  
//  }
  
  /**
   * Returns the next pointer.
   * @return next iterator token
   */
  int next() {
    ln = cn;
    if(r(cn) != -1) {
      cn = r(cn);
      while(l(cn) != -1) cn = l(cn);
    } else {
      int t = cn;
      cn = p(cn);
      while(cn != -1 && t == r(cn)) {
        t = cn;
        cn = p(cn);
      }
    }
    return ln;
  }

  // PRIVATE METHODS ==========================================================

  /**
   * Creates a new node.
   * @param tok token of the node
   * @param pre pre value of the node
   * @param pa pointer on parent node
   * @param f flag for usage of existing tree
   * @return pointer of the new node
   */
  private int n(final byte[] tok, final int pre, final int pa, 
      final boolean f) {
    tree.add(-1); // left node
    tree.add(-1); // right node
    tree.add(pa); // parent node
    mod.add(false);
    tokens.add(tok);
    pres.add(Num.newNum(pre));
    if (!f) map.add(tokens.size() - 1);    
    return mod.size() - 1;
  }

  /**
   * Gets the left child.
   * @param nd current node
   * @return left node
   */
  protected int l(final int nd) {
    return tree.get(nd * 3);
  }

  /**
   * Gets the right child.
   * @param nd current node
   * @return right node
   */
  protected int r(final int nd) {
    return tree.get(nd * 3 + 1);
  }

  /**
   * Gets the parent node.
   * @param nd current node
   * @return parent node
   */
  private int p(final int nd) {
    return tree.get(nd * 3 + 2);
  }

  /**
   * Setter for the left child.
   * @param nd current node
   * @param val left node
   */
  private void l(final int nd, final int val) {
    tree.set(val, nd * 3);
  }

  /**
   * Setter for the right child.
   * @param nd current node
   * @param val right node
   */
  private void r(final int nd, final int val) {
    tree.set(val, nd * 3 + 1);
  }

  /**
   * Setter for the parent node.
   * @param nd current node
   * @param val parent node
   */
  private void p(final int nd, final int val) {
    tree.set(val, nd * 3 + 2);
  }

  /**
   * Adjusts the tree balance.
   * @param nd node to be adjusted
   */
  private void a(final int nd) {
    int n = nd;
    mod.set(true, n);

    while(n != -1 && n != root && mod.get(p(n))) {
      if(p(n) == l(p(p(n)))) {
        final int y = r(p(p(n)));
        if(y != -1 && mod.get(y)) {
          mod.set(false, p(n));
          mod.set(false, y);
          mod.set(true, p(p(n)));
          n = p(p(n));
        } else {
          if(n == r(p(n))) {
            n = p(n);
            rl(n);
          }
          mod.set(false, p(n));
          mod.set(true, p(p(n)));
          if(p(p(n)) != -1) rr(p(p(n)));
        }
      } else {
        final int y = l(p(p(n)));
        if(y != -1 && mod.get(y)) {
          mod.set(false, p(n));
          mod.set(false, y);
          mod.set(true, p(p(n)));
          n = p(p(n));
        } else {
          if(n == l(p(n))) {
            n = p(n);
            rr(n);
          }
          mod.set(false, p(n));
          mod.set(true, p(p(n)));
          if(p(p(n)) != -1) rl(p(p(n)));
        }
      }
    }
    mod.set(false, root);
  }

  /**
   * Left rotation.
   * @param n node to be rotated
   */
  private void rl(final int n) {
    final int r = r(n);
    r(n, l(r));
    if(l(r) != -1) p(l(r), n);
    p(r, p(n));
    if(p(n) == -1) root = r;
    else if (l(p(n)) == n) l(p(n), r);
    else r(p(n), r);
    l(r, n);
    p(n, r);
  }

  /**
   * Right rotation.
   * @param n node to be rotated
   */
  private void rr(final int n) {
    final int l = l(n);
    l(n, r(l));
    if (r(l) != -1) p(r(l), n);
    p(l, p(n));
    if (p(n) == -1) root = l;
    else if(r(p(n)) == n) r(p(n), l);
    else l(p(n), l);
    r(l, n);
    p(n, l);
  }
}
