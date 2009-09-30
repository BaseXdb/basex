package org.basex.index;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.util.ScoringTokenizer;
import org.basex.util.Token;
import org.basex.util.Tokenizer;

/**
 * This class provides a skeleton for full-text index builders.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
abstract class FTBuilder extends IndexBuilder {
  /** Word parser. */
  final Tokenizer wp;

  /**
   * Constructor.
   * @param d data reference
   * @param pr database properties
   */
  protected FTBuilder(final Data d, final Prop pr) {
    super(d);
    if (pr.is(Prop.INDEXSCORES)) 
      wp = new ScoringTokenizer(pr);
    else 
      wp = new Tokenizer(pr);
  }

  /**
   * Extracts and indexes words from the specified data reference.
   * @throws IOException IO exception
   */
  final void index() throws IOException {
    final String[] s = {"wonder girls", "cloud computing", "evidence theory",
    "dempster schafer", "rabindranath tagore", "danny boyle", 
    "hard disk", "second world war", "stock exchange", "insider trading",
    "game show", "hatha yoga", "world wide web", "french revolution", 
    "global warming", "human activity", "virtual museums"};
    int p;
    int i, j;
    for(id = 0; id < total; id++) {
      if(data.kind(id) != Data.TEXT) continue;
      checkStop();
      p = 0;
      i = 0;
      j = 0;
      wp.init(data.text(id));
      while(wp.more()) {
        final byte[] tok = wp.get();
        // skip too long tokens
        if(tok.length <= Token.MAXLEN) index(tok);
        
        // [SG] INEX index phrases
        if (wp instanceof ScoringTokenizer) {
          if (j == 0) {
            for (i = 0; i < s.length; i++) { 
              if(s[i].indexOf(new String(tok)) == 0) {
                p++;
                j += tok.length + 1;
                break;
              }
            }
          } else {
            if (j > 0 && s[i].length() >= j + tok.length 
                && s[i].substring(j, j + tok.length).equals(new String(tok))) {
              if (j + tok.length == s[i].length()) {
                index(s[i].getBytes());
                j = 0;
              } else {
                j += tok.length + 1;
              }
            } else j = 0;
          }                        
        }          
      }
    }
    write();
  }

  /**
   * Indexes a single token.
   * @param tok token to be indexed
   */
  abstract void index(final byte[] tok);

  /**
   * Writes the index data to disk.
   * @throws IOException I/O exception
   */
  abstract void write() throws IOException;

  @Override
  public final String det() {
    return INDEXFTX;
  }
}
