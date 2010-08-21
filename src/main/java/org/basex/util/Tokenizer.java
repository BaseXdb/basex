package org.basex.util;

import static org.basex.util.Token.*;
import java.util.Arrays;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.index.IndexToken;
import org.basex.query.ft.FTOpt;
import org.basex.query.ft.StemDir;

/**
 * Full-text tokenizer.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Tokenizer implements IndexToken {
  /** Units. */
  public enum FTUnit {
    /** Word unit. */      WORD,
    /** Sentence unit. */  SENTENCE,
    /** Paragraph unit. */ PARAGRAPH;

    /**
     * Returns a string representation.
     * @return string representation
     */
    @Override
    public String toString() { return name().toLowerCase(); }
  }

  /** Stemming instance. */
  private final Stemming stem = new Stemming();
  /** Cached sentence positions. */
  private final IntList sen = new IntList();
  /** Cached paragraph positions. */
  private final IntList par = new IntList();

  /** Stemming dictionary. */
  public StemDir sd;
  /** Stemming flag. */
  public boolean st;
  /** Diacritics flag. */
  public boolean dc;
  /** Sensitivity flag. */
  public boolean cs;
  /** Uppercase flag. */
  public boolean uc;
  /** Lowercase flag. */
  public boolean lc;
  /** Wildcard flag. */
  public boolean wc;
  /** Fuzzy flag. */
  public boolean fz;
  /** Fast evaluation flag. */
  public boolean fast;
  /** Flag for a paragraph. */
  public boolean pa;

  /** Current token. */
  public int pos;

  /** Text. */
  public byte[] text;
  /** Current character position. */
  public int p;

  /** Current sentence. */
  private int sent;
  /** Current paragraph. */
  private int para;
  /** Last punctuation mark. */
  private int pm;
  /** Last character position. */
  private int lp;

  /** Character start position. */
  private int s;
  /** Number of tokens. */
  private int count;
  /** Flag indicating a new sentence. */
  private boolean sc;

  /**
   * Empty constructor.
   * @param pr (optional) database properties
   */
  public Tokenizer(final Prop pr) {
    this(EMPTY, pr);
  }

  /**
   * Constructor.
   * @param pr (optional) database properties
   * @param txt text
   */
  public Tokenizer(final byte[] txt, final Prop pr) {
    init(txt);
    if(pr != null) {
      st = pr.is(Prop.STEMMING);
      dc = pr.is(Prop.DIACRITICS);
      cs = pr.is(Prop.CASESENS);
    }
  }

  @Override
  public IndexType type() {
    return IndexType.FTXT;
  }

  /**
   * Constructor.
   * @param txt text
   * @param fto full-text options
   * @param f fast evaluation
   * @param pr database properties
   */
  public Tokenizer(final byte[] txt, final FTOpt fto, final boolean f,
      final Prop pr) {
    this(txt, pr);
    lc = fto.is(FTOpt.LC);
    uc = fto.is(FTOpt.UC);
    cs = fto.is(FTOpt.CS);
    wc = fto.is(FTOpt.WC);
    fz = fto.is(FTOpt.FZ);
    st = fto.is(FTOpt.ST);
    dc = fto.is(FTOpt.DC);
    sd = fto.sd;
    fast = f;
  }

  /**
   * Sets the text.
   * @param txt text
   */
  public void init(final byte[] txt) {
    if(text != txt) {
      count = -1;
      text = txt;
      sen.reset();
      par.reset();
    }
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

    lp = p;
    // parse whitespaces
    boolean sn = false;
    pa = false;
    boolean bs = false;
    for(; p < l; p += cl(text, p)) {
      final int c = cp(text, p);
      if(wc && !bs) {
        bs = c == '\\';
        if(bs) continue;
        if(c == '.') break;
      }
      // [CG] XQFT: support other languages (Jap./Chin.: U+3002, etc.)
      if(!sn && (c == '.' || c == '!' || c == '?')) {
        sn = true;
        sent++;
        pm = c;
      } else if(!pa && c == '\n') {
        pa = true;
        para++;
      } else if(ftChar(c)) {
        if(bs) p--;
        break;
      }
      bs = false;
    }
    // end of text...
    s = p;
    if(p == l) return false;

    // parse token
    for(; p < l; p += cl(text, p)) {
      int c = cp(text, p);
      // parse wildcards
      if(wc && !bs) {
        bs = c == '\\';
        if(bs) continue;
        if(c == '.') {
          c = p + 1 < l ? text[p + 1] : 0;
          if(c == '?' || c == '*' || c == '+') {
            ++p;
          } else if(c == '{') {
            while(++p < l && text[p] != '}');
            if(p == l) break;
          }
          continue;
        }
      }
      if(!ftChar(c)) {
        if(bs) p--;
        break;
      }
      bs = false;
    }
    return true;
  }

  @Override
  public byte[] get() {
    return get(orig());
  }

  /**
   * Returns a normalized version of the specified token.
   * @param tok input token
   * @return result
   */
  private byte[] get(final byte[] tok) {
    byte[] n = tok;
    final boolean a = ascii(n);
    if(!dc) n = dia(n, a);
    if(uc) n = upper(n, a);
    if(lc || !cs) n = lower(n, a);
    if(st) n = sd == null ? stem.stem(n) : sd.stem(n);
    return n;
  }

  /**
   * Returns the original token.
   * @return original token
   */
  private byte[] orig() {
    return Arrays.copyOfRange(text, s, p);
  }

  /**
   * Counts the number of tokens.
   * @return number of tokens
   */
  public int count() {
    if(count == -1) {
      init();
      while(more());
      count = pos;
    }
    return count;
  }

  /**
   * Checks if more tokens are to be returned; special characters are included.
   * @return result of check
   */
  public boolean moreSC() {
    final int l = text.length;
    // parse whitespaces
    pa = false;
    sc = false;
    lp = p;
    for(; p < l; p += cl(text, p)) {
      final int c = cp(text, p);
      if(c == '\n') {
        pa = true;
        p++;
        sc = true;
        break;
      } else if(ftChar(c)) {
        break;
      }
      sc = true;
    }

    // special chars found
    if(lp < p) return true;
    pos++;

    // end of text...
    s = p;
    if(p == l) return false;

    // parse token
    for(; p < l; p += cl(text, p)) {
      final int c = cp(text, p);
      if(!ftChar(c)) {
        s = p - cl(text, p);
        break;
      }
    }
    return true;
  }

  /**
   * Returns true if the current token is a special char.
   * @return boolean
   */
  public boolean isSC() {
    return sc;
  }

  /**
   * Get next token, including special characters.
   * @return next token
   */
  public byte[] nextSC() {
    return lp < p ? Arrays.copyOfRange(text, lp, p)
        : Arrays.copyOfRange(text, p, s);
  }

  /**
   * Calculates a position value, dependent on the specified unit.
   * @param w word position
   * @param u unit
   * @return new position
   */
  public int pos(final int w, final FTUnit u) {
    if(u == FTUnit.WORD) return w;

    // if necessary, calculate sentences and paragraphs
    final IntList il = u == FTUnit.SENTENCE ? sen : par;
    if(sen.size() == 0) {
      init();
      while(more()) {
        sen.add(sent);
        par.add(para);
      }
    }
    return il.get(w);
  }

  /**
   * Removes diacritics from the specified token.
   * This method supports all latin1 characters, including supplements.
   * @param t token to be converted
   * @param a ascii flag
   * @return converted token
   */
  private static byte[] dia(final byte[] t, final boolean a) {
    if(a) return t;

    // find first character to be normalized
    final int tl = t.length;
    for(int i = 0; i < tl; i += cl(t, i)) {
      final int c = cp(t, i);
      // normalized character found; run conversion
      if(c != norm(c)) {
        final TokenBuilder tb = new TokenBuilder();
        tb.add(t, 0, i);
        for(int j = i; j < tl; j += cl(t, j)) tb.addUTF(norm(cp(t, j)));
        return tb.finish();
      }
    }
    // return original character
    return t;
  }

  /**
   * Converts the specified token to upper case.
   * @param t token to be converted
   * @param a ascii flag
   * @return the converted token
   */
  private static byte[] upper(final byte[] t, final boolean a) {
    final int tl = t.length;
    if(a) {
      for(int i = 0; i < tl; ++i) t[i] = (byte) uc(t[i]);
      return t;
    }
    final TokenBuilder tb = new TokenBuilder();
    for(int i = 0; i < tl; i += cl(t, i)) tb.addUTF(uc(cp(t, i)));
    return tb.finish();
  }

  /**
   * Converts the specified token to lower case.
   * @param t token to be converted
   * @param a ascii flag
   * @return the converted token
   */
  private static byte[] lower(final byte[] t, final boolean a) {
    final int tl = t.length;
    if(a) {
      for(int i = 0; i < tl; ++i) t[i] = (byte) lc(t[i]);
      return t;
    }
    final TokenBuilder tb = new TokenBuilder();
    for(int i = 0; i < tl; i += cl(t, i)) tb.addUTF(lc(cp(t, i)));
    return tb.finish();
  }

  /**
   * Gets full-text info for the specified token; needed for visualizations.
   * int[0]: length of each token
   * int[1]: sentence info, length of each sentence
   * int[2]: paragraph info, length of each paragraph
   * int[3]: each token as int[]
   * int[4]: punctuation marks of each sentence
   * @param t text to be parsed
   * @return int arrays
   */
  public static int[][] getInfo(final byte[] t) {
    final Tokenizer tok = new Tokenizer(t, null);
    final IntList[] il = new IntList[] { new IntList(), new IntList(),
        new IntList(), new IntList(), new IntList()};
    int lass = 0;
    int lasp = 0;
    int sl = 0;
    int pl = 0;
    while(tok.more()) {
      final byte[] n = tok.orig();
      final int l = n.length;
      il[0].add(l);
      for(final byte b : n) il[3].add(b);

      if(tok.sent != lass) {
        if(sl > 0) {
          il[1].add(sl);
          il[4].add(tok.pm);
        }
        lass = tok.sent;
        sl = 0;
      }
      if(tok.para != lasp) {
        if(pl > 0) il[2].add(pl);
        lasp = tok.para;
        pl = 0;
      }

      sl += l;
      pl += l;
    }

    if(tok.sent != lass && sl > 0) {
      il[1].add(sl);
      il[4].add(tok.pm);
    }
    if(pl > 0) il[2].add(pl);

    // last sentence not finished with a punctuation mark
    il[1].add(sl + 1);

    return new int[][] { il[0].toArray(), il[1].toArray(), il[2].toArray(),
        il[3].toArray(), il[4].toArray()};
  }

  @Override
  public String toString() {
    return Main.name(this) + '[' + string(text) + ']';
  }
}
