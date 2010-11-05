package org.basex.util.ft;

import static org.basex.util.Token.*;
import static org.basex.util.ft.FTOptions.*;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import org.basex.core.Prop;
import org.basex.query.ft.FTOpt;
import org.basex.util.IntList;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;
import org.basex.util.ft.FTLexer.FTUnit;

/**
 * Full-text tokenizer.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class BaseXTokenizer extends Tokenizer {
  /** Cached sentence positions. */
  private final IntList sen = new IntList();
  /** Cached paragraph positions. */
  private final IntList par = new IntList();

  /** Diacritics flag. */
  private boolean dc;
  /** Sensitivity flag. */
  private boolean cs;
  /** Uppercase flag. */
  private boolean uc;
  /** Lowercase flag. */
  private boolean lc;
  /** Wildcard flag. */
  private boolean wc;
  /** Flag for a paragraph. */
  private boolean pa;

  /** Text. */
  private byte[] text;
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

  /** Current token. */
  int pos;
  /** Current character position. */
  int p;
  /** Flag indicating a special character. */
  boolean sc;

  /**
   * Empty constructor.
   * @param pr (optional) database properties
   */
  BaseXTokenizer(final Prop pr) {
    this(EMPTY, pr);
  }

  /**
   * Constructor.
   * @param pr (optional) database properties
   * @param txt text
   */
  private BaseXTokenizer(final byte[] txt, final Prop pr) {
    init(txt);
    if(pr != null) {
      dc = pr.is(Prop.DIACRITICS);
      cs = pr.is(Prop.CASESENS);
    }
  }

  /**
   * Constructor.
   * @param txt text
   * @param fto full-text options
   * @param pr database properties
   */
  private BaseXTokenizer(final byte[] txt, final FTOpt fto, final Prop pr) {
    this(txt, pr);
    setFTOpt(fto);
  }

  @Override
  Tokenizer newInstance(final byte[] txt, final Prop pr, final FTOpt f) {
    return new BaseXTokenizer(txt, f, pr);
  }

  /**
   * Set full-text options.
   * @param fto new full-text option
   */
  private void setFTOpt(final FTOpt fto) {
    if(null != fto) {
      lc = fto.is(LC);
      uc = fto.is(UC);
      cs = fto.is(CS);
      wc = fto.is(WC);
      dc = fto.is(DC);
    }
  }

  /**
   * Sets the text.
   * @param txt text
   */
  private void init(final byte[] txt) {
    if(text != txt) {
      text = txt;
      sen.reset();
      par.reset();
    }
    init();
  }

  /**
   * Initializes the iterator.
   */
  void init() {
    sent = 0;
    para = 0;
    pos = -1;
    p = 0;
  }

  /**
   * Scans the next token and returns {@code true} if more tokens can be
   * returned.
   * @return result of check
   */
  boolean more() {
    final int l = text.length;
    ++pos;

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
        ++sent;
        pm = c;
      } else if(!pa && c == '\n') {
        pa = true;
        ++para;
      } else if(ftChar(c)) {
        if(bs) --p;
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
            while(++p < l && text[p] != '}')
              ;
            if(p == l) break;
          }
          continue;
        }
      }
      if(!ftChar(c)) {
        if(bs) --p;
        break;
      }
      bs = false;
    }
    return true;
  }

  /**
   * Get the current token applying filters before returning the result.
   * @return byte array representing the filtered token
   */
  byte[] get() {
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
   * Checks if more tokens are to be returned; special characters are included.
   * @return result of check
   */
  boolean moreSC() {
    final int l = text.length;
    // parse whitespaces
    pa = false;
    sc = false;
    lp = p;
    for(; p < l; p += cl(text, p)) {
      final int c = cp(text, p);
      if(c == '\n') {
        pa = true;
        ++p;
        sc = true;
        break;
      } else if(ftChar(c)) {
        break;
      }
      sc = true;
    }

    // special chars found
    if(lp < p) return true;
    ++pos;

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
   * Get next token, including special characters.
   * @return next token
   */
  byte[] getSC() {
    return lp < p ? Arrays.copyOfRange(text, lp, p) : Arrays.copyOfRange(text,
        p, s);
  }

  @Override
  int pos(final int w, final FTUnit u) {
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
   * Removes diacritics from the specified token. This method supports all
   * latin1 characters, including supplements.
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
        for(int j = i; j < tl; j += cl(t, j))
          tb.add(norm(cp(t, j)));
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
      for(int i = 0; i < tl; ++i)
        t[i] = (byte) uc(t[i]);
      return t;
    }
    final TokenBuilder tb = new TokenBuilder();
    for(int i = 0; i < tl; i += cl(t, i))
      tb.add(uc(cp(t, i)));
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
      for(int i = 0; i < tl; ++i)
        t[i] = (byte) lc(t[i]);
      return t;
    }
    final TokenBuilder tb = new TokenBuilder();
    for(int i = 0; i < tl; i += cl(t, i))
      tb.add(lc(cp(t, i)));
    return tb.finish();
  }

  @Override
  int[][] getInfo(final byte[] t) {
    final BaseXTokenizer tok = new BaseXTokenizer(t, null);
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
      for(final byte b : n)
        il[3].add(b);

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
    return Util.name(this) + '[' + string(text) + ']';
  }

  @Override
  int getPrecedence() {
    return 1000;
  }

  @Override
  public Iterator<Span> iterator() {
    return new Iterator<Span>() {
      { init(); }
      int next;

      @Override
      public boolean hasNext() {
        if(next <= 0 && (specialChars ? moreSC() : more())) next++;
        return next > 0;
      }

      @Override
      public Span next() {
        if(--next < 0) hasNext();
        return new Span(get(), p, p, pos, sc);
      }

      @Override
      public void remove() {
        Util.notimplemented();
      }
    };
  }

  @Override
  EnumSet<LanguageTokens> supportedLanguages() {
    return LanguageTokens.wsTokenizable();
  }

  @Override
  boolean isParagraph() {
    return pa;
  }
}
