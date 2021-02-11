package org.basex.index.ft;

import org.basex.index.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class indexes full-text tokens in a balanced binary tree, including
 * their pre and pos values. An iterator returns all compressed pre and pos
 * values in a sorted manner.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
final class FTIndexTree extends IndexTree {
  /** Compressed pre values. */
  private TokenList poss = new TokenList(FACTOR);
  /** Tree structure [left, right, parent]. */
  private IntList numpre = new IntList(FACTOR);
  /** Current pointer on token in the index. */
  private int lcn;
  /** Current pointer on ft data in the index. */
  private int pft;

  /**
   * Constructor.
   */
  FTIndexTree() {
    super(IndexType.FULLTEXT);
  }

  /**
   * Checks if the specified token was already indexed. If yes, its pre
   * value is added to the existing values. Otherwise a new index entry
   * is created.
   * @param token token to be indexed
   * @param id id value of the token
   * @param pos pos value of the token
   * @param index current file id
   */
  void add(final byte[] token, final int id, final int pos, final int index) {
    final int os = keys.size();
    final int n = add(token, id, 0, index == 0);
    if(os == keys.size()) {
      final int i = index > 0 ? maps.get(Num.num(n)) : n;
      if(poss.size() > i && poss.get(i) != null) {
        poss.set(i, Num.add(poss.get(i), pos));
        numpre.set(i, numpre.get(i) + 1);
        return;
      }
    }
    poss.add(Num.newNum(pos));
    numpre.add(1);
  }

  /**
   * Initializes the tree for adding new full-text data.
   */
  void initFT() {
    poss = new TokenList(FACTOR);
    ids = new TokenList(FACTOR);
    numpre = new IntList(FACTOR);
    maps = new TokenIntMap();
  }

  /**
   * Checks for more tokens.
   * @param index current index split counter
   * @return boolean more
   */
  boolean more(final int index) {
    while(more()) {
      lcn = cn;
      // write compressed representation if the index has already been split
      pft = index > 0 ? maps.get(Num.num(lcn)) : lcn;
      if(pft > -1) return true;
      next();
    }
    return false;
  }

  /**
   * Returns the next token.
   * @return byte[] next token
   */
  byte[] nextTok() {
    return keys.get(lcn);
  }

  /**
   * Returns the next pre values.
   * @return byte[] compressed pre values
   */
  byte[] nextPres() {
    return ids.get(pft);
  }

  /**
   * Returns the next pos values.
   * @return byte[] compressed pos values
   */
  byte[] nextPoss() {
    return poss.get(pft);
  }

  /**
   * Returns the next number of pre values.
   * @return number of pre values
   */
  int nextNumPre() {
    return numpre.get(pft);
  }
}
