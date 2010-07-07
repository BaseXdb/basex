package org.basex.util;

import static org.basex.util.Token.*;
import java.util.Arrays;

/**
 * Simple stemming algorithm. Based on the publication from
 * Porter (1980): An algorithm for suffix stripping.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class Stemming {
  /** Stemming character. */
  private static final byte[] S = token("s");
  /** Stemming character. */
  private static final byte[] SSES = token("sses");
  /** Stemming character. */
  private static final byte[] IES = token("ies");
  /** Stemming character. */
  private static final byte[] EED = token("eed");
  /** Stemming character. */
  private static final byte[] ED = token("ed");
  /** Stemming character. */
  private static final byte[] ING = token("ing");
  /** Stemming character. */
  private static final byte[] AT = token("at");
  /** Stemming character. */
  private static final byte[] BL = token("bl");
  /** Stemming character. */
  private static final byte[] IZ = token("iz");
  /** Stemming character. */
  private static final byte[] Y = token("y");
  /** Stemming character. */
  private static final byte[] TION = token("tion");
  /** Stemming character. */
  private static final byte[] SION = token("sion");
  /** Stemming character. */
  private static final byte[] ION = token("ion");
  /** Stemming character. */
  private static final byte[] E = token("e");
  /** Stemming character. */
  private static final byte[] LL = token("ll");
  /** Stemming character. */
  private static final byte[] L = token("l");

  /** Step 2. */
  private static final byte[][][] ST2 = {
    tokens("ational", "ate"), tokens("tional", "tion"), tokens("enci", "ence"),
    tokens("anci", "ance"), tokens("izer", "ize"), tokens("izer", "ize"),
    tokens("abli", "able"), tokens("alli", "al"), tokens("entli", "ent"),
    tokens("eli", "e"), tokens("ousli", "ous"), tokens("ization", "ize"),
    tokens("ization", "ize"), tokens("ation", "ate"), tokens("ator", "ate"),
    tokens("alism", "al"), tokens("iveness", "ive"), tokens("fulness", "ful"),
    tokens("ousness", "ous"), tokens("aliti", "al"), tokens("iviti", "ive"),
    tokens("biliti", "ble")};
  /** Step 3. */
  private static final byte[][][] ST3 = {
    tokens("icate", "ic"), tokens("ative", ""), tokens("alize", "al"),
    tokens("alize", "al"), tokens("iciti", "ic"), tokens("ical", "ic"),
    tokens("ful", ""), tokens("ness", "") };
  /** Step 4. */
  private static final byte[][] ST4 =  tokens("al", "ance", "ence", "er", "ic",
    "able", "ible", "ant", "ement", "ment", "ent", "sion", "tion", "ou", "ism",
    "ate", "iti", "ous", "ive", "ize", "ize");

  /** Token to be stemmed. */
  private byte[] tok;
  /** Token length. */
  private int te;
  /** Stemming length. */
  private int tt;

  /**
   * Stems the specified word.
   * @param str word to be stemmed
   * @return result
   */
  byte[] stem(final byte[] str) {
    te = str.length;
    tok = str;
    return !s() ? str : Arrays.copyOf(str, te);
  }

  /**
   * Stems the current word.
   * @return true if word was stemmed
   */
  private boolean s() {
    if(te < 3) return false;

    // step 1
    if(e(S)) {
      if(e(SSES) || e(IES)) te -= 2;
      else if(l(te - 2) != 's') te--;
    }

    if(e(EED)) {
      if(m() > 0) te--;
    } else if((e(ED) || e(ING)) && v()) {
      te = tt;

      if(e(AT) || e(BL) || e(IZ)) {
        tt = te;
        a((byte) 'e');
      } else if(te > 1) {
        final int c = l(te - 1);
        if(c == l(te - 2) && c != 'l' && c != 's' && c != 'z') {
          te--;
        } else if(m() == 1) {
          if(c(te)) a((byte) 'e');
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
      te -= 3;
    } else {
      for(final byte[] s : ST4) {
        if(e(s)) {
          if(m() > 1) te = tt;
          break;
        }
      }
    }

    // step 5
    if(e(E)) {
      final int m = m();
      if(m > 1 || m == 1 && !c(te - 1)) te--;
    }
    if(e(LL) && e(L) && m() > 1) te--;

    return te != tok.length;
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
   * Suffix test.
   * @param s suffix
   * @return result of check
   */
  private boolean e(final byte[] s) {
    final int sl = s.length;
    final int l = te - sl;
    if(l < 0) return false;
    for(int i = 0; i < sl; i++) if(l(l + i) != s[i]) return false;
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
        if(v) c++;
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
    for(int i = 0; i < tt; i++) if(v(i)) return true;
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
    return lc(tok[p]);
  }

  /**
   * Adds a character.
   * @param c character
   */
  private void a(final byte c) {
    te = tt;
    tok[te++] = c;
  }

  /**
   * Adds a token.
   * @param t token
   */
  private void a(final byte[] t) {
    te = tt;
    for(final byte c : t) tok[te++] = c;
  }
}
