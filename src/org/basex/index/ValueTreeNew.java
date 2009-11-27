package org.basex.index;

import org.basex.util.BoolList;
import org.basex.util.IntList;
import org.basex.util.Num;
import org.basex.util.TokenList;

/**
 * This class indexes all the XML Tokens in a balanced binary tree.
 * The iterator returns all tokens in a sorted manner.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
final class ValueTreeNew {
  /** Tokens saved in the tree. */
  final TokenList tokens = new TokenList();
  /** Tree structure [left, right, parent]. */
  final IntList tree = new IntList();
  /** Compressed pre values. */
  final byte[][] pres;

  
  /** Flag if a node has changed. */
  final BoolList changed = new BoolList();
  /** Tree root node. */
  int root = -1;
  /** Current number of tokens in the index. */
  int numPre;
  /** Iterator reference. */
  Iterator iterator;

  /**
   * Constructor.
   * @param maxPre maximal pre value
   */
  ValueTreeNew(final int maxPre) {
    pres = new byte[maxPre][];
  }
  
  /**
   * Check if specified token was already indexed; if yes, its pre
   * value is added to the existing values. otherwise, create new index entry. 
   * @param token token to be indexed
   * @param pre pre value for the token
   */
  public void  index(final byte[] token, final int pre) {
    // index is empty.. create root node
    if(root == -1) {
      root = newNode(token, pre, -1);
      return;
    }

    int node = root;
    while(true) {
      int c = compareTo(token, node);
      if(c == 0) {
        addPre(node, pre);
        return;
      }

      int child = c < 0 ? getLeft(node) : getRight(node);
      if(child != -1) {
        node = child;
        continue;
      }

      child = newNode(token, pre, node);
      if(c < 0) {
        setLeft(node, child);
        adjust(getLeft(node));
      } else {
        setRight(node, child);
        adjust(getRight(node));
      }
      return;
    }
  }


  /**
   * Create new node.
   * @param tok token of the node
   * @param pre pre value of the node
   * @param pa pointer on parent node
   * @return pointer of the new node
   */
  private int newNode(final byte[] tok, final int pre, final int pa) {
    tokens.add(tok);
    tree.add(-1); // left node
    tree.add(-1); // rigth node
    tree.add(pa); // parent node
    changed.add(false);
    final int p = tokens.size() - 1;
    pres[p] = Num.newNum(pre); 
    return p;
  }
  
  /**
   * Getter for the tree data.
   * @param node pointer on the node
   * @param p pointer on the data referenced
   * @return tree data value
   */
  private int get(final int node, final int p) {
    return tree.get(node * 3 + p); 
  }

  /**
   * Setter for the tree data.
   * @param node pointer on the node
   * @param p pointer on the data referenced
   * @param value data value to be set
   */
  private void set(final int node, final int p, final int value) {
    tree.set(value, node * 3 + p); 
  }
  
  /**
   * Get left child.
   * @param node current node
   * @return left node
   */
  public int getLeft(final int node) {
    return get(node, 0); 
  }

  /**
   * Setter for the left child.
   * @param node current node
   * @param value left node
   */
  private void setLeft(final int node, final int value) {
    set(node, 0, value);
  }
  
  /**
   * Get right child.
   * @param node current node
   * @return right node
   */
  public int getRight(final int node) {
    return get(node, 1); 
  }

  /**
   * Setter for the right child.
   * @param node current node
   * @param value right node
   */
  private void setRight(final int node, final int value) {
    set(node, 1, value);
  }

  /**
   * Get parent node.
   * @param node current node
   * @return parent node
   */
  public int getParent(final int node) {
    return  get(node, 2); 
  }
  
  /**
   * Setter for the parent node.
   * @param node current node
   * @param value parent node
   */
  private void setParent(final int node, final int value) {
    set(node, 2, value);
  }

  /**
   * Get pre values.
   * @param node current node
   * @return int[] pre values
   */
  public byte[] getPres(final int node) {
    return pres[node];
  }
  
