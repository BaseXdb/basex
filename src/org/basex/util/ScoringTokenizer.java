package org.basex.util;

import org.basex.core.Prop;

/**
 * Full-text tokenizer with scoring support.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Sebastian Gath
 */
public final class ScoringTokenizer extends Tokenizer{
  /** Token map. */
  private IntMap token;
  /** Maximum score. */
  private int max = 1;

  /**
   * Empty constructor.
   * @param pr (optional) database properties
   */
  public ScoringTokenizer(final Prop pr) {
    super(Token.EMPTY, pr);
  }

  @Override
  public void init(final byte[] txt) {
    super.init(txt);    
    initScoring();
    init();
  }

  /**
   * Returns the score for the specified key.
   * @param key key
   * @return score value
   */
  public int score(final byte[] key) {
    final int c = token.get(key);
    token.set(key, -1);
    return c == 0 ? 0 : c * 1000 /  max;
  }

  /**
   * Initializes the scoring process.
   */
  private void initScoring() {
    token = new IntMap();
    while(more()) {
      final byte[] b = get();
      int c = token.get(b);
      if(c != 0) {
        token.set(b, ++c);
        if(c > max) max = c;
      } else { 
        token.add(b, 1);
      }
    }
  }
}
