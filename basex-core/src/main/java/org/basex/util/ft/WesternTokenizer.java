package org.basex.util.ft;

import static org.basex.util.Token.*;
import static org.basex.util.FTToken.*;
import static org.basex.util.ft.FTFlag.*;

import java.util.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Full-text tokenizer.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class WesternTokenizer extends Tokenizer {
  /** Supported languages. */
  private static final HashSet<Language> SUPPORTED = new HashSet<>();

  static {
    final String[] nonw = { "ar", "ja", "ko", "th", "zh" };
    for(final Language l : Language.ALL.values()) {
      if(!eq(l.code(), nonw)) SUPPORTED.add(l);
    }
  }

  /** Cached sentence positions. */
  private final IntList sentPos = new IntList();
  /** Cached paragraph positions. */
  private final IntList paraPos = new IntList();

  /** Case option. */
  private final FTCase casesens;
  /** Diacritics flag. */
  private final boolean diacritics;
  /** Wildcard flag. */
  private final boolean wildcards;

  /** Text. */
  private byte[] text = EMPTY;
  /** Current sentence counter. */
  private int sentence;
  /** Current paragraph counter. */
  private int paragraph;
  /** Current punctuation mark. */
  private int punct;
  /** Current start position. */
  private int spos;
  /** Current end position. */
  private int epos;

  /** Current token position. */
  private int pos = -1;
  /** Next pointer. */
  private int next;

  /** Flag for a paragraph. */
  private boolean para;
  /** Flag indicating a special character. */
  private boolean spec;

  /**
   * Constructor.
   * @param fto full-text options
   */
  public WesternTokenizer(final FTOpt fto) {
    casesens = fto != null && fto.cs != null ? fto.cs : FTCase.INSENSITIVE;
    wildcards = fto != null && fto.is(WC);
    diacritics = fto != null && fto.is(DC);
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
      sentPos.reset();
      paraPos.reset();
    }
    init();
    return this;
  }

  /**
   * Initializes the iterator.
   */
  private void init() {
    sentence = 0;
    paragraph = 0;
    pos = -1;
    epos = 0;
    next = 0;
  }

  @Override
  public boolean hasNext() {
    int n = next;
    if(n <= 0 && (all ? moreAll() : more())) next = ++n;
    return n > 0;
  }

  @Override
  public FTSpan next() {
    return new FTSpan(nextToken(), pos, spec);
  }

  @Override
  public byte[] nextToken() {
    if(--next < 0) hasNext();

    byte[] t = token();
    if(all) return t;

    final boolean a = ascii(t);
    if(!a && !diacritics) t = noDiacritics(t);
    final FTCase cs = casesens;
    if(cs == FTCase.UPPER) t = upper(t, a);
    else if(cs != FTCase.SENSITIVE) t = lower(t, a);
    return t;
  }

  /**
   * Scans the next token and returns {@code true} if more tokens can be returned.
   * @return result of check
   */
  private boolean more() {
    final boolean wc = wildcards;
    final byte[] txt = text;
    final int txtl = txt.length;

    // parse whitespaces
    int cp = epos;
    boolean bs = false, pa = false, sn = false;
    for(; cp < txtl; cp += cl(txt, cp)) {
      final int ch = cp(txt, cp);
      if(wc && !bs) {
        bs = ch == '\\';
        if(bs) continue;
        if(ch == '.') break;
      }
      if(!sn && (ch == '.' || ch == '!' || ch == '?')) {
        sn = true;
        ++sentence;
        punct = ch;
      } else if(!pa && ch == '\n') {
        pa = true;
        ++paragraph;
      } else if(valid(ch)) {
        // backslash (bs) followed by any character is the character itself:
        if(bs) {
          --cp;
          bs = false;
        }
        break;
      }
      bs = false;
    }

    // parse token
    final int lp = cp;
    spos = cp;
    for(; cp < txtl; cp += cl(txt, cp)) {
      int ch = cp(txt, cp);
      // parse wildcards
      if(wc && !bs) {
        bs = ch == '\\';
        if(bs) continue;
        if(ch == '.') {
          ch = cp + 1 < txtl ? txt[cp + 1] : 0;
          if(ch == '?' || ch == '*' || ch == '+') {
            ++cp;
          } else if(ch == '{') {
            while(++cp < txtl && txt[cp] != '}');
            if(cp == txtl) break;
          }
          continue;
        }
      }
      if(!valid(ch)) {
        if(bs) --cp;
        break;
      }
      bs = false;
    }
    epos = cp;
    ++pos;
    return lp < cp;
  }

  /**
   * Checks if more tokens are to be returned; all characters are included.
   * @return result of check
   */
  private boolean moreAll() {
    final byte[] txt = text;
    final int txtl = txt.length;

    // parse whitespaces
    int cp = epos;

    final int lp = cp;
    spos = cp;
    boolean pa = false, sp = false;
    for(; cp < txtl; cp += cl(txt, cp)) {
      final int ch = cp(txt, cp);
      if(ch == '\n') pa = true;
      else if(valid(ch)) break;
      sp = true;
    }
    para = pa;
    spec = sp;
    epos = cp;
    // token delimiters found
    if(lp < cp) return true;

    // parse token
    for(; cp < txtl; cp += cl(txt, cp)) {
      final int ch = cp(txt, cp);
      if(!valid(ch)) break;
    }
    epos = cp;
    ++pos;
    return lp < cp;
  }

  /**
   * Returns the current token.
   * @return current token
   */
  private byte[] token() {
    final int sp = spos, l = epos - sp;
    final byte[] tmp = new byte[l];
    System.arraycopy(text, sp, tmp, 0, l);
    return tmp;
  }

  @Override
  int pos(final int w, final FTUnit u) {
    if(u == FTUnit.WORDS) return w;

    // if necessary, calculate sentences and paragraphs
    final IntList sPos = sentPos, pPos = paraPos;
    final IntList il = u == FTUnit.SENTENCES ? sPos : pPos;
    if(sPos.isEmpty()) {
      init();
      while(more()) {
        sPos.add(sentence);
        pPos.add(paragraph);
      }
    }
    return il.get(w);
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
      for(int i = 0; i < tl; ++i) t[i] = (byte) uc(t[i]);
      return t;
    }
    final TokenBuilder tb = new TokenBuilder();
    for(int i = 0; i < tl; i += cl(t, i)) tb.add(uc(cp(t, i)));
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
    for(int i = 0; i < tl; i += cl(t, i)) tb.add(lc(cp(t, i)));
    return tb.finish();
  }

  @Override
  int[][] info() {
    init();
    final IntList[] il = {
      new IntList(), new IntList(), new IntList(), new IntList(), new IntList()
    };
    int lass = 0, lasp = 0, sl = 0, pl = 0;
    while(more()) {
      final byte[] n = token();
      final int l = n.length;
      il[0].add(l);
      for(final byte b : n) il[3].add(b);

      if(sentence != lass) {
        if(sl > 0) {
          il[1].add(sl);
          il[4].add(punct);
        }
        lass = sentence;
        sl = 0;
      }
      if(paragraph != lasp) {
        if(pl > 0) il[2].add(pl);
        lasp = paragraph;
        pl = 0;
      }
      sl += l;
      pl += l;
    }

    if(sentence != lass && sl > 0) {
      il[1].add(sl);
      il[4].add(punct);
    }
    if(pl > 0) il[2].add(pl);

    // last sentence not finished with a punctuation mark
    il[1].add(sl + 1);

    return new int[][] {
        il[0].finish(), il[1].finish(), il[2].finish(), il[3].finish(), il[4].finish()
    };
  }

  @Override
  protected byte prec() {
    return 10;
  }

  @Override
  boolean paragraph() {
    return para;
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + string(text) + ']';
  }
}
