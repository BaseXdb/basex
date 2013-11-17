package org.basex.util.ft;

import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;

import java.util.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Full-text tokenizer.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class WesternTokenizer extends Tokenizer {
  /** Supported languages. */
  private static final HashSet<Language> SUPPORTED = new HashSet<Language>();

  static {
    final String[] nonw = { "ar", "ja", "ko", "th", "zh" };
    for(final Language l : Language.ALL.values()) {
      if(!eq(l.code(), nonw)) SUPPORTED.add(l);
    }
  }

  /** Cached sentence positions. */
  private final IntList sen = new IntList();
  /** Cached paragraph positions. */
  private final IntList par = new IntList();

  /** Case option. */
  private final FTCase cs;
  /** Diacritics flag. */
  private final boolean dc;
  /** Wildcard flag. */
  private final boolean wc;
  /** Flag for a paragraph. */
  private boolean pa;

  /** Text. */
  private byte[] text = EMPTY;
  /** Current sentence. */
  private int sent;
  /** Current paragraph. */
  private int para;
  /** Last punctuation mark. */
  private int pm;
  /** Last character position. */
  private int lp;
  /** Character start position. */
  private int spos;

  /** Current token position. */
  private int pos = -1;
  /** Current character position. */
  private int cpos;
  /** Flag indicating a special character. */
  private boolean sc;
  /** Next pointer. */
  private int next;

  /**
   * Constructor.
   * @param fto full-text options
   */
  public WesternTokenizer(final FTOpt fto) {
    cs = fto != null && fto.cs != null ? fto.cs : FTCase.INSENSITIVE;
    wc = fto != null && fto.is(WC);
    dc = fto != null && fto.is(DC);
  }

  @Override
  Collection<Language> languages() {
    return SUPPORTED;
  }

  @Override
  Tokenizer get(final FTOpt f) {
    return new WesternTokenizer(f);
  }

  @Override
  public WesternTokenizer init(final byte[] txt) {
    if(text != txt) {
      text = txt;
      sen.reset();
      par.reset();
    }
    init();
    return this;
  }

  /**
   * Initializes the iterator.
   */
  private void init() {
    sent = 0;
    para = 0;
    pos = -1;
    cpos = 0;
    next = 0;
  }

  @Override
  public boolean hasNext() {
    if(next <= 0 && (special ? moreSC() : more())) next++;
    return next > 0;
  }

  @Override
  public FTSpan next() {
    return new FTSpan(nextToken(), pos, sc);
  }

  @Override
  public byte[] nextToken() {
    if(--next < 0) hasNext();
    return special ? getSC() : get();
  }

  /**
   * Scans the next token and returns {@code true} if more tokens can be
   * returned.
   * @return result of check
   */
  private boolean more() {
    final int l = text.length;
    ++pos;

    // parse whitespaces
    lp = cpos;
    pa = false;
    boolean bs = false;
    for(boolean sn = false; cpos < l; cpos += cl(text, cpos)) {
      final int c = cp(text, cpos);
      if(wc && !bs) {
        bs = c == '\\';
        if(bs) continue;
        if(c == '.') break;
      }
      if(!sn && (c == '.' || c == '!' || c == '?')) {
        sn = true;
        ++sent;
        pm = c;
      } else if(!pa && c == '\n') {
        pa = true;
        ++para;
      } else if(ftChar(c)) {
        if(bs) {
          // backslash (bs) followed by any character is the character itself:
          --cpos;
          bs = false;
        }
        break;
      }
      bs = false;
    }
    // end of text...
    spos = cpos;
    if(cpos == l) return false;

    // parse token
    for(; cpos < l; cpos += cl(text, cpos)) {
      int c = cp(text, cpos);
      // parse wildcards
      if(wc && !bs) {
        bs = c == '\\';
        if(bs) continue;
        if(c == '.') {
          c = cpos + 1 < l ? text[cpos + 1] : 0;
          if(c == '?' || c == '*' || c == '+') {
            ++cpos;
          } else if(c == '{') {
            while(++cpos < l && text[cpos] != '}');
            if(cpos == l) break;
          }
          continue;
        }
      }
      if(!ftChar(c)) {
        if(bs) --cpos;
        break;
      }
      bs = false;
    }
    return true;
  }

  /**
   * Returns a normalized version of the current token.
   * @return result
   */
  private byte[] get() {
    byte[] n = orig();
    final boolean a = ascii(n);
    if(!a && !dc) n = dia(n);
    if(cs == FTCase.UPPER) n = upper(n, a);
    else if(cs != FTCase.SENSITIVE) n = lower(n, a);
    return n;
  }

  /**
   * Returns the original token.
   * @return original token
   */
  private byte[] orig() {
    final int l = cpos - spos;
    final byte[] copy = new byte[l];
    System.arraycopy(text, spos, copy, 0, l);
    return copy;
  }

  /**
   * Checks if more tokens are to be returned; special characters are included.
   * @return result of check
   */
  private boolean moreSC() {
    final int l = text.length;
    // parse whitespaces
    pa = false;
    sc = false;
    lp = cpos;
    for(; cpos < l; cpos += cl(text, cpos)) {
      final int c = cp(text, cpos);
      if(c == '\n') {
        pa = true;
        ++cpos;
        sc = true;
        break;
      }
      if(ftChar(c)) {
        break;
      }
      sc = true;
    }

    // special chars found
    if(lp < cpos) return true;
    ++pos;

    // end of text...
    spos = cpos;
    if(cpos == l) return false;

    // parse token
    for(; cpos < l; cpos += cl(text, cpos)) {
      final int c = cp(text, cpos);
      if(!ftChar(c)) {
        spos = cpos - cl(text, cpos);
        break;
      }
    }
    return true;
  }

  /**
   * Get next token, including special characters.
   * @return next token
   */
  private byte[] getSC() {
    return lp < cpos ? Arrays.copyOfRange(text, lp, cpos) :
      Arrays.copyOfRange(text, cpos, spos);
  }

  @Override
  int pos(final int w, final FTUnit u) {
    if(u == FTUnit.WORDS) return w;

    // if necessary, calculate sentences and paragraphs
    final IntList il = u == FTUnit.SENTENCES ? sen : par;
    if(sen.isEmpty()) {
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
   * @return converted token
   */
  static byte[] dia(final byte[] t) {
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
  static byte[] upper(final byte[] t, final boolean a) {
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
  static byte[] lower(final byte[] t, final boolean a) {
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
  int[][] info() {
    init();
    final IntList[] il = { new IntList(), new IntList(),
        new IntList(), new IntList(), new IntList()};
    int lass = 0;
    int lasp = 0;
    int sl = 0;
    int pl = 0;

    while(more()) {
      final byte[] n = orig();
      final int l = n.length;
      il[0].add(l);
      for(final byte b : n) il[3].add(b);

      if(sent != lass) {
        if(sl > 0) {
          il[1].add(sl);
          il[4].add(pm);
        }
        lass = sent;
        sl = 0;
      }
      if(para != lasp) {
        if(pl > 0) il[2].add(pl);
        lasp = para;
        pl = 0;
      }
      sl += l;
      pl += l;
    }

    if(sent != lass && sl > 0) {
      il[1].add(sl);
      il[4].add(pm);
    }
    if(pl > 0) il[2].add(pl);

    // last sentence not finished with a punctuation mark
    il[1].add(sl + 1);

    return new int[][] { il[0].toArray(), il[1].toArray(), il[2].toArray(),
        il[3].toArray(), il[4].toArray()};
  }

  @Override
  protected byte prec() {
    return 10;
  }

  @Override
  boolean paragraph() {
    return pa;
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + string(text) + ']';
  }
}
