package org.basex.index;

import static org.basex.util.Token.*;
import org.basex.core.Prop;
import org.basex.util.Stemming;
import org.basex.util.TokenBuilder;
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
      if(wc && c == '.') break;

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
    if(wc) n = wc(n);
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
   * Count number of tokens, sentences and paragraphs.
   * [0] number of tokens
   * [1] number of sentences
   * [2] number of paragraphs
   * [3] number of lines needed
   *
   * @param ww width of a line in bytes
   * @return int[3]
   */
  public int[] countSenPar(final int ww) {
    int lass = 0;
    int lasp = 0;
    int lw = 0; // width of a line

    int[] c = new int[4];
    init();
    while(more()) {
      if (lw + p - s + 1 < ww) lw += p - s + 1;
      else {
        c[3]++;
        lw = 0;
      }

      c[0]++;
      if (sent != lass) {
        c[1]++;
        lass = sent;
      }
      if (para != lasp) {
        c[2]++;
        lasp = para;
        lw = 0;
        c[3]++;
      }
    }
    c[3]++;
    return c;
  }
  
  /**
   * Returns a wildcard token.
   * @param n input token
   * @return resulting token
   */
  private byte[] wc(final byte[] n) {
    if(!contains(n, '\\')) return n;
    final TokenBuilder tb = new TokenBuilder();
    boolean bs = false;
    for(byte c : n) {
      if(c == '\\') {
        bs = true;
      } else if(bs) {
        if(Character.isLetterOrDigit(c)) tb.add(c);
        bs = false;
      } else {
        tb.add(c);
      }
    }
    return tb.finish();
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
