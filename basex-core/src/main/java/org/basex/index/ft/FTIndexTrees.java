package org.basex.index.ft;

import org.basex.index.*;

/**
 * This class provides an array with several {@link IndexTree} instances,
 * one for each token length.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
final class FTIndexTrees {
  /** For each key length, an extra tree is created. */
  private final IndexTree[] trees;
  /** Pointer on current tree. */
  private int ctree;

  /**
   * Constructor.
   * @param size number of tree instances
   */
  FTIndexTrees(final int size) {
    trees = new IndexTree[size + 1];
  }

  /**
   * Indexes a token with full-text data.
   * @param token token to be indexed
   * @param pre PRE value for the token
   * @param pos pos value of the token
   */
  void index(final byte[] token, final int pre, final int pos) {
    final int tl = token.length;
    if(trees[tl] == null) trees[tl] = new IndexTree(IndexType.FULLTEXT);
    trees[tl].add(token, pre, pos);
  }

  /**
   * Returns the estimated memory consumption of all trees.
   * @return memory consumption in bytes
   */
  long memory() {
    long m = 0;
    for(final IndexTree tree : trees) {
      if(tree != null) m += tree.memory();
    }
    return m;
  }

  /**
   * Initializes all trees for iterative traversal.
   */
  void init() {
    for(final IndexTree tree : trees) {
      if(tree != null) tree.init();
    }
    ctree = 0;
  }

  /**
   * Checks for more tokens.
   * @return boolean more
   */
  boolean more() {
    final int tl = trees.length;
    for(; ctree < tl; ctree++) {
      final IndexTree tree = trees[ctree];
      if(tree != null && tree.more()) return true;
    }
    return false;
  }

  /**
   * Returns the next tree.
   * @return tree
   */
  IndexTree nextTree() {
    return trees[ctree];
  }
}
