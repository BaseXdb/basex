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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class WesternTokenizer extends Tokenizer {
  /** Supported languages. */
  private static final HashSet<Language> SUPPORTED = new HashSet<>();

  static {
    final String[] nonw = { "ja", "ko", "th", "zh" };
    for(final Language l : Language.ALL.values()) {
      if(!Strings.eq(l.code(), nonw)) SUPPORTED.add(l);
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
    if(original) return t;

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

    // parse whitespace
    int t = epos;
    boolean bs = false, pa = false, sn = false;
    for(; t < txtl; t += cl(txt, t)) {
      final int cp = cp(txt, t);
      if(wc && !bs) {
        bs = cp == '\\';
        if(bs) continue;
        if(cp == '.') break;
      }
      if(!sn && (cp == '.' || cp == '!' || cp == '?')) {
        sn = true;
        ++sentence;
        punct = cp;
      } else if(!pa && cp == '\n') {
        pa = true;
        ++paragraph;
      } else if(lod(cp)) {
        // backslash (bs) followed by any character is the character itself:
        if(bs) {
          --t;
          bs = false;
        }
        break;
      }
      bs = false;
    }

    // parse token
    final int lp = t;
    spos = t;
    for(; t < txtl; t += cl(txt, t)) {
      int cp = cp(txt, t);
      // parse wildcards
      if(wc && !bs) {
        bs = cp == '\\';
        if(bs) continue;
        if(cp == '.') {
          cp = t + 1 < txtl ? txt[t + 1] : 0;
          if(cp == '?' || cp == '*' || cp == '+') {
            ++t;
          } else if(cp == '{') {
            while(++t < txtl && txt[t] != '}');
            if(t == txtl) break;
          }
          continue;
        }
      }
      if(!lod(cp)) {
        if(bs) --t;
        break;
      }
      bs = false;
    }
    epos = t;
    ++pos;
    return lp < t;
  }

  /**
   * Checks if more tokens are to be returned; all characters are included.
   * @return result of check
   */
  private boolean moreAll() {
    final byte[] txt = text;
    final int txtl = txt.length;

    // parse whitespace
    int t = epos;

    final int lp = t;
    spos = t;
    boolean pa = false, sp = false;
    for(; t < txtl; t += cl(txt, t)) {
      final int cp = cp(txt, t);
      if(cp == '\n') pa = true;
      else if(lod(cp)) break;
      sp = true;
    }
    para = pa;
    spec = sp;
    epos = t;
    // token delimiters found
    if(lp < t) return true;

    // parse token
    for(; t < txtl; t += cl(txt, t)) {
      final int cp = cp(txt, t);
      if(!lod(cp)) break;
    }
    epos = t;
    ++pos;
    return lp < t;
  }

  /**
   * Returns the current token.
   * @return current token
   */
  private byte[] token() {
    return Arrays.copyOfRange(text, spos, epos);
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
   * @param token token to be converted
   * @param ascii ascii flag
   * @return the converted token
   */
  static byte[] upper(final byte[] token, final boolean ascii) {
    final int tl = token.length;
    if(ascii) {
      for(int i = 0; i < tl; ++i) token[i] = (byte) uc(token[i]);
      return token;
    }
    final TokenBuilder tb = new TokenBuilder();
    forEachCp(token, cp -> tb.add(uc(cp)));
    return tb.finish();
  }

  /**
   * Converts the specified token to lower case.
   * @param token token to be converted
   * @param ascii ascii flag
   * @return the converted token
   */
  static byte[] lower(final byte[] token, final boolean ascii) {
    final int tl = token.length;
    if(ascii) {
      for(int i = 0; i < tl; ++i) token[i] = (byte) lc(token[i]);
      return token;
    }
    final TokenBuilder tb = new TokenBuilder();
    forEachCp(token, cp -> tb.add(lc(cp)));
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
