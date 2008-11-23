package org.basex.index;

import static org.basex.util.Token.*;
import org.basex.core.Prop;
import org.basex.util.Stemming;
import org.basex.util.TokenList;

/**
 * Full-text tokenizer.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTTokenizer extends IndexToken {
  /** Stemming instance. */
  private final Stemming stem = new Stemming();
  /** Stemming flag. */
  public boolean st = Prop.ftst;
  /** Diacritics flag. */
  public boolean dc = Prop.ftdc;
  /** Sensitivity flag. */
  public boolean cs = Prop.ftcs;
  /** Uppercase flag. */
  public boolean uc;
  /** Lowercase flag. */
  public boolean lc;
  /** Wildcard flag. */
  public boolean wc;
  /** Fuzzy flag. */
  public boolean fz;
  /** Flag for loading ftposition data. */
  public boolean lp;

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
  public FTTokenizer() {
    super(Type.FTX);
  }

  /**
   * Constructor.
   * @param txt text
   */
  public FTTokenizer(final byte[] txt) {
    this();
    text = txt;
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
      } else if(c < 0x100 && Character.isLetterOrDigit(c)) {
        break;
      }
    }
    // end of text...
    s = p;
    if(p == l) return false;

    // parse token
    for(; p < l; p += cl(text[p])) {
      final int c = cp(text, p);
      if(c < 0x100 && Character.isLetterOrDigit(c)) continue;
      // [CG] FT/parse wildcard indicators
      if(!wc || ws(c)) break;
    }
    return true;
  }
  
  @Override
  public byte[] get() {
    byte[] n = substring(text, s, p);
    if(!dc) n = dc(n);
    if(uc) n = uc(n);
    if(lc || !cs) n = lc(n);
    if(st) n = stem.word(n);
    return n;
  }

  /**
   * Counts the number of tokens.
   * @return number of tokens
   */
  public int count() {
    init();
    while(more());
    return pos;
  }

  /**
   * Returns the text size.
   * @return size
   */
  public int size() {
    return text.length;
  }

  /**
   * Converts the tokens to a TokenList.
   * @return TokenList
   */
  public TokenList getTokenList() {
    final TokenList tl = new TokenList();
    init();
    while(more()) tl.add(get());
    return tl;
  }

  @Override
  public String toString() {
    return "FTTokenizer[" + string(text) + "]";
  }
}
