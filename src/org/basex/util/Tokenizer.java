package org.basex.util;

import static org.basex.util.Token.*;
import java.util.Arrays;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.data.Data.Type;
import org.basex.index.IndexToken;
import org.basex.query.ft.FTOpt;
import org.basex.query.ft.StemDir;

/**
 * Full-text tokenizer.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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

  /** Current sentence. */
  public int sent;
  /** Current paragraph. */
  public int para;
  /** Current token. */
  public int pos;

  /** Text. */
  public byte[] text;
  /** Current character position. */
  public int p;
  /** Last punctuation mark. */
  public int pm;

  /** Character start position. */
  private int s;
  /** Number of tokens. */
  private int count;

  /**
   * Empty constructor.
   * @param pr (optional) database properties
   */
  public Tokenizer(final Prop pr) {
    this(Token.EMPTY, pr);
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

  public Type type() {
    return Type.FTX;
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

    // parse whitespaces
    boolean sn = false;
    boolean pa = false;
    boolean bs = false;
    for(; p < l; p += cl(text[p])) {
      final int c = cp(text, p);
      if(wc && !bs) {
        bs = c == '\\';
        if(bs) continue;
        if(c == '.') break;
      }
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
    for(; p < l; p += cl(text[p])) {
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

  public byte[] get() {
    return get(orig());
  }

  /**
   * Returns a normalized version of the specified token.
   * @param tok input token
   * @return result
   */
  public byte[] get(final byte[] tok) {
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
  public byte[] orig() {
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
   * Calculates a position value, dependent on the specified unit.
   * Once calculated values are cached.
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
   * Note that this method does only support the conversion of the standard
   * character set.
   * @param t token to be converted
   * @param a ascii flag
   * @return converted token
   */
  private static byte[] dia(final byte[] t, final boolean a) {
    if(a) return t;
    final String s = utf8(t, 0, t.length);
    final TokenBuilder sb = new TokenBuilder();
    final int jl = s.length();
    for(int j = 0; j < jl; j++) sb.addUTF(Token.norm(s.charAt(j)));
    return sb.finish();
  }

  /**
   * Converts the specified token to upper case.
   * @param t token to be converted
   * @param a ascii flag
   * @return the converted token
   */
  private static byte[] upper(final byte[] t, final boolean a) {
    if(!a) return token(string(t).toUpperCase());
    for(int i = 0; i < t.length; i++) t[i] = (byte) uc(t[i]);
    return t;
  }

  /**
   * Converts the specified token to lower case.
   * @param t token to be converted
   * @param a ascii flag
   * @return the converted token
   */
  private static byte[] lower(final byte[] t, final boolean a) {
    if(!a) return token(string(t).toLowerCase());
    for(int i = 0; i < t.length; i++) t[i] = (byte) lc(t[i]);
    return t;
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

    return new int[][] { il[0].finish(), il[1].finish(), il[2].finish(),
        il[3].finish(), il[4].finish()};
  }

  @Override
  public String toString() {
    return Main.name(this) + '[' + string(text) + ']';
  }
}
