package org.basex.index;

import org.basex.util.Token;

/**
 * This class indexes all the XML Tokens in a balanced binary tree.
 * The iterator returns all compressed pre values in a sorted manner.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
class ValueFTTrees {
  /** For each token length a tree is created. */
  final ValueFTTree[] trees = new ValueFTTree[Token.MAXLEN];
  /** Pointer on current tree. */
  int nsize;

  /**
   * Check if specified token was already indexed; if yes, its pre
   * value is added to the existing values. otherwise, create new index entry.
   * @param tok token to be indexed
   * @param pre pre value for the token
   * @param pos pos value of the token
   * @param cf current file id
   */
  void index(final byte[] tok, final int pre, final int pos, final int cf) {
    final int tl = tok.length;
    if(trees[tl] == null) trees[tl] = new ValueFTTree();
    trees[tl].index(tok, pre, pos, cf);
  }

  /**
   * Initializes all trees for iterative traversal.
   */
  void init() {
    for(int j = 0; j < trees.length; j++) if(trees[j] != null) trees[j].init();
    nsize = -1;
  }

  /**
   * Initializes all trees and removes full-text data.
   */
  void initTrees() {
    for(int j = 0; j < trees.length; j++)
      if(trees[j] != null) trees[j].initTree();
  }

  /**
   * Checks for more tokens.
   * @param cf current file
   * @return boolean more
   */
  boolean more(final int cf) {
    if(nsize != -1 && trees[nsize].more(cf)) return true;
    while(++nsize < trees.length) if(trees[nsize] != null) return more(cf);
    return false;
  }

  /**
   * Returns the next token.
   * @return byte[] next token
   */
  ValueFTTree nextTree() {
    return trees[nsize];
  }
}
