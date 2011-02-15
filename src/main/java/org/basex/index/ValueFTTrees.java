package org.basex.index;

import org.basex.util.Token;

/**
 * This class provides an array with several {@link ValueFTTree} instances,
 * one for each token length.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
class ValueFTTrees {
  /** For each token length a tree is created. */
  final ValueFTTree[] trees = new ValueFTTree[Token.MAXLEN + 1];
  /** Pointer on current tree. */
  private int ctree;

  /**
   * Indexes a token with full-text data.
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
   * Initializes all trees for adding new full-text data.
   */
  void initFT() {
    for(final ValueFTTree tree : trees) if(tree != null) tree.initFT();
  }

  /**
   * Initializes all trees for iterative traversal.
   */
  void init() {
    for(final ValueFTTree tree : trees) if(tree != null) tree.init();
    ctree = -1;
  }

  /**
   * Checks for more tokens.
   * @param cf current file
   * @return boolean more
   */
  boolean more(final int cf) {
    if(ctree != -1 && trees[ctree].more(cf)) return true;
    while(++ctree < trees.length) if(trees[ctree] != null) return more(cf);
    return false;
  }

  /**
   * Returns the next token.
   * @return byte[] next token
   */
  ValueFTTree nextTree() {
    return trees[ctree];
  }
}
