package org.basex.util;

/**
 * Simple stemming algorithm, derived from
 * Porter [1980], An algorithm for suffix stripping.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class Stemming {
  /** Step 2. */
  private static final String[][] ST2 = { { "ational", "ate" },
    { "tional", "tion" }, { "enci", "ence" }, { "anci", "ance" },
    { "izer", "ize" }, { "izer", "ize" }, { "abli", "able" }, { "alli", "al" },
    { "entli", "ent" }, { "eli", "e" }, { "ousli", "ous" },
    { "ization", "ize" }, { "ization", "ize" }, { "ation", "ate" },
    { "ator", "ate" }, { "alism", "al" }, { "iveness", "ive" },
    { "fulness", "ful" }, { "ousness", "ous" }, { "aliti", "al" },
    { "iviti", "ive" }, { "biliti", "ble" } };
  /** Step 3. */
  private static final String[][] ST3 = { { "icate", "ic" },
    { "ative", "" }, { "alize", "al" }, { "alize", "al" }, { "iciti", "ic" },
    { "ical", "ic" }, { "ful", "" }, { "ness", "" } };
  /** Step 4. */
  private static final String[] ST4 = {
    "al", "ance", "ence", "er", "ic", "able", "ible", "ant", "ement", "ment",
    "ent", "sion", "tion", "ou", "ism", "ate", "iti", "ous", "ive", "ize", "ize"
  };

  /** String to be stemmed. */
  private byte[] tok;
  /** Beginning of string. */
  private int ts;
  /** String length. */
  private int te;
  /** String stemming length. */
  private int tt;

  /**
   * Stems the specified word.
   * @param str word to be stemmed
   * @return result
   */
  public byte[] word(final byte[] str) {
    tok = str;
    ts = 0;
    te = str.length;
    word();
    return f();
  }

  /**
   * Stems the current word.
   */
  public void word() {
    if(te - ts < 3) return;

    // step 1
    if(e("s")) {
      if(e("sses") || e("ies")) te -= 2;
      else if(c(te - 2) != 's') te--;
    }

    if(e("eed")) {
      if(m() > 0) te--;
    } else if((e("ed") || e("ing")) && v()) {
      te = tt;

      if(e("at") || e("bl") || e("iz")) {
        tt = te;
        a((byte) 'e');
      } else if(te > 1) {
        final int c = c(te - 1);
        if(c == c(te - 2) && c != 'l' && c != 's' && c != 'z') {
          te--;
        } else if(m() == 1) {
          if(cvc(te)) a((byte) 'e');
        }
      }
    }
    if(e("y") && v()) a((byte) 'i');

    // step 2
    for(final String[] s : ST2) {
      if(e(s[0])) {
        if(m() > 0) a(s[1]);
        break;
      }
    }

    // step 3
    for(final String[] s : ST3) {
      if(e(s[0])) {
        if(m() > 0) a(s[1]);
        break;
      }
    }

    // step 4
    if((e("tion") || e("sion")) && e("ion") && m() > 1) {
      te -= 3;
    } else {
      for(final String s : ST4) {
        if(e(s)) {
          if(m() > 1) te = tt;
          break;
        }
      }
    }

    // step 5
    if(e("e")) {
      final int m = m();
      if(m > 1 || m == 1 && !cvc(te - 1)) te--;
    }
    if(e("ll") && e("l") && m() > 1) te--;
  }

  /**
   * Finishes the token.
   * @return result of check
   */
  private byte[] f() {
    final TokenBuilder tb = new TokenBuilder();
    for(int i = ts; i < te; i++) tb.add(tok[i]);
    return tb.finish();
  }

  /**
   * Checks for the cvc pattern.
   * @param l position
   * @return result of check
   */
  private boolean cvc(final int l) {
    if(l < 3) return false;
    final int c = c(l - 1);
    return c != 'w' && c != 'x' && c != 'y' &&
      !v(l - 1) && v(l - 2) && !v(l - 3);
  }

  /**
   * Suffix test.
   * @param s suffix
   * @return result of check
   */
  private boolean e(final String s) {
    final int sl = s.length();
    final int l = te - sl;
    if(l < ts) return false;
    for(int i = 0; i < sl; i++) {
      if(c(l + i) != s.charAt(i)) return false;
    }
    tt = l;
    return true;
  }

  /**
   * Returns word measure.
   * @return measure
   */
  private int m() {
    int c = 0;
    int i = ts - 1;
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
    for(int i = ts; i < tt; i++) if(v(i)) return true;
    return false;
  }

  /**
   * Vowel test.
   * @param p position
   * @return result of check
   */
  private boolean v(final int p) {
    final int c = c(p);
    return c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u' ||
      c == 'y' && p != ts && !v(p - 1);
  }

  /**
   * Returns the character at the specified position.
   * @param p position
   * @return result of check
   */
  private int c(final int p) {
    return Character.toLowerCase(tok[p]);
  }

  /**
   * Adds a character.
   * @param ch character
   */
  private void a(final byte ch) {
    te = tt;
    tok[te++] = ch;
  }

  /**
   * Adds a string.
   * @param s string
   */
  private void a(final String s) {
    te = tt;
    for(int i = 0; i < s.length(); i++) tok[te++] = (byte) s.charAt(i);
  }
}
