package org.basex.index.ft;

/**
 * This class provides an array with several {@link FTIndexTree} instances,
 * one for each token length.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
final class FTIndexTrees {
  /** For each key length, an extra tree is created. */
  private final FTIndexTree[] trees;
  /** Pointer on current tree. */
  private int ctree;

  /**
   * Constructor.
   * @param size number of tree instances
   */
  FTIndexTrees(final int size) {
    trees = new FTIndexTree[size + 1];
  }

  /**
   * Indexes a token with full-text data.
   * @param token token to be indexed
   * @param pre pre value for the token
   * @param pos pos value of the token
   * @param index current file id
   */
  void index(final byte[] token, final int pre, final int pos, final int index) {
    final int tl = token.length;
    if(trees[tl] == null) trees[tl] = new FTIndexTree();
    trees[tl].add(token, pre, pos, index);
  }

  /**
   * Initializes all trees for adding new full-text data.
   */
  void initFT() {
    for(final FTIndexTree tree : trees) {
      if(tree != null) tree.initFT();
    }
  }

  /**
   * Initializes all trees for iterative traversal.
   */
  void init() {
    for(final FTIndexTree tree : trees) {
      if(tree != null) tree.init();
    }
    ctree = -1;
  }

  /**
   * Checks for more tokens.
   * @param index current index split counter
   * @return boolean more
   */
  boolean more(final int index) {
    if(ctree != -1 && trees[ctree].more(index)) return true;
    final int tl = trees.length;
    while(++ctree < tl) {
      if(trees[ctree] != null) return more(index);
    }
    return false;
  }

  /**
   * Returns the next token.
   * @return byte[] next token
   */
  FTIndexTree nextTree() {
    return trees[ctree];
  }
}
