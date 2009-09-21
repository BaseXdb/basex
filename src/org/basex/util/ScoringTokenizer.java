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
  private Map<Integer> token = new Map<Integer>();
  /** Maximum score. */
  private int max = 0;

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
    super.init();
  }

  /**
   * Returns the score for the specified key.
   * @param key key
   * @return score value
   */
  public int score(final byte[] key) {
    int c = token.get(key);
    return c == 0 ? 0 : c * 1000 /  max;
  }

  /**
   * Initializes the scoring process.
   */
  private void initScoring() {
    while(super.more()) {
      final byte[] b = super.get();
      Integer c = token.get(b);
      if(c != null) {
        System.out.println(new String(b) + " " + (c + 1));
        token.set(b, ++c);
        if(c > max) max = c;
      } else token.add(b, 1);
    }
  }
}
