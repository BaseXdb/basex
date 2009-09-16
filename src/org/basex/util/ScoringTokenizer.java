package org.basex.util;

import java.util.HashMap;

import org.basex.core.Prop;

public final class ScoringTokenizer extends Tokenizer{
  private Map<Integer> token = new Map<Integer>(); 
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

  public int score(final byte[] key) {
    int c = token.get(key);
    if (c == 0) return 0;
    return c * 1000 /  max; 
  }
  
  private void initScoring() {
    while(super.more()) {
      final byte[] b = super.get();
      Integer c = token.get(b); 
      if (c != null) {
        System.out.println(new String(b) + " " + (c+1));
        token.setValue(b, ++c);
        if (c > max) max = c;
      } else token.add(b, 1);            
    }
  }
  
}