//  /**
//   * Setter pre value.
//   * @param node current node
//   * @param value pre value
//   */
//  private void setPre(final int node, final int value) {
//    set(node, 3, value);
//  }

  /**
   * Add pre value to the existing values.
   * @param node current node
   * @param value pre value to be added
   */
  private void addPre(final int node, final int value) {
    pres[node] = Num.add(pres[node], value);
  }

  /**
   * Compare the two tokens lexicographically.
   * @param v1 first token
   * @param p2 pointer on second token
   * @return 0 if the strings are equals, -1 if the
   * token is lexicographically smaller than the
   * specified token, and 1 otherwise
   */
  public int compareTo(final byte[] v1, final int p2) {
    // compare tokens character wise
    final byte[] v2 = tokens.get(p2);
    int lim = Math.min(v1.length, v2.length);
    int i = -1;
    while(++i < lim) {
      int c = v1[i] - v2[i];
      if(c != 0) return c;
    }
    return v1.length - v2.length;
  }

  /**
   * Find specified token in the index and return its pre value. 
   * @param token token to be found
   * @return indexed token reference
   */
  public int get(final byte[] token) {
    int  node = root;
    while(node != -1) {
      int c = compareTo(token, node);
      if(c == 0) return node;
      node = c < 0 ? getLeft(node) : getRight(node);
    }
    return -1;
  }

  /**
   * Getter for the token value.
   * @param node current node
   * @return byte[] token
   */
  public byte[] getToken(final int node) {
    return tokens.get(node);
  }

  /**
   * Initialize index iterator.
   */
  public void init() {
    iterator = new Iterator();
  }

  /**
   * Check if iterator has more tokens.
   * @return true if more tokens exist
   */
  public boolean more() {
    return iterator.more();
  }

  /**
   * Return next iterator token.
   * Has to be called first.
   * @return next iterator token
   */
  public byte[] nextTok() {
    return iterator.nextTok();
  }

  /**
   * Returns pre values for the next iterator token.
   * Has to be called after nextTok()
   * @return next iterator token
   */
  public byte[] nextPres() {
    return iterator.nextPres();
  }

  
  /**
   * Adjusts tree balance.
   * @param n node to be adjusted
   */
  private void adjust(final int n) {
    int node = n;
    changed.set(true, node);

    while(node != -1 && node != root && changed.get(getParent(node))) {
      if(getParent(node) == getLeft(getParent(getParent(node)))) {
        int y = getRight(getParent(getParent(node)));
        if(y != -1 && changed.get(y)) {
          changed.set(false, getParent(node));
          changed.set(false, y);
          changed.set(true, getParent(getParent(node)));
          node = getParent(getParent(node));
        } else {
          if(node == getRight(getParent(node))) {
            node = getParent(node);
            rotateLeft(node);
          }
          changed.set(false, getParent(node));
          changed.set(true, getParent(getParent(node)));
          if(getParent(getParent(node)) != -1)
            rotateRight(getParent(getParent(node)));
        }
      } else {
        int y = getLeft(getParent(getParent(node)));
        if(y != -1 && changed.get(y)) {
          changed.set(false, getParent(node));
          changed.set(false, y);
          changed.set(true, getParent(getParent(node)));
          node = getParent(getParent(node));
        } else {
          if(node == getLeft(getParent(node))) {
            node = getParent(node);
            rotateRight(node);
          }
          changed.set(false, getParent(node));
          changed.set(true, getParent(getParent(node)));
          if(getParent(getParent(node)) != -1)
            rotateLeft(getParent(getParent(node)));
        }
      }
    }
    changed.set(false, root);
  }

  /**
   * Left rotation.
   * @param node node to be rotated
   */
  private void rotateLeft(final int node) {
    int right = getRight(node);
    setRight(node, getLeft(right));

    if(getLeft(right) != -1) {
      setParent(getLeft(right), node);
    }
    setParent(right, getParent(node));
    if(getParent(node) == -1) root = right;
    else if (getLeft(getParent(node)) == node) setLeft(getParent(node), right);
    else setRight(getParent(node), right);
    setLeft(right, node);
    setParent(node, right);
  }

  /**
   * Right rotation.
   * @param node node to be rotated
   */
  private void rotateRight(final int node) {
    int left = getLeft(node);
    setLeft(node, getRight(left));

    if (getRight(left) != -1) setParent(getRight(left), node);
    setParent(left, getParent(node));
    if (getParent(node) == -1) root = left;
    else if(getRight(getParent(node)) == node) setRight(getParent(node), left);
    else setLeft(getParent(node), left);
    setRight(left, node);
    setParent(node, left);
  }


  /** Depth first tree iterator. */
  private class Iterator {
    /** Current tree node. */
    int node;
    /** Last tree node. */
    int last;

    /** Iterator constructor. */
    Iterator() {
      // get left-most node
      node = root;
      if(node != -1) {
        while(getLeft(node) != -1) {
          node = getLeft(node);
        }
      }
    }

    /**
     * Checks if more tokens are found.
     * @return if there are more tokens
     */
    boolean more() {
      return node != -1;
    }

    /**
     * Return next token.
     * @return next token
     */
    byte[] nextTok() {
      last = node;
      if(getRight(node) != -1) {
        node = getRight(node);
        while(getLeft(node) != -1) node = getLeft(node);
      } else {
        int tmp = node;
        node = getParent(node);
        while(node != -1 && tmp == getRight(node)) {
          tmp = node;
          node = getParent(node);
        }
      }
      return tokens.get(last);
    }
    
    /**
     * Return next pre values.
     * @return int[] pre values
     */
    byte[] nextPres() {
      return getPres(last);
    }
  }

  /*
    public static void main(String[] args) {
      // fill test arrays with random tokens
      final byte[][] indexed = new byte[50000][];
      final byte[][] copy = new byte[50000][];

      for(int i = 0; i < indexed.length; i++) {
        TokenBuilder t = new TokenBuilder();
        int len = (int) (Math.random() * 10 + 1);
        for(int k = 0; k < len; k++) {
          t.add((char) (Math.random() * 26 + 65));
        }
        indexed[i] = t.finish();
        copy[i] = indexed[i];
      }

      ValueTreeNew v = new ValueTreeNew(50000);
      for(int i = 0; i < indexed.length; i++) 
        v.index(indexed[i], i+1);

      for(int i = 0; i < indexed.length; i++) { 
        final int p = v.get(copy[i]);
        if (!Token.eq(v.getToken(p), copy[i])) 
            System.out.print(new String(copy[i]) + " was not found");
      }
   }
   */
}
