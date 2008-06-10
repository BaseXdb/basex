package org.basex.util;

import static org.basex.util.Token.*;
import org.basex.core.Prop;

/**
 * Full-text tokenizer.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTTokenizer {
  /** Stemming instance. */
  private static final Stemming STEM = new Stemming();
  /** Textual contents. */
  private byte[] text;
  
  /** Stemming flag. */
  public boolean stem = Prop.ftstem;
  /** Diacritics flag. */
  public boolean dc = Prop.ftdc;
  /** Sensitivity flag. */
  public boolean sens = Prop.ftcs;
  /** Uppercase flag. */
  public boolean uc;
  /** Lowercase flag. */
  public boolean lc;
  /** Wildcard flag. */
  public boolean wc;
  /** Fuzzy flag. */
  public boolean fz;
  
  /** Current sentence. */
  public int sent;
  /** Current paragraph. */
  public int para;
  /** Current token. */
  public int pos = -1;
  /** Current character position. */
  public int p;
  /** Character start position. */
  public int s;
  
  /**
   * Empty constructor.
   */
  public FTTokenizer() { }
  
  /**
   * Constructor.
   * @param txt text
   */
  public FTTokenizer(final byte[] txt) {
    init(txt);
  }
  
  /**
   * Sets the text.
   * @param txt text
   */
  public void init(final byte[] txt) {
    text = txt;
    init();
  }
  
  /**
   * Initializes the iterator.
   */
  public void init() {
    sent = 0;
    para = 0;
    pos = -1;
    p = 0;
  }
  
  /**
   * Checks if more tokens are to be returned.
   * @return result of check
   */
  public boolean more() {
    final int l = text.length;
    pos++;

    // parse whitespaces
    boolean sn = false;
    boolean pa = false;
    for(; p < l; p += cl(text[p])) {
      final int c = cp(text, p);
      if(c == '.' && wc) break;
      
      if(!sn && (c == '.' || c == '!' || c == '?')) {
        sn = true;
        sent++;
      } else if(!pa && c == '\n') {
        pa = true;
        para++;
      } else if(Character.isLetterOrDigit(c)) {
        break;
      }
    }
    // end of text...
    s = p;
    if(p == l) return false;
    
    // parse token
    for(; p < l; p += cl(text[p])) {
      final int c = cp(text, p);
      if(Character.isLetterOrDigit(c)) continue;
      // [CG] parse wildcard indicators
      if(!wc || ws(c)) break;
    }
    return true;
  }
  
  /**
   * Returns the next token.
   * @return size
   */
  public byte[] next() {
    byte[] n = substring(text, s, p);
    if(!dc) n = dc(n);
    if(uc) n = uc(n);
    if(lc || !sens) n = lc(n);
    if(stem) n = STEM.word(n);
    return n;
  }
  
  /**
   * Returns the text size.
   * @return size
   */
  public int size() {
    return text.length;
  }
  
  @Override
  public String toString() {
    return "FTTokenizer[" + string(text) + "]";
  }
}
