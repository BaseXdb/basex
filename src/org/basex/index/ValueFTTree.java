package org.basex.index;

import org.basex.util.IntList;
import org.basex.util.IntMap;
import org.basex.util.Num;
import org.basex.util.TokenList;

/**
 * This class indexes all the XML Tokens in a balanced binary tree.
 * Additional all pre and pos values are stored.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
final class ValueFTTree extends ValueTree {
  /** Compressed pre values. */
  private TokenList poss = new TokenList(FACTOR);
  /** Tree structure [left, right, parent]. */
  private IntList numpre = new IntList(FACTOR);
  /** Current pointer on token in the index. */
  private int lcn;
  /** Current pointer on ft data in the index. */
  private int pft;

  /**
   * Checks if the specified token was already indexed; if yes, its pre
   * value is added to the existing values. otherwise, a new index entry
   * is created.
   * @param tok token to be indexed
   * @param pre pre value for the token
   * @param pos pos value of the token
   * @param cf current file id
   */
  void index(final byte[] tok, final int pre, final int pos, final int cf) {
    final int os = tokens.size();
    final int n = index(tok, pre, cf == 0);
    if(os == tokens.size()) {
      final int i = cf > 0 ? maps.get(Num.num(n)) : n;
      if(poss.size() > i && poss.get(i) != null) {
        poss.set(Num.add(poss.get(i), pos), i);
        numpre.set(numpre.get(i) + 1, i);
        return;
      }
    }
    poss.add(Num.newNum(pos));
    numpre.add(1);
  }

  /**
   * Initializes the iterator.
   */
  void initIter() {
    cn = root;
    if(cn != -1) while(l(cn) != -1) cn = l(cn);
  }

  /**
   * Initializes the tree for new full-text values.
   */
  void initTree() {
    poss = new TokenList(FACTOR);
    pres = new TokenList(FACTOR);
    numpre = new IntList(FACTOR);
    maps = new IntMap();
  }

  /**
   * Checks for more tokens.
   * @param cf current file
   * @return boolean more
   */
  boolean more(final int cf) {
    while(more()) {
      lcn = cn;
      pft = cf > 0 ? maps.get(Num.num(lcn)) : lcn;
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
    return tokens.get(lcn);
  }

  /**
   * Returns the next pre values.
   * @return byte[] compressed pre values
   */
  byte[] nextPres() {
    return pres.get(pft);
  }

  /**
   * Returns the next pos values.
   * @return byte[] compressed pos values
   */
  byte[] nextPos() {
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
