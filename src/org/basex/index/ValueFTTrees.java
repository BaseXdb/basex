package org.basex.index;

import org.basex.util.IntList;

/**
 * This class indexes all the XML Tokens in a balanced binary tree.
 * The iterator returns all compressed pre values in a sorted manner.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
class ValueFTTrees {
  /** Saves the token length for each ValueFTTree. */
  final IntList sizes = new IntList(1.25);
  /** For each token length a tree is created. */
  ValueFTTree[] trees = new ValueFTTree[sizes.maxSize()];

  /**
   * Check if specified token was already indexed; if yes, its pre
   * value is added to the existing values. otherwise, create new index entry.
   * @param tok token to be indexed
   * @param pre pre value for the token
   * @param pos pos value of the token
   * @param cf current file id
   */
  void index(final byte[] tok, final int pre, final int pos, final int cf) {
    int i = sizes.indexOf(tok.length);
    if(i == -1) {
      sizes.add(tok.length);
      if(sizes.size() > trees.length) {
        final ValueFTTree[] tmp = new ValueFTTree[sizes.maxSize()];
        System.arraycopy(trees, 0, tmp, 0, trees.length);
        trees = tmp;
      }
      i = sizes.size() - 1;
      trees[i] = new ValueFTTree();
    }

    trees[i].index(tok, pre, pos, cf);
  }

  /**
   * Return next token length.
   * @return id of ValueFTTree
   */
  private int getNextMin() {
    int min = -1;
    for(int j = 0; j < sizes.size(); j++) {
      final int n = sizes.get(j);
      if(n > 0 && (min == -1 || sizes.get(min) > n)) min = j;
    }
    return min;
  }

  /**
   * Init all ValueFTTrees, for iterative traversal.
   */
  void init() {
    for(int j = 0; j < sizes.size(); j++) trees[j].init();
  }

  /**
   * Initializes all trees and removes full-text data.
   */
  void initTrees() {
    for(int j = 0; j < sizes.size(); j++) {
      trees[j].initTree();
      sizes.set(-sizes.get(j), j);
    }
  }

  /** Pointer on current ValueFTTree. */
  int nsize = -1;

  /**
   * Checks for more tokens.
   * @param cf current file
   * @return boolean more
   */
  boolean more(final int cf) {
    if(nsize == -1) {
      nsize = getNextMin();
      if(nsize == -1) return false;
    }
    if(trees[nsize].more(cf)) return true;

    sizes.set(-sizes.get(nsize), nsize);
    nsize = -1;
    return more(cf);
  }

  /**
   * Returns next token.
   * @return byte[] next token
   */
  byte[] nextTok() {
    return trees[nsize].nextTok();
  }

  /**
   * Returns next pointer.
   * @return int next pointer.
   */
  int nextPoi() {
    return trees[nsize].next();
  }

  /**
   * Returns the next pre values.
   * @return byte[] compressed pre values
   */
  byte[] nextPres() {
    return trees[nsize].nextPres();
  }

  /**
   * Returns next pos values.
   * @return compressed pos values
   */
  byte[] nextPos() {
    return trees[nsize].nextPos();
  }

  /**
   * Returns next number of pre values.
   * @return int number of pre values
   */
  int nextNumPre() {
    return trees[nsize].nextNumPre();
  }
}
