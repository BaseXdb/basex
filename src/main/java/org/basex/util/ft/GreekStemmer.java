package org.basex.util.ft;

import java.util.*;

import org.basex.util.*;

/**
 * German stemming algorithm, derived from the Apache Lucene project and the report
 * "Development of a Stemmer for the Greek Language" by Georgios Ntais.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class GreekStemmer extends InternalStemmer {
  /**
   * Constructor.
   * @param fti full-text iterator
   */
  GreekStemmer(final FTIterator fti) {
    super(fti);
  }

  @Override
  GreekStemmer get(final Language l, final FTIterator fti) {
    return new GreekStemmer(fti);
  }

  @Override
  Collection<Language> languages() {
    return collection("el");
  }

  @Override
  protected byte[] stem(final byte[] word) {
    int ln = 0;
    final int wl = word.length;
    final char[] s = new char[wl];
    for(int i = 0; i < wl; i += Token.cl(word, i)) {
      s[ln++] = (char) Token.cp(word, i);
    }
    if(ln < 4) return word;

    final int olen = ln;
    // "short rules": if it hits one of these, it skips the "long list"
    int l = rule0(s, ln);
    l = rule1(s, l);
    l = rule2(s, l);
    l = rule3(s, l);
    l = rule4(s, l);
    l = rule5(s, l);
    l = rule6(s, l);
    l = rule7(s, l);
    l = rule8(s, l);
    l = rule9(s, l);
    l = rule10(s, l);
    l = rule11(s, l);
    l = rule12(s, l);
    l = rule13(s, l);
    l = rule14(s, l);
    l = rule15(s, l);
    l = rule16(s, l);
    l = rule17(s, l);
    l = rule18(s, l);
    l = rule19(s, l);
    l = rule20(s, l);
    if(l == olen) l = rule21(s, l);
    // "long list"
    l = rule22(s, l);

    final TokenBuilder tb = new TokenBuilder(l << 1);
    for(int i = 0; i < l; i++) tb.add(s[i]);
    return tb.finish();
  }

  /**
   * Applies rule 0.
   * @param s characters
   * @param l length
   * @return new length
   */
  private int rule0(final char[] s, final int l) {
    if(l > 9 && (
      e(s, l, "\u03ba\u03b1\u03b8\u03b5\u03c3\u03c4\u03c9\u03c4\u03bf\u03c3") ||
      e(s, l, "\u03ba\u03b1\u03b8\u03b5\u03c3\u03c4\u03c9\u03c4\u03c9\u03bd")))
      return l - 4;
    if(l > 8 && (
      e(s, l, "\u03b3\u03b5\u03b3\u03bf\u03bd\u03bf\u03c4\u03bf\u03c3") ||
      e(s, l, "\u03b3\u03b5\u03b3\u03bf\u03bd\u03bf\u03c4\u03c9\u03bd")))
      return l - 4;
    if(l > 8 &&
      e(s, l, "\u03ba\u03b1\u03b8\u03b5\u03c3\u03c4\u03c9\u03c4\u03b1"))
      return l - 3;
    if(l > 7 && (
      e(s, l, "\u03c4\u03b1\u03c4\u03bf\u03b3\u03b9\u03bf\u03c5") ||
      e(s, l, "\u03c4\u03b1\u03c4\u03bf\u03b3\u03b9\u03c9\u03bd")))
      return l - 4;
    if(l > 7 &&
      e(s, l, "\u03b3\u03b5\u03b3\u03bf\u03bd\u03bf\u03c4\u03b1"))
      return l - 3;
    if(l > 7 &&
      e(s, l, "\u03ba\u03b1\u03b8\u03b5\u03c3\u03c4\u03c9\u03c3"))
      return l - 2;
    if(l > 6 &&
      e(s, l, "\u03c3\u03ba\u03b1\u03b3\u03b9\u03bf\u03c5") ||
      e(s, l, "\u03c3\u03ba\u03b1\u03b3\u03b9\u03c9\u03bd") ||
      e(s, l, "\u03bf\u03bb\u03bf\u03b3\u03b9\u03bf\u03c5") ||
      e(s, l, "\u03bf\u03bb\u03bf\u03b3\u03b9\u03c9\u03bd") ||
      e(s, l, "\u03ba\u03c1\u03b5\u03b1\u03c4\u03bf\u03c3") ||
      e(s, l, "\u03ba\u03c1\u03b5\u03b1\u03c4\u03c9\u03bd") ||
      e(s, l, "\u03c0\u03b5\u03c1\u03b1\u03c4\u03bf\u03c3") ||
      e(s, l, "\u03c0\u03b5\u03c1\u03b1\u03c4\u03c9\u03bd") ||
      e(s, l, "\u03c4\u03b5\u03c1\u03b1\u03c4\u03bf\u03c3") ||
      e(s, l, "\u03c4\u03b5\u03c1\u03b1\u03c4\u03c9\u03bd"))
      return l - 4;
    if(l > 6 &&
      e(s, l, "\u03c4\u03b1\u03c4\u03bf\u03b3\u03b9\u03b1"))
      return l - 3;
    if(l > 6 &&
      e(s, l, "\u03b3\u03b5\u03b3\u03bf\u03bd\u03bf\u03c3"))
      return l - 2;
    if(l > 5 && (
      e(s, l, "\u03c6\u03b1\u03b3\u03b9\u03bf\u03c5") ||
      e(s, l, "\u03c6\u03b1\u03b3\u03b9\u03c9\u03bd") ||
      e(s, l, "\u03c3\u03bf\u03b3\u03b9\u03bf\u03c5") ||
      e(s, l, "\u03c3\u03bf\u03b3\u03b9\u03c9\u03bd")))
      return l - 4;
    if(l > 5 && (
      e(s, l, "\u03c3\u03ba\u03b1\u03b3\u03b9\u03b1") ||
      e(s, l, "\u03bf\u03bb\u03bf\u03b3\u03b9\u03b1") ||
      e(s, l, "\u03ba\u03c1\u03b5\u03b1\u03c4\u03b1") ||
      e(s, l, "\u03c0\u03b5\u03c1\u03b1\u03c4\u03b1") ||
      e(s, l, "\u03c4\u03b5\u03c1\u03b1\u03c4\u03b1")))
      return l - 3;
    if(l > 4 && (
      e(s, l, "\u03c6\u03b1\u03b3\u03b9\u03b1") ||
      e(s, l, "\u03c3\u03bf\u03b3\u03b9\u03b1") ||
      e(s, l, "\u03c6\u03c9\u03c4\u03bf\u03c3") ||
      e(s, l, "\u03c6\u03c9\u03c4\u03c9\u03bd")))
      return l - 3;
    if(l > 4 && (
      e(s, l, "\u03ba\u03c1\u03b5\u03b1\u03c3") ||
      e(s, l, "\u03c0\u03b5\u03c1\u03b1\u03c3") ||
      e(s, l, "\u03c4\u03b5\u03c1\u03b1\u03c3")))
      return l - 2;
    if(l > 3 &&
      e(s, l, "\u03c6\u03c9\u03c4\u03b1"))
      return l - 2;
    if(l > 2 &&
      e(s, l, "\u03c6\u03c9\u03c3"))
      return l - 1;
    return l;
  }

  /**
   * Applies rule 1.
   * @param s characters
   * @param l length
   * @return new length
   */
  private int rule1(final char[] s, final int l) {
    int len = l;
    if(len > 4 && (
        e(s, len, "\u03b1\u03b4\u03b5\u03c3") ||
        e(s, len, "\u03b1\u03b4\u03c9\u03bd"))) {
      len -= 4;
      if(!(e(s, len, "\u03bf\u03ba") ||
        e(s, len, "\u03bc\u03b1\u03bc") ||
        e(s, len, "\u03bc\u03b1\u03bd") ||
        e(s, len, "\u03bc\u03c0\u03b1\u03bc\u03c0") ||
        e(s, len, "\u03c0\u03b1\u03c4\u03b5\u03c1") ||
        e(s, len, "\u03b3\u03b9\u03b1\u03b3\u03b9") ||
        e(s, len, "\u03bd\u03c4\u03b1\u03bd\u03c4") ||
        e(s, len, "\u03ba\u03c5\u03c1") ||
        e(s, len, "\u03b8\u03b5\u03b9") ||
        e(s, len, "\u03c0\u03b5\u03b8\u03b5\u03c1")))
      len += 2; // add back -\u03b1\u03b4
    }
    return len;
  }

  /**
   * Applies rule 2.
   * @param s characters
   * @param l length
   * @return new length
   */
  private int rule2(final char[] s, final int l) {
    int len = l;
    if(len > 4 && (
        e(s, len, "\u03b5\u03b4\u03b5\u03c3") ||
        e(s, len, "\u03b5\u03b4\u03c9\u03bd"))) {
      len -= 4;
      if(e(s, len, "\u03bf\u03c0") ||
        e(s, len, "\u03b9\u03c0") ||
        e(s, len, "\u03b5\u03bc\u03c0") ||
        e(s, len, "\u03c5\u03c0") ||
        e(s, len, "\u03b3\u03b7\u03c0") ||
        e(s, len, "\u03b4\u03b1\u03c0") ||
        e(s, len, "\u03ba\u03c1\u03b1\u03c3\u03c0") ||
        e(s, len, "\u03bc\u03b9\u03bb"))
      len += 2; // add back
    }
    return len;
  }

  /**
   * Applies rule 3.
   * @param s characters
   * @param l length
   * @return new length
   */
  private int rule3(final char[] s, final int l) {
    int len = l;
    if(len > 5 && (
        e(s, len, "\u03bf\u03c5\u03b4\u03b5\u03c3") ||
        e(s, len, "\u03bf\u03c5\u03b4\u03c9\u03bd"))) {
      len -= 5;
      if(e(s, len, "\u03b1\u03c1\u03ba") ||
         e(s, len, "\u03ba\u03b1\u03bb\u03b9\u03b1\u03ba") ||
         e(s, len, "\u03c0\u03b5\u03c4\u03b1\u03bb") ||
         e(s, len, "\u03bb\u03b9\u03c7") ||
         e(s, len, "\u03c0\u03bb\u03b5\u03be") ||
         e(s, len, "\u03c3\u03ba") ||
         e(s, len, "\u03c3") ||
         e(s, len, "\u03c6\u03bb") ||
         e(s, len, "\u03c6\u03c1") ||
         e(s, len, "\u03b2\u03b5\u03bb") ||
         e(s, len, "\u03bb\u03bf\u03c5\u03bb") ||
         e(s, len, "\u03c7\u03bd") || e(s, len, "\u03c3\u03c0") ||
         e(s, len, "\u03c4\u03c1\u03b1\u03b3") ||
         e(s, len, "\u03c6\u03b5"))
        len += 3; // add back
    }
    return len;
  }

  /** String arrays. */
  private static final String[] EXC4 = {
    "\u03b8", "\u03b4", "\u03b5\u03bb", "\u03b3\u03b1\u03bb", "\u03bd", "\u03c0",
    "\u03b9\u03b4", "\u03c0\u03b1\u03c1"
  };

  /**
   * Applies rule 4.
   * @param s characters
   * @param l length
   * @return new length
   */
  private int rule4(final char[] s, final int l) {
    int len = l;
    if(len > 3 && (
        e(s, len, "\u03b5\u03c9\u03c3") ||
        e(s, len, "\u03b5\u03c9\u03bd"))) {
      len -= 3;
      if(c(EXC4, s, len)) len++; // add back -\u03b5
    }
    return len;
  }

  /**
   * Applies rule 5.
   * @param s characters
   * @param l length
   * @return new length
   */
  private int rule5(final char[] s, final int l) {
    int len = l;
    if(len > 2 && e(s, len, "\u03b9\u03b1")) {
      len -= 2;
      if(ev(s, len)) len++; // add back -\u03b9
    } else if(len > 3 && (
        e(s, len, "\u03b9\u03bf\u03c5") ||
        e(s, len, "\u03b9\u03c9\u03bd"))) {
      len -= 3;
      if(ev(s, len)) len++; // add back -\u03b9
    }
    return len;
  }

  /** String arrays. */
  private static final String[] EXC6 = {
    "\u03b1\u03bb", "\u03b1\u03b4", "\u03b5\u03bd\u03b4", "\u03b1\u03bc\u03b1\u03bd",
    "\u03b1\u03bc\u03bc\u03bf\u03c7\u03b1\u03bb", "\u03b7\u03b8",
    "\u03b1\u03bd\u03b7\u03b8", "\u03b1\u03bd\u03c4\u03b9\u03b4", "\u03c6\u03c5\u03c3",
    "\u03b2\u03c1\u03c9\u03bc", "\u03b3\u03b5\u03c1", "\u03b5\u03be\u03c9\u03b4",
    "\u03ba\u03b1\u03bb\u03c0", "\u03ba\u03b1\u03bb\u03bb\u03b9\u03bd",
    "\u03ba\u03b1\u03c4\u03b1\u03b4", "\u03bc\u03bf\u03c5\u03bb",
    "\u03bc\u03c0\u03b1\u03bd", "\u03bc\u03c0\u03b1\u03b3\u03b9\u03b1\u03c4",
    "\u03bc\u03c0\u03bf\u03bb", "\u03bc\u03c0\u03bf\u03c3", "\u03bd\u03b9\u03c4",
    "\u03be\u03b9\u03ba", "\u03c3\u03c5\u03bd\u03bf\u03bc\u03b7\u03bb",
    "\u03c0\u03b5\u03c4\u03c3", "\u03c0\u03b9\u03c4\u03c3",
    "\u03c0\u03b9\u03ba\u03b1\u03bd\u03c4", "\u03c0\u03bb\u03b9\u03b1\u03c4\u03c3",
    "\u03c0\u03bf\u03c3\u03c4\u03b5\u03bb\u03bd", "\u03c0\u03c1\u03c9\u03c4\u03bf\u03b4",
    "\u03c3\u03b5\u03c1\u03c4", "\u03c3\u03c5\u03bd\u03b1\u03b4",
    "\u03c4\u03c3\u03b1\u03bc", "\u03c5\u03c0\u03bf\u03b4",
    "\u03c6\u03b9\u03bb\u03bf\u03bd", "\u03c6\u03c5\u03bb\u03bf\u03b4",
    "\u03c7\u03b1\u03c3"
  };

  /**
   * Applies rule 6.
   * @param s characters
   * @param l length
   * @return new length
   */
  private int rule6(final char[] s, final int l) {
    int len = l;
    boolean rem = false;
    if(len > 3 && (
        e(s, len, "\u03b9\u03ba\u03b1") ||
        e(s, len, "\u03b9\u03ba\u03bf"))) {
      len -= 3;
      rem = true;
    } else if(len > 4 && (
        e(s, len, "\u03b9\u03ba\u03bf\u03c5") ||
        e(s, len, "\u03b9\u03ba\u03c9\u03bd"))) {
      len -= 4;
      rem = true;
    }

    if(rem) {
      if(ev(s, len) || c(EXC6, s, len)) len += 2; // add back -\u03b9\u03ba
    }
    return len;
  }

  /** String arrays. */
  private static final String[] EXC7 = {
    "\u03b1\u03bd\u03b1\u03c0", "\u03b1\u03c0\u03bf\u03b8", "\u03b1\u03c0\u03bf\u03ba",
    "\u03b1\u03c0\u03bf\u03c3\u03c4", "\u03b2\u03bf\u03c5\u03b2", "\u03be\u03b5\u03b8",
    "\u03bf\u03c5\u03bb", "\u03c0\u03b5\u03b8", "\u03c0\u03b9\u03ba\u03c1",
    "\u03c0\u03bf\u03c4", "\u03c3\u03b9\u03c7", "\u03c7"
  };

  /**
   * Applies rule 7.
   * @param s characters
   * @param l length
   * @return new length
   */
  private int rule7(final char[] s, final int l) {
    int len = l;
    if(len == 5 && e(s, len, "\u03b1\u03b3\u03b1\u03bc\u03b5")) return len - 1;

    if(len > 7 && e(s, len, "\u03b7\u03b8\u03b7\u03ba\u03b1\u03bc\u03b5"))
      len -= 7;
    else if(len > 6 && e(s, len, "\u03bf\u03c5\u03c3\u03b1\u03bc\u03b5"))
      len -= 6;
    else if(len > 5 && (
        e(s, len, "\u03b1\u03b3\u03b1\u03bc\u03b5") ||
        e(s, len, "\u03b7\u03c3\u03b1\u03bc\u03b5") ||
        e(s, len, "\u03b7\u03ba\u03b1\u03bc\u03b5"))) len -= 5;
    if(len > 3 && e(s, len, "\u03b1\u03bc\u03b5")) {
      len -= 3;
      if(c(EXC7, s, len)) len += 2; // add back -\u03b1\u03bc
    }

    return len;
  }

  /** String arrays. */
  private static final String[] EXC8A = {
    "\u03c4\u03c1", "\u03c4\u03c3"
  };

  /** String arrays. */
  private static final String[] EXC8B = {
    "\u03b2\u03b5\u03c4\u03b5\u03c1", "\u03b2\u03bf\u03c5\u03bb\u03ba",
    "\u03b2\u03c1\u03b1\u03c7\u03bc", "\u03b3",
    "\u03b4\u03c1\u03b1\u03b4\u03bf\u03c5\u03bc", "\u03b8",
    "\u03ba\u03b1\u03bb\u03c0\u03bf\u03c5\u03b6", "\u03ba\u03b1\u03c3\u03c4\u03b5\u03bb",
    "\u03ba\u03bf\u03c1\u03bc\u03bf\u03c1", "\u03bb\u03b1\u03bf\u03c0\u03bb",
    "\u03bc\u03c9\u03b1\u03bc\u03b5\u03b8", "\u03bc",
    "\u03bc\u03bf\u03c5\u03c3\u03bf\u03c5\u03bb\u03bc", "\u03bd", "\u03bf\u03c5\u03bb",
    "\u03c0", "\u03c0\u03b5\u03bb\u03b5\u03ba", "\u03c0\u03bb",
    "\u03c0\u03bf\u03bb\u03b9\u03c3", "\u03c0\u03bf\u03c1\u03c4\u03bf\u03bb",
    "\u03c3\u03b1\u03c1\u03b1\u03ba\u03b1\u03c4\u03c3", "\u03c3\u03bf\u03c5\u03bb\u03c4",
    "\u03c4\u03c3\u03b1\u03c1\u03bb\u03b1\u03c4", "\u03bf\u03c1\u03c6",
    "\u03c4\u03c3\u03b9\u03b3\u03b3", "\u03c4\u03c3\u03bf\u03c0",
    "\u03c6\u03c9\u03c4\u03bf\u03c3\u03c4\u03b5\u03c6", "\u03c7",
    "\u03c8\u03c5\u03c7\u03bf\u03c0\u03bb", "\u03b1\u03b3", "\u03bf\u03c1\u03c6",
    "\u03b3\u03b1\u03bb", "\u03b3\u03b5\u03c1", "\u03b4\u03b5\u03ba",
    "\u03b4\u03b9\u03c0\u03bb", "\u03b1\u03bc\u03b5\u03c1\u03b9\u03ba\u03b1\u03bd",
    "\u03bf\u03c5\u03c1", "\u03c0\u03b9\u03b8", "\u03c0\u03bf\u03c5\u03c1\u03b9\u03c4",
    "\u03c3", "\u03b6\u03c9\u03bd\u03c4", "\u03b9\u03ba", "\u03ba\u03b1\u03c3\u03c4",
    "\u03ba\u03bf\u03c0", "\u03bb\u03b9\u03c7", "\u03bb\u03bf\u03c5\u03b8\u03b7\u03c1",
    "\u03bc\u03b1\u03b9\u03bd\u03c4", "\u03bc\u03b5\u03bb", "\u03c3\u03b9\u03b3",
    "\u03c3\u03c0", "\u03c3\u03c4\u03b5\u03b3", "\u03c4\u03c1\u03b1\u03b3",
    "\u03c4\u03c3\u03b1\u03b3", "\u03c6", "\u03b5\u03c1", "\u03b1\u03b4\u03b1\u03c0",
    "\u03b1\u03b8\u03b9\u03b3\u03b3", "\u03b1\u03bc\u03b7\u03c7",
    "\u03b1\u03bd\u03b9\u03ba", "\u03b1\u03bd\u03bf\u03c1\u03b3",
    "\u03b1\u03c0\u03b7\u03b3", "\u03b1\u03c0\u03b9\u03b8",
    "\u03b1\u03c4\u03c3\u03b9\u03b3\u03b3", "\u03b2\u03b1\u03c3",
    "\u03b2\u03b1\u03c3\u03ba", "\u03b2\u03b1\u03b8\u03c5\u03b3\u03b1\u03bb",
    "\u03b2\u03b9\u03bf\u03bc\u03b7\u03c7", "\u03b2\u03c1\u03b1\u03c7\u03c5\u03ba",
    "\u03b4\u03b9\u03b1\u03c4", "\u03b4\u03b9\u03b1\u03c6",
    "\u03b5\u03bd\u03bf\u03c1\u03b3", "\u03b8\u03c5\u03c3",
    "\u03ba\u03b1\u03c0\u03bd\u03bf\u03b2\u03b9\u03bf\u03bc\u03b7\u03c7",
    "\u03ba\u03b1\u03c4\u03b1\u03b3\u03b1\u03bb", "\u03ba\u03bb\u03b9\u03b2",
    "\u03ba\u03bf\u03b9\u03bb\u03b1\u03c1\u03c6", "\u03bb\u03b9\u03b2",
    "\u03bc\u03b5\u03b3\u03bb\u03bf\u03b2\u03b9\u03bf\u03bc\u03b7\u03c7",
    "\u03bc\u03b9\u03ba\u03c1\u03bf\u03b2\u03b9\u03bf\u03bc\u03b7\u03c7",
    "\u03bd\u03c4\u03b1\u03b2", "\u03be\u03b7\u03c1\u03bf\u03ba\u03bb\u03b9\u03b2",
    "\u03bf\u03bb\u03b9\u03b3\u03bf\u03b4\u03b1\u03bc",
    "\u03bf\u03bb\u03bf\u03b3\u03b1\u03bb", "\u03c0\u03b5\u03bd\u03c4\u03b1\u03c1\u03c6",
    "\u03c0\u03b5\u03c1\u03b7\u03c6", "\u03c0\u03b5\u03c1\u03b9\u03c4\u03c1",
    "\u03c0\u03bb\u03b1\u03c4", "\u03c0\u03bf\u03bb\u03c5\u03b4\u03b1\u03c0",
    "\u03c0\u03bf\u03bb\u03c5\u03bc\u03b7\u03c7", "\u03c3\u03c4\u03b5\u03c6",
    "\u03c4\u03b1\u03b2", "\u03c4\u03b5\u03c4", "\u03c5\u03c0\u03b5\u03c1\u03b7\u03c6",
    "\u03c5\u03c0\u03bf\u03ba\u03bf\u03c0",
    "\u03c7\u03b1\u03bc\u03b7\u03bb\u03bf\u03b4\u03b1\u03c0",
    "\u03c8\u03b7\u03bb\u03bf\u03c4\u03b1\u03b2"
  };

  /**
   * Applies rule 8.
   * @param s characters
   * @param l length
   * @return new length
   */
  private int rule8(final char[] s, final int l) {
    boolean rem = false;

    int len = l;
    if(len > 8 && e(s, len, "\u03b9\u03bf\u03c5\u03bd\u03c4\u03b1\u03bd\u03b5")) {
      len -= 8;
      rem = true;
    } else if(len > 7 &&
        e(s, len, "\u03b9\u03bf\u03bd\u03c4\u03b1\u03bd\u03b5") ||
        e(s, len, "\u03bf\u03c5\u03bd\u03c4\u03b1\u03bd\u03b5") ||
        e(s, len, "\u03b7\u03b8\u03b7\u03ba\u03b1\u03bd\u03b5")) {
      len -= 7;
      rem = true;
    } else if(len > 6 &&
        e(s, len, "\u03b9\u03bf\u03c4\u03b1\u03bd\u03b5") ||
        e(s, len, "\u03bf\u03bd\u03c4\u03b1\u03bd\u03b5") ||
        e(s, len, "\u03bf\u03c5\u03c3\u03b1\u03bd\u03b5")) {
      len -= 6;
      rem = true;
    } else if(len > 5 &&
        e(s, len, "\u03b1\u03b3\u03b1\u03bd\u03b5") ||
        e(s, len, "\u03b7\u03c3\u03b1\u03bd\u03b5") ||
        e(s, len, "\u03bf\u03c4\u03b1\u03bd\u03b5") ||
        e(s, len, "\u03b7\u03ba\u03b1\u03bd\u03b5")) {
      len -= 5;
      rem = true;
    }

    if(rem && c(EXC8A, s, len)) {
      // add -\u03b1\u03b3\u03b1\u03bd (we rem > 4 chars so its safe)
      len += 4;
      s[len - 4] = '\u03b1';
      s[len - 3] = '\u03b3';
      s[len - 2] = '\u03b1';
      s[len - 1] = '\u03bd';
    }

    if(len > 3 && e(s, len, "\u03b1\u03bd\u03b5")) {
      len -= 3;
      if(ey(s, len) || c(EXC8B, s, len)) {
        len += 2; // add back -\u03b1\u03bd
      }
    }

    return len;
  }

  /** String arrays. */
  private static final String[] EXC9 = {
    "\u03b1\u03b2\u03b1\u03c1", "\u03b2\u03b5\u03bd", "\u03b5\u03bd\u03b1\u03c1",
    "\u03b1\u03b2\u03c1", "\u03b1\u03b4", "\u03b1\u03b8", "\u03b1\u03bd",
    "\u03b1\u03c0\u03bb", "\u03b2\u03b1\u03c1\u03bf\u03bd", "\u03bd\u03c4\u03c1",
    "\u03c3\u03ba", "\u03ba\u03bf\u03c0", "\u03bc\u03c0\u03bf\u03c1",
    "\u03bd\u03b9\u03c6", "\u03c0\u03b1\u03b3",
    "\u03c0\u03b1\u03c1\u03b1\u03ba\u03b1\u03bb", "\u03c3\u03b5\u03c1\u03c0",
    "\u03c3\u03ba\u03b5\u03bb", "\u03c3\u03c5\u03c1\u03c6", "\u03c4\u03bf\u03ba",
    "\u03c5", "\u03b4", "\u03b5\u03bc", "\u03b8\u03b1\u03c1\u03c1", "\u03b8"
  };

  /**
   * Applies rule 9.
   * @param s characters
   * @param l length
   * @return new length
   */
  private int rule9(final char[] s, final int l) {
    int len = l;
    if(len > 5 && e(s, len, "\u03b7\u03c3\u03b5\u03c4\u03b5")) len -= 5;

    if(len > 3 && e(s, len, "\u03b5\u03c4\u03b5")) {
      len -= 3;
      if(c(EXC9, s, len) || ey(s, len) ||
        e(s, len, "\u03bf\u03b4") ||
        e(s, len, "\u03b1\u03b9\u03c1") ||
        e(s, len, "\u03c6\u03bf\u03c1") ||
        e(s, len, "\u03c4\u03b1\u03b8") ||
        e(s, len, "\u03b4\u03b9\u03b1\u03b8") ||
        e(s, len, "\u03c3\u03c7") ||
        e(s, len, "\u03b5\u03bd\u03b4") ||
        e(s, len, "\u03b5\u03c5\u03c1") ||
        e(s, len, "\u03c4\u03b9\u03b8") ||
        e(s, len, "\u03c5\u03c0\u03b5\u03c1\u03b8") ||
        e(s, len, "\u03c1\u03b1\u03b8") ||
        e(s, len, "\u03b5\u03bd\u03b8") ||
        e(s, len, "\u03c1\u03bf\u03b8") ||
        e(s, len, "\u03c3\u03b8") ||
        e(s, len, "\u03c0\u03c5\u03c1") ||
        e(s, len, "\u03b1\u03b9\u03bd") ||
        e(s, len, "\u03c3\u03c5\u03bd\u03b4") ||
        e(s, len, "\u03c3\u03c5\u03bd") ||
        e(s, len, "\u03c3\u03c5\u03bd\u03b8") ||
        e(s, len, "\u03c7\u03c9\u03c1") ||
        e(s, len, "\u03c0\u03bf\u03bd") ||
        e(s, len, "\u03b2\u03c1") ||
        e(s, len, "\u03ba\u03b1\u03b8") ||
        e(s, len, "\u03b5\u03c5\u03b8") ||
        e(s, len, "\u03b5\u03ba\u03b8") ||
        e(s, len, "\u03bd\u03b5\u03c4") ||
        e(s, len, "\u03c1\u03bf\u03bd") ||
        e(s, len, "\u03b1\u03c1\u03ba") ||
        e(s, len, "\u03b2\u03b1\u03c1") ||
        e(s, len, "\u03b2\u03bf\u03bb") ||
        e(s, len, "\u03c9\u03c6\u03b5\u03bb")) {
        len += 2; // add back -\u03b5\u03c4
      }
    }

    return len;
  }

  /**
   * Applies rule 10.
   * @param s characters
   * @param l length
   * @return new length
   */
  private int rule10(final char[] s, final int l) {
    int len = l;
    if(len > 5 && (
        e(s, len, "\u03bf\u03bd\u03c4\u03b1\u03c3") ||
        e(s, len, "\u03c9\u03bd\u03c4\u03b1\u03c3"))) {
      len -= 5;
      if(len == 3 && e(s, len, "\u03b1\u03c1\u03c7")) {
        len += 3; // add back *\u03bd\u03c4
        s[len - 3] = '\u03bf';
      }
      if(e(s, len, "\u03ba\u03c1\u03b5")) {
        len += 3; // add back *\u03bd\u03c4
        s[len - 3] = '\u03c9';
      }
    }

    return len;
  }

  /**
   * Applies rule 11.
   * @param s characters
   * @param l length
   * @return new length
   */
  private int rule11(final char[] s, final int l) {
    int len = l;
    if(len > 6 && e(s, len, "\u03bf\u03bc\u03b1\u03c3\u03c4\u03b5")) {
      len -= 6;
      if(len == 2 && e(s, len, "\u03bf\u03bd")) {
        len += 5; // add back -\u03bf\u03bc\u03b1\u03c3\u03c4
      }
    } else if(len > 7 && e(s, len, "\u03b9\u03bf\u03bc\u03b1\u03c3\u03c4\u03b5")) {
      len -= 7;
      if(len == 2 && e(s, len, "\u03bf\u03bd")) {
        len += 5;
        s[len - 5] = '\u03bf';
        s[len - 4] = '\u03bc';
        s[len - 3] = '\u03b1';
        s[len - 2] = '\u03c3';
        s[len - 1] = '\u03c4';
      }
    }
    return len;
  }

  /** String arrays. */
  private static final String[] EXC12A = {
    "\u03c0", "\u03b1\u03c0", "\u03c3\u03c5\u03bc\u03c0",
    "\u03b1\u03c3\u03c5\u03bc\u03c0", "\u03b1\u03ba\u03b1\u03c4\u03b1\u03c0",
    "\u03b1\u03bc\u03b5\u03c4\u03b1\u03bc\u03c6"
  };

  /** String arrays. */
  private static final String[] EXC12B = {
    "\u03b1\u03bb", "\u03b1\u03c1", "\u03b5\u03ba\u03c4\u03b5\u03bb", "\u03b6", "\u03bc",
    "\u03be", "\u03c0\u03b1\u03c1\u03b1\u03ba\u03b1\u03bb", "\u03b1\u03c1",
    "\u03c0\u03c1\u03bf", "\u03bd\u03b9\u03c3"
  };

  /**
   * Applies rule 12.
   * @param s characters
   * @param l length
   * @return new length
   */
  private int rule12(final char[] s, final int l) {
    int len = l;
    if(len > 5 && e(s, len, "\u03b9\u03b5\u03c3\u03c4\u03b5")) {
      len -= 5;
      if(c(EXC12A, s, len)) len += 4; // add back -\u03b9\u03b5\u03c3\u03c4
    }

    if(len > 4 && e(s, len, "\u03b5\u03c3\u03c4\u03b5")) {
      len -= 4;
      if(c(EXC12B, s, len)) len += 3; // add back -\u03b5\u03c3\u03c4
    }

    return len;
  }

  /** String arrays. */
  private static final String[] EXC13 = {
    "\u03b4\u03b9\u03b1\u03b8", "\u03b8",
    "\u03c0\u03b1\u03c1\u03b1\u03ba\u03b1\u03c4\u03b1\u03b8",
    "\u03c0\u03c1\u03bf\u03c3\u03b8", "\u03c3\u03c5\u03bd\u03b8"
  };

  /**
   * Applies rule 13.
   * @param s characters
   * @param l length
   * @return new length
   */
  private int rule13(final char[] s, final int l) {
    int len = l;
    if(len > 6 && e(s, len, "\u03b7\u03b8\u03b7\u03ba\u03b5\u03c3")) {
      len -= 6;
    } else if(len > 5 && (
        e(s, len, "\u03b7\u03b8\u03b7\u03ba\u03b1") ||
        e(s, len, "\u03b7\u03b8\u03b7\u03ba\u03b5"))) {
      len -= 5;
    }

    boolean rem = false;
    if(len > 4 && e(s, len, "\u03b7\u03ba\u03b5\u03c3")) {
      len -= 4;
      rem = true;
    } else if(len > 3 && (
        e(s, len, "\u03b7\u03ba\u03b1") ||
        e(s, len, "\u03b7\u03ba\u03b5"))) {
      len -= 3;
      rem = true;
    }

    if(rem && (c(EXC13, s, len) ||
        e(s, len, "\u03c3\u03ba\u03c9\u03bb") ||
        e(s, len, "\u03c3\u03ba\u03bf\u03c5\u03bb") ||
        e(s, len, "\u03bd\u03b1\u03c1\u03b8") ||
        e(s, len, "\u03c3\u03c6") ||
        e(s, len, "\u03bf\u03b8") ||
        e(s, len, "\u03c0\u03b9\u03b8"))) {
      len += 2; // add back the -\u03b7\u03ba
    }

    return len;
  }

  /** String arrays. */
  private static final String[] EXC14 = {
    "\u03c6\u03b1\u03c1\u03bc\u03b1\u03ba", "\u03c7\u03b1\u03b4", "\u03b1\u03b3\u03ba",
    "\u03b1\u03bd\u03b1\u03c1\u03c1", "\u03b2\u03c1\u03bf\u03bc",
    "\u03b5\u03ba\u03bb\u03b9\u03c0", "\u03bb\u03b1\u03bc\u03c0\u03b9\u03b4",
    "\u03bb\u03b5\u03c7", "\u03bc", "\u03c0\u03b1\u03c4", "\u03c1", "\u03bb",
    "\u03bc\u03b5\u03b4", "\u03bc\u03b5\u03c3\u03b1\u03b6",
    "\u03c5\u03c0\u03bf\u03c4\u03b5\u03b9\u03bd", "\u03b1\u03bc", "\u03b1\u03b9\u03b8",
    "\u03b1\u03bd\u03b7\u03ba", "\u03b4\u03b5\u03c3\u03c0\u03bf\u03b6",
    "\u03b5\u03bd\u03b4\u03b9\u03b1\u03c6\u03b5\u03c1", "\u03b4\u03b5",
    "\u03b4\u03b5\u03c5\u03c4\u03b5\u03c1\u03b5\u03c5",
    "\u03ba\u03b1\u03b8\u03b1\u03c1\u03b5\u03c5", "\u03c0\u03bb\u03b5",
    "\u03c4\u03c3\u03b1"
  };

  /**
   * Applies rule 14.
   * @param s characters
   * @param l length
   * @return new length
   */
  private int rule14(final char[] s, final int l) {
    int len = l;
    boolean rem = false;

    if(len > 5 && e(s, len, "\u03bf\u03c5\u03c3\u03b5\u03c3")) {
      len -= 5;
      rem = true;
    } else if(len > 4 && (
        e(s, len, "\u03bf\u03c5\u03c3\u03b1") ||
        e(s, len, "\u03bf\u03c5\u03c3\u03b5"))) {
      len -= 4;
      rem = true;
    }

    if(rem && (c(EXC14, s, len) || ev(s, len) ||
      e(s, len, "\u03c0\u03bf\u03b4\u03b1\u03c1") ||
      e(s, len, "\u03b2\u03bb\u03b5\u03c0") ||
      e(s, len, "\u03c0\u03b1\u03bd\u03c4\u03b1\u03c7") ||
      e(s, len, "\u03c6\u03c1\u03c5\u03b4") ||
      e(s, len, "\u03bc\u03b1\u03bd\u03c4\u03b9\u03bb") ||
      e(s, len, "\u03bc\u03b1\u03bb\u03bb") ||
      e(s, len, "\u03ba\u03c5\u03bc\u03b1\u03c4") ||
      e(s, len, "\u03bb\u03b1\u03c7") ||
      e(s, len, "\u03bb\u03b7\u03b3") ||
      e(s, len, "\u03c6\u03b1\u03b3") ||
      e(s, len, "\u03bf\u03bc") ||
      e(s, len, "\u03c0\u03c1\u03c9\u03c4"))) {
      len += 3; // add back -\u03bf\u03c5\u03c3
    }

    return len;
  }

  /** String arrays. */
  private static final String[] EXC15A = {
    "\u03b1\u03b2\u03b1\u03c3\u03c4", "\u03c0\u03bf\u03bb\u03c5\u03c6",
    "\u03b1\u03b4\u03b7\u03c6", "\u03c0\u03b1\u03bc\u03c6", "\u03c1",
    "\u03b1\u03c3\u03c0", "\u03b1\u03c6", "\u03b1\u03bc\u03b1\u03bb",
    "\u03b1\u03bc\u03b1\u03bb\u03bb\u03b9", "\u03b1\u03bd\u03c5\u03c3\u03c4",
    "\u03b1\u03c0\u03b5\u03c1", "\u03b1\u03c3\u03c0\u03b1\u03c1",
    "\u03b1\u03c7\u03b1\u03c1", "\u03b4\u03b5\u03c1\u03b2\u03b5\u03bd",
    "\u03b4\u03c1\u03bf\u03c3\u03bf\u03c0", "\u03be\u03b5\u03c6",
    "\u03bd\u03b5\u03bf\u03c0", "\u03bd\u03bf\u03bc\u03bf\u03c4",
    "\u03bf\u03bb\u03bf\u03c0", "\u03bf\u03bc\u03bf\u03c4",
    "\u03c0\u03c1\u03bf\u03c3\u03c4", "\u03c0\u03c1\u03bf\u03c3\u03c9\u03c0\u03bf\u03c0",
    "\u03c3\u03c5\u03bc\u03c0", "\u03c3\u03c5\u03bd\u03c4", "\u03c4",
    "\u03c5\u03c0\u03bf\u03c4", "\u03c7\u03b1\u03c1", "\u03b1\u03b5\u03b9\u03c0",
    "\u03b1\u03b9\u03bc\u03bf\u03c3\u03c4", "\u03b1\u03bd\u03c5\u03c0",
    "\u03b1\u03c0\u03bf\u03c4", "\u03b1\u03c1\u03c4\u03b9\u03c0",
    "\u03b4\u03b9\u03b1\u03c4", "\u03b5\u03bd", "\u03b5\u03c0\u03b9\u03c4",
    "\u03ba\u03c1\u03bf\u03ba\u03b1\u03bb\u03bf\u03c0",
    "\u03c3\u03b9\u03b4\u03b7\u03c1\u03bf\u03c0", "\u03bb", "\u03bd\u03b1\u03c5",
    "\u03bf\u03c5\u03bb\u03b1\u03bc", "\u03bf\u03c5\u03c1", "\u03c0", "\u03c4\u03c1",
    "\u03bc"
  };

  /** String arrays. */
  private static final String[] EXC15B = {
    "\u03c8\u03bf\u03c6", "\u03bd\u03b1\u03c5\u03bb\u03bf\u03c7"
  };

  /**
   * Applies rule 15.
   * @param s characters
   * @param l length
   * @return new length
   */
  private int rule15(final char[] s, final int l) {
    int len = l;
    boolean rem = false;
    if(len > 4 && e(s, len, "\u03b1\u03b3\u03b5\u03c3")) {
      len -= 4;
      rem = true;
    } else if(len > 3 && (
        e(s, len, "\u03b1\u03b3\u03b1") ||
        e(s, len, "\u03b1\u03b3\u03b5"))) {
      len -= 3;
      rem = true;
    }

    if(rem) {
      final boolean cond1 = c(EXC15A, s, len) ||
        e(s, len, "\u03bf\u03c6") ||
        e(s, len, "\u03c0\u03b5\u03bb") ||
        e(s, len, "\u03c7\u03bf\u03c1\u03c4") ||
        e(s, len, "\u03bb\u03bb") ||
        e(s, len, "\u03c3\u03c6") ||
        e(s, len, "\u03c1\u03c0") ||
        e(s, len, "\u03c6\u03c1") ||
        e(s, len, "\u03c0\u03c1") ||
        e(s, len, "\u03bb\u03bf\u03c7") ||
        e(s, len, "\u03c3\u03bc\u03b7\u03bd");

      final boolean cond2 = c(EXC15B, s, len) ||
        e(s, len, "\u03ba\u03bf\u03bb\u03bb");

      if(cond1 && !cond2) len += 2; // add back -\u03b1\u03b3
    }

    return len;
  }

  /** String arrays. */
  private static final String[] EXC16 = {
    "\u03bd", "\u03c7\u03b5\u03c1\u03c3\u03bf\u03bd",
    "\u03b4\u03c9\u03b4\u03b5\u03ba\u03b1\u03bd", "\u03b5\u03c1\u03b7\u03bc\u03bf\u03bd",
    "\u03bc\u03b5\u03b3\u03b1\u03bb\u03bf\u03bd", "\u03b5\u03c0\u03c4\u03b1\u03bd"
  };

  /**
   * Applies rule 16.
   * @param s characters
   * @param l length
   * @return new length
   */
  private int rule16(final char[] s, final int l) {
    int len = l;
    boolean rem = false;
    if(len > 4 && e(s, len, "\u03b7\u03c3\u03bf\u03c5")) {
      len -= 4;
      rem = true;
    } else if(len > 3 && (
        e(s, len, "\u03b7\u03c3\u03b5") ||
        e(s, len, "\u03b7\u03c3\u03b1"))) {
      len -= 3;
      rem = true;
    }

    if(rem && c(EXC16, s, len)) len += 2; // add back -\u03b7\u03c3

    return len;
  }

  /** String arrays. */
  private static final String[] EXC17 = {
    "\u03b1\u03c3\u03b2", "\u03c3\u03b2", "\u03b1\u03c7\u03c1", "\u03c7\u03c1",
    "\u03b1\u03c0\u03bb", "\u03b1\u03b5\u03b9\u03bc\u03bd",
    "\u03b4\u03c5\u03c3\u03c7\u03c1", "\u03b5\u03c5\u03c7\u03c1",
    "\u03ba\u03bf\u03b9\u03bd\u03bf\u03c7\u03c1", "\u03c0\u03b1\u03bb\u03b9\u03bc\u03c8"
  };

  /**
   * Applies rule 17.
   * @param s characters
   * @param l length
   * @return new length
   */
  private int rule17(final char[] s, final int l) {
    int len = l;
    if(len > 4 && e(s, len, "\u03b7\u03c3\u03c4\u03b5")) {
      len -= 4;
      if(c(EXC17, s, len)) len += 3; // add back the -\u03b7\u03c3\u03c4
    }

    return len;
  }

  /** String arrays. */
  private static final String[] EXC18 = {
    "\u03bd", "\u03c1", "\u03c3\u03c0\u03b9",
    "\u03c3\u03c4\u03c1\u03b1\u03b2\u03bf\u03bc\u03bf\u03c5\u03c4\u03c3",
    "\u03ba\u03b1\u03ba\u03bf\u03bc\u03bf\u03c5\u03c4\u03c3", "\u03b5\u03be\u03c9\u03bd"
  };

  /**
   * Applies rule 18.
   * @param s characters
   * @param l length
   * @return new length
   */
  private int rule18(final char[] s, final int l) {
    boolean rem = false;

    int len = l;
    if(len > 6 && (
        e(s, len, "\u03b7\u03c3\u03bf\u03c5\u03bd\u03b5") ||
        e(s, len, "\u03b7\u03b8\u03bf\u03c5\u03bd\u03b5"))) {
      len -= 6;
      rem = true;
    } else if(len > 4 && e(s, len, "\u03bf\u03c5\u03bd\u03b5")) {
      len -= 4;
      rem = true;
    }

    if(rem && c(EXC18, s, len)) {
      len += 3;
      s[len - 3] = '\u03bf';
      s[len - 2] = '\u03c5';
      s[len - 1] = '\u03bd';
    }
    return len;
  }

  /** String arrays. */
  private static final String[] EXC19 = {
    "\u03c0\u03b1\u03c1\u03b1\u03c3\u03bf\u03c5\u03c3", "\u03c6", "\u03c7",
    "\u03c9\u03c1\u03b9\u03bf\u03c0\u03bb", "\u03b1\u03b6",
    "\u03b1\u03bb\u03bb\u03bf\u03c3\u03bf\u03c5\u03c3", "\u03b1\u03c3\u03bf\u03c5\u03c3"
  };

  /**
   * Applies rule 19.
   * @param s characters
   * @param l length
   * @return new length
   */
  private int rule19(final char[] s, final int l) {
    int len = l;
    boolean rem = false;

    if(len > 6 && (
        e(s, len, "\u03b7\u03c3\u03bf\u03c5\u03bc\u03b5") ||
        e(s, len, "\u03b7\u03b8\u03bf\u03c5\u03bc\u03b5"))) {
      len -= 6;
      rem = true;
    } else if(len > 4 && e(s, len, "\u03bf\u03c5\u03bc\u03b5")) {
      len -= 4;
      rem = true;
    }

    if(rem && c(EXC19, s, len)) {
      len += 3;
      s[len - 3] = '\u03bf';
      s[len - 2] = '\u03c5';
      s[len - 1] = '\u03bc';
    }
    return len;
  }

  /**
   * Applies rule 20.
   * @param s characters
   * @param l length
   * @return new length
   */
  private int rule20(final char[] s, final int l) {
    int len = l;
    if(len > 5 && (
      e(s, len, "\u03bc\u03b1\u03c4\u03c9\u03bd") ||
      e(s, len, "\u03bc\u03b1\u03c4\u03bf\u03c3"))) len -= 3;
    else if(len > 4 && e(s, len, "\u03bc\u03b1\u03c4\u03b1")) len -= 2;
    return len;
  }

  /**
   * Applies rule 21.
   * @param s characters
   * @param l length
   * @return new length
   */
  private int rule21(final char[] s, final int l) {
    if(l > 9 && e(s, l, "\u03b9\u03bf\u03bd\u03c4\u03bf\u03c5\u03c3\u03b1\u03bd"))
      return l - 9;

    if(l > 8 && (
      e(s, l, "\u03b9\u03bf\u03bc\u03b1\u03c3\u03c4\u03b1\u03bd") ||
      e(s, l, "\u03b9\u03bf\u03c3\u03b1\u03c3\u03c4\u03b1\u03bd") ||
      e(s, l, "\u03b9\u03bf\u03c5\u03bc\u03b1\u03c3\u03c4\u03b5") ||
      e(s, l, "\u03bf\u03bd\u03c4\u03bf\u03c5\u03c3\u03b1\u03bd"))) return l - 8;

    if(l > 7 && (
      e(s, l, "\u03b9\u03b5\u03bc\u03b1\u03c3\u03c4\u03b5") ||
      e(s, l, "\u03b9\u03b5\u03c3\u03b1\u03c3\u03c4\u03b5") ||
      e(s, l, "\u03b9\u03bf\u03bc\u03bf\u03c5\u03bd\u03b1") ||
      e(s, l, "\u03b9\u03bf\u03c3\u03b1\u03c3\u03c4\u03b5") ||
      e(s, l, "\u03b9\u03bf\u03c3\u03bf\u03c5\u03bd\u03b1") ||
      e(s, l, "\u03b9\u03bf\u03c5\u03bd\u03c4\u03b1\u03b9") ||
      e(s, l, "\u03b9\u03bf\u03c5\u03bd\u03c4\u03b1\u03bd") ||
      e(s, l, "\u03b7\u03b8\u03b7\u03ba\u03b1\u03c4\u03b5") ||
      e(s, l, "\u03bf\u03bc\u03b1\u03c3\u03c4\u03b1\u03bd") ||
      e(s, l, "\u03bf\u03c3\u03b1\u03c3\u03c4\u03b1\u03bd") ||
      e(s, l, "\u03bf\u03c5\u03bc\u03b1\u03c3\u03c4\u03b5"))) return l - 7;

    if(l > 6 && (
      e(s, l, "\u03b9\u03bf\u03bc\u03bf\u03c5\u03bd") ||
      e(s, l, "\u03b9\u03bf\u03bd\u03c4\u03b1\u03bd") ||
      e(s, l, "\u03b9\u03bf\u03c3\u03bf\u03c5\u03bd") ||
      e(s, l, "\u03b7\u03b8\u03b5\u03b9\u03c4\u03b5") ||
      e(s, l, "\u03b7\u03b8\u03b7\u03ba\u03b1\u03bd") ||
      e(s, l, "\u03bf\u03bc\u03bf\u03c5\u03bd\u03b1") ||
      e(s, l, "\u03bf\u03c3\u03b1\u03c3\u03c4\u03b5") ||
      e(s, l, "\u03bf\u03c3\u03bf\u03c5\u03bd\u03b1") ||
      e(s, l, "\u03bf\u03c5\u03bd\u03c4\u03b1\u03b9") ||
      e(s, l, "\u03bf\u03c5\u03bd\u03c4\u03b1\u03bd") ||
      e(s, l, "\u03bf\u03c5\u03c3\u03b1\u03c4\u03b5"))) return l - 6;

    if(l > 5 && (
      e(s, l, "\u03b1\u03b3\u03b1\u03c4\u03b5") ||
      e(s, l, "\u03b9\u03b5\u03bc\u03b1\u03b9") ||
      e(s, l, "\u03b9\u03b5\u03c4\u03b1\u03b9") ||
      e(s, l, "\u03b9\u03b5\u03c3\u03b1\u03b9") ||
      e(s, l, "\u03b9\u03bf\u03c4\u03b1\u03bd") ||
      e(s, l, "\u03b9\u03bf\u03c5\u03bc\u03b1") ||
      e(s, l, "\u03b7\u03b8\u03b5\u03b9\u03c3") ||
      e(s, l, "\u03b7\u03b8\u03bf\u03c5\u03bd") ||
      e(s, l, "\u03b7\u03ba\u03b1\u03c4\u03b5") ||
      e(s, l, "\u03b7\u03c3\u03b1\u03c4\u03b5") ||
      e(s, l, "\u03b7\u03c3\u03bf\u03c5\u03bd") ||
      e(s, l, "\u03bf\u03bc\u03bf\u03c5\u03bd") ||
      e(s, l, "\u03bf\u03bd\u03c4\u03b1\u03b9") ||
      e(s, l, "\u03bf\u03bd\u03c4\u03b1\u03bd") ||
      e(s, l, "\u03bf\u03c3\u03bf\u03c5\u03bd") ||
      e(s, l, "\u03bf\u03c5\u03bc\u03b1\u03b9") ||
      e(s, l, "\u03bf\u03c5\u03c3\u03b1\u03bd"))) return l - 5;

    if(l > 4 && (
      e(s, l, "\u03b1\u03b3\u03b1\u03bd") ||
      e(s, l, "\u03b1\u03bc\u03b1\u03b9") ||
      e(s, l, "\u03b1\u03c3\u03b1\u03b9") ||
      e(s, l, "\u03b1\u03c4\u03b1\u03b9") ||
      e(s, l, "\u03b5\u03b9\u03c4\u03b5") ||
      e(s, l, "\u03b5\u03c3\u03b1\u03b9") ||
      e(s, l, "\u03b5\u03c4\u03b1\u03b9") ||
      e(s, l, "\u03b7\u03b4\u03b5\u03c3") ||
      e(s, l, "\u03b7\u03b4\u03c9\u03bd") ||
      e(s, l, "\u03b7\u03b8\u03b5\u03b9") ||
      e(s, l, "\u03b7\u03ba\u03b1\u03bd") ||
      e(s, l, "\u03b7\u03c3\u03b1\u03bd") ||
      e(s, l, "\u03b7\u03c3\u03b5\u03b9") ||
      e(s, l, "\u03b7\u03c3\u03b5\u03c3") ||
      e(s, l, "\u03bf\u03bc\u03b1\u03b9") ||
      e(s, l, "\u03bf\u03c4\u03b1\u03bd"))) return l - 4;

    if(l > 3 && (
      e(s, l, "\u03b1\u03b5\u03b9") ||
      e(s, l, "\u03b5\u03b9\u03c3") ||
      e(s, l, "\u03b7\u03b8\u03c9") ||
      e(s, l, "\u03b7\u03c3\u03c9") ||
      e(s, l, "\u03bf\u03c5\u03bd") ||
      e(s, l, "\u03bf\u03c5\u03c3"))) return l - 3;

    if(l > 2 && (
      e(s, l, "\u03b1\u03bd") ||
      e(s, l, "\u03b1\u03c3") ||
      e(s, l, "\u03b1\u03c9") ||
      e(s, l, "\u03b5\u03b9") ||
      e(s, l, "\u03b5\u03c3") ||
      e(s, l, "\u03b7\u03c3") ||
      e(s, l, "\u03bf\u03b9") ||
      e(s, l, "\u03bf\u03c3") ||
      e(s, l, "\u03bf\u03c5") ||
      e(s, l, "\u03c5\u03c3") ||
      e(s, l, "\u03c9\u03bd"))) return l - 2;

    if(l > 1 && ev(s, l)) return l - 1;

    return l;
  }

  /**
   * Applies rule 22.
   * @param s characters
   * @param l length
   * @return new length
   */
  private int rule22(final char[] s, final int l) {
    if(e(s, l, "\u03b5\u03c3\u03c4\u03b5\u03c1") ||
       e(s, l, "\u03b5\u03c3\u03c4\u03b1\u03c4")) return l - 5;
    if(e(s, l, "\u03bf\u03c4\u03b5\u03c1") ||
       e(s, l, "\u03bf\u03c4\u03b1\u03c4") ||
       e(s, l, "\u03c5\u03c4\u03b5\u03c1") ||
       e(s, l, "\u03c5\u03c4\u03b1\u03c4") ||
       e(s, l, "\u03c9\u03c4\u03b5\u03c1") ||
       e(s, l, "\u03c9\u03c4\u03b1\u03c4")) return l - 4;
    return l;
  }

  /**
   * Checks if the specified characters end with a specified suffix.
   * @param s characters
   * @param l length
   * @param suf suffix
   * @return result of check
   */
  private boolean e(final char[] s, final int l, final String suf) {
    final int sl = suf.length();
    if(sl > l) return false;
    for(int i = sl - 1; i >= 0; i--) {
      if(s[l - (sl - i)] != suf.charAt(i)) return false;
    }
    return true;
  }

  /**
   * Checks if the specified characters end with a vowel.
   * @param s characters
   * @param l length
   * @return result of check
   */
  private boolean ev(final char[] s, final int l) {
    if(l == 0) return false;
    switch(s[l - 1]) {
      case '\u03b1':
      case '\u03b5':
      case '\u03b7':
      case '\u03b9':
      case '\u03bf':
      case '\u03c5':
      case '\u03c9':
        return true;
      default:
        return false;
    }
  }

  /**
   * Checks if the specified characters end with a vowel, but no y.
   * @param s characters
   * @param l length
   * @return result of check
   */
  private boolean ey(final char[] s, final int l) {
    if(l == 0) return false;
    switch(s[l - 1]) {
      case '\u03b1':
      case '\u03b5':
      case '\u03b7':
      case '\u03b9':
      case '\u03bf':
      case '\u03c9':
        return true;
      default:
        return false;
    }
  }

  /**
   * Checks if the specified characters are contained in the string array.
   * @param strings string array
   * @param s characters
   * @param l length of characters
   * @return result of check
   */
  private boolean c(final String[] strings, final char[] s, final int l) {
    for(final String e : strings) {
      final int el = e.length();
      if(l != el) continue;
      int i = -1;
      while(++i < l && e.charAt(i) == s[i]);
      if(i == l) return true;
    }
    return false;
  }
}
