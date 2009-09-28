package org.basex.util;

import java.io.BufferedReader;
import java.io.FileReader;

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
  /** Container for frequency. */
  private IntMap freq;

  /**
   * Empty constructor.
   * @param pr (optional) database properties
   */
  public ScoringTokenizer(final Prop pr) {
    super(Token.EMPTY, pr);
    readFrequency();
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
    if (c > 0 && freq != null) {
      final int f = freq.get(key);
      if (f > 0) {
        return (int) (Math.log(2666130 / f) * c * 1000 / max);
      }
    }
    
    return c == 0 ? 0 : c * 1000 /  max;
  }

  /**
   * Read frequency out of an external file.
   */
  public void readFrequency() {
    try {
      final BufferedReader br = new BufferedReader(
          new FileReader("words.freq"));
      String l;
      freq = new IntMap();
      while((l = br.readLine()) != null) {
        final int i = l.indexOf(';');
        freq.add(l.substring(0, i).getBytes(), 
            Integer.valueOf(l.substring(i + 1)));
      }
                
      br.close();
    } catch (Exception e) {
      
    }
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
