package org.basex.util;

import java.io.BufferedReader;
import java.io.FileReader;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.query.ft.FTOpt;

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
  /** Flag for phrase processing. */
  private boolean phrase;
  
  /**
   * Empty constructor.
   * @param pr (optional) database properties
   */
  public ScoringTokenizer(final Prop pr) {
    super(Token.EMPTY, pr);
    readFrequency();
  }
  
  /**
   * Constructor.
   * @param txt text
   * @param fto full-text options
   * @param f fast evaluation
   * @param pr database properties
   */
  public ScoringTokenizer(final byte[] txt, final FTOpt fto, final boolean f,
      final Prop pr) {
    super(txt, fto, f, pr);
  }

  @Override
  public byte[] get() {
    return phrase ? super.text : super.get();
  }
  
  @Override
  public void init(final byte[] txt) {
    super.init(txt);
    initScoring();
    init();
  }

  /**
   * Sets flag for phrase processing.
   */
  public void phrase() {
    phrase = true;
  }
  
  /**
   * Returns the score for the specified key.
   * @param key key
   * @return score value
   */
  public int score(final byte[] key) {
    final int c = token.get(key);
    token.set(key, -1);
    if(c > 0 && freq != null) {
      final int f = freq.get(key);
      if(f > 0) {
        return Math.max(1, (int) (Math.log(2666130d / f) * c * 1000 / max));
      }
    }
    return c == 0 ? 0 : Math.max(1, c * 1000 / max);
  }

  /**
   * Reads the frequency out of an external file.
   */
  public void readFrequency() {
    try {
      final BufferedReader br = new BufferedReader(
          new FileReader("words.freq"));
      String l;
      freq = new IntMap();
      while((l = br.readLine()) != null) {
        final int i = l.indexOf(';');
        freq.add(Token.token(l.substring(0, i)),
            Token.toInt(l.substring(i + 1)));
      }
      br.close();
    } catch(final Exception ex) {
      Main.errln(ex);
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
