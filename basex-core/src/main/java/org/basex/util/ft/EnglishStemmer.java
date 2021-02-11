package org.basex.util.ft;

import static org.basex.util.Token.*;

import java.util.*;

/**
 * English stemming algorithm, based on the publication from
 * Porter (1980), "An algorithm for suffix stripping".
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class EnglishStemmer extends InternalStemmer {
  /** Stemming character. */
  private static final byte[] AT = token("at");
  /** Stemming character. */
  private static final byte[] BL = token("bl");
  /** Stemming character. */
  private static final byte[] ED = token("ed");
  /** Stemming character. */
  private static final byte[] EED = token("eed");
  /** Stemming character. */
  private static final byte[] IES = token("ies");
  /** Stemming character. */
  private static final byte[] ING = token("ing");
  /** Stemming character. */
  private static final byte[] ION = token("ion");
  /** Stemming character. */
  private static final byte[] IZ = token("iz");
  /** Stemming character. */
  private static final byte[] LL = token("ll");
  /** Stemming character. */
  private static final byte[] SION = token("sion");
  /** Stemming character. */
  private static final byte[] SSES = token("sses");
  /** Stemming character. */
  private static final byte[] TION = token("tion");
  /** Stemming character. */
  private static final byte S = 's';
  /** Stemming character. */
  private static final byte Y = 'y';
  /** Stemming character. */
  private static final byte E = 'e';
  /** Stemming character. */
  private static final byte L = 'l';

  /** Step 2. */
  private static final byte[][][] ST2 = {
    tokens("abli", "able"), tokens("alism", "al"), tokens("aliti", "al"),
    tokens("alli", "al"), tokens("anci", "ance"), tokens("ation", "ate"),
    tokens("ational", "ate"), tokens("ator", "ate"), tokens("biliti", "ble"),
    tokens("eli", "e"), tokens("enci", "ence"), tokens("entli", "ent"),
    tokens("fulness", "ful"), tokens("iveness", "ive"), tokens("iviti", "ive"),
    tokens("ization", "ize"), tokens("ization", "ize"), tokens("izer", "ize"),
    tokens("izer", "ize"), tokens("ousli", "ous"), tokens("ousness", "ous"),
    tokens("tional", "tion"),
  };
  /** Step 3. */
  private static final byte[][][] ST3 = {
    tokens("alize", "al"), tokens("alize", "al"), tokens("ative", ""), tokens("ful", ""),
    tokens("ical", "ic"), tokens("icate", "ic"), tokens("iciti", "ic"), tokens("ness", "")
  };
  /** Step 4. */
  private static final byte[][] ST4 = tokens(
    "able", "al", "ance", "ant", "ate", "ement", "ence", "ent", "er", "ible", "ic", "ism",
    "iti", "ive", "ize", "ment", "ou", "ous", "sion", "tion"
  );

  /** Token to be stemmed. */
  private byte[] token;
  /** Token length. */
  private int tl;
  /** Stemming length. */
  private int tt;

  /**
   * Constructor.
   * @param fti full-text iterator
   */
  EnglishStemmer(final FTIterator fti) {
    super(fti);
  }

  @Override
  Stemmer get(final Language lang, final FTIterator fti) {
    return new EnglishStemmer(fti);
  }

  @Override
  Collection<Language> languages() {
    return collection("en");
  }

  @Override
  protected byte[] stem(final byte[] str) {
    tl = str.length;
    token = str;
    return s() ? Arrays.copyOf(str, tl) : str;
  }

  /**
   * Stems the current word.
   * @return true if word was stemmed
   */
  private boolean s() {
    if(tl < 3) return false;

    // step 1
    if(e(S)) {
      if(e(SSES) || e(IES)) tl -= 2;
      else if(l(tl - 2) != 's') --tl;
    }

    if(e(EED)) {
      if(m() > 0) --tl;
    } else if((e(ED) || e(ING)) && v()) {
      tl = tt;

      if(e(AT) || e(BL) || e(IZ)) {
        tt = tl;
        a((byte) 'e');
      } else if(tl > 1) {
        final int c = l(tl - 1);
        if(c == l(tl - 2) && c != 'l' && c != 's' && c != 'z') {
          --tl;
        } else if(m() == 1 && c(tl)) {
          a((byte) 'e');
        }
      }
    }
    if(e(Y) && v()) a((byte) 'i');

    // step 2
    for(final byte[][] s : ST2) {
      if(e(s[0])) {
        if(m() > 0) a(s[1]);
        break;
      }
    }

    // step 3
    for(final byte[][] s : ST3) {
      if(e(s[0])) {
        if(m() > 0) a(s[1]);
        break;
      }
    }

    // step 4
    if((e(TION) || e(SION)) && e(ION) && m() > 1) {
      tl -= 3;
    } else {
      for(final byte[] s : ST4) {
        if(e(s)) {
          if(m() > 1) tl = tt;
          break;
        }
      }
    }

    // step 5
    if(e(E)) {
      final int m = m();
      if(m > 1 || m == 1 && !c(tl - 1)) --tl;
    }
    if(e(LL) && e(L) && m() > 1) --tl;

    return tl != token.length;
  }

  /**
   * Checks for the cvc pattern.
   * @param l position
   * @return result of check
   */
  private boolean c(final int l) {
    if(l < 3) return false;
    final int c = l(l - 1);
    return c != 'w' && c != 'x' && c != 'y' &&
        !v(l - 1) && v(l - 2) && !v(l - 3);
  }

  /**
   * Suffix test for a token.
   * @param s suffix
   * @return result of check
   */
  private boolean e(final byte[] s) {
    final int sl = s.length;
    final int l = tl - sl;
    if(l < 0) return false;
    for(int i = 0; i < sl; ++i)
      if(l(l + i) != s[i]) return false;
    tt = l;
    return true;
  }

  /**
   * Suffix test for a single character.
   * @param s suffix
   * @return result of check
   */
  private boolean e(final byte s) {
    final int l = tl - 1;
    if(l < 0 || l(l) != s) return false;
    tt = l;
    return true;
  }

  /**
   * Returns word measure.
   * @return measure
   */
  private int m() {
    int c = 0;
    int i = -1;
    boolean v = false;
    while(++i < tt) {
      if(v ^ v(i)) {
        if(v) ++c;
        v ^= true;
      }
    }
    return c;
  }

  /**
   * Vowel test.
   * @return result of check
   */
  private boolean v() {
    for(int i = 0; i < tt; ++i)
      if(v(i)) return true;
    return false;
  }

  /**
   * Vowel test.
   * @param p position
   * @return result of check
   */
  private boolean v(final int p) {
    final int c = l(p);
    return c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u' ||
        c == 'y' && p != 0 && !v(p - 1);
  }

  /**
   * Returns the lower character at the specified position.
   * @param p position
   * @return result of check
   */
  private int l(final int p) {
    return lc(token[p]);
  }

  /**
   * Adds a character.
   * @param c character
   */
  private void a(final byte c) {
    tl = tt;
    token[tl++] = c;
  }

  /**
   * Adds a token.
   * @param t token
   */
  private void a(final byte[] t) {
    tl = tt;
    for(final byte c : t) token[tl++] = c;
  }
}
