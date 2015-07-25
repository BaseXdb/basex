package org.basex.util.ft;

import java.util.*;

import org.basex.util.*;

/**
 * German stemming algorithm, derived from the Apache Lucene project and the
 * report "A Fast and Simple Stemming Algorithm for German Words" by
 * J&ouml;rg Caumanns.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class GermanStemmer extends InternalStemmer {
  /** Removed characters. */
  private int subst;

  /**
   * Constructor.
   * @param fti full-text iterator
   */
  GermanStemmer(final FTIterator fti) {
    super(fti);
  }

  @Override
  Stemmer get(final Language lang, final FTIterator fti) {
    return new GermanStemmer(fti);
  }

  @Override
  Collection<Language> languages() {
    return collection("de");
  }

  @Override
  protected byte[] stem(final byte[] word) {
    subst = 0;
    return part(resub(opt(strip(subst(new TokenBuilder(word)))))).finish();
  }

  /**
   * Does some substitutions.
   * @param tb string builder
   * @return substituted string
   */
  private TokenBuilder subst(final TokenBuilder tb) {
    subst = 0;
    final int s = tb.size();
    final TokenBuilder tmp = new TokenBuilder(s);
    int ls = 0;
    int nx = tb.cp(0);
    for(int c = 0; c < s;) {
      int ch = nx;
      c += tb.cl(c);
      nx = c < s ? tb.cp(c) : 0;
      int sb = 0;
      if(ch == ls) {
        ch = '*';
      } else if(ch == '\u00e4') {
        ch = 'a';
      } else if(ch == '\u00f6') {
        ch = 'o';
      } else if(ch == '\u00fc') {
        ch = 'u';
      } else if(ch == '\u00df') {
        tmp.add('s');
        ch = 's';
        subst++;
      } else if(ch == 's' && nx == 'c' && c + 1 < s && tb.get(c + 1) == 'h') {
        ch = '\1';
        sb = 2;
      } else if(ch == 'c' && nx == 'h') {
        ch = '\2';
        sb = 1;
      } else if(ch == 'e' && nx == 'i') {
        ch = '\3';
        sb = 1;
      } else if(ch == 'i' && nx == 'e') {
        ch = '\4';
        sb = 1;
      } else if(ch == 'i' && nx == 'g') {
        ch = '\5';
        sb = 1;
      } else if(ch == 's' && nx == 't') {
        ch = '\6';
        sb = 1;
      }
      if(sb > 0) {
        c += sb;
        nx = c < s ? tb.cp(c) : 0;
        subst += sb;
      }
      ls = ch;
      tmp.add(ch);
    }
    return tmp;
  }

  /**
   * Strips suffixes.
   * @param tb token builder
   * @return token builder
   */
  private TokenBuilder strip(final TokenBuilder tb) {
    while(tb.size() > 3) {
      final int tl = tb.size(), c1 = tb.get(tl - 1), c2 = tb.get(tl - 2);
      if(tl + subst > 5 && c2 == 'n' && c1 == 'd' ||
         tl + subst > 4 && c2 == 'e' && (c1 == 'm' || c1 == 'r')) {
        tb.size(tl - 2);
      } else if(c1 == 'e' || c1 == 's' || c1 == 'n' || c1 == 't') {
        tb.size(tl - 1);
      } else {
        break;
      }
    }
    return tb;
  }

  /**
   * Does optimizations.
   * @param tb token builder
   * @return token builder
   */
  private TokenBuilder opt(final TokenBuilder tb) {
    int tl = tb.size();
    if(tl > 5 && tb.get(tl - 5) == 'e' && tb.get(tl - 4) == 'r' &&
      tb.get(tl - 3) == 'i' && tb.get(tl - 2) == 'n' && tb.get(tl - 1) == '*') {
      tb.size(tl - 1);
      strip(tb);
    }
    tl = tb.size();
    if(tl > 0 && tb.get(tl - 1) == 'z') tb.set(tl - 1, (byte) 'x');
    return tb;
  }

  /**
   * Undoes the changes made by substitute.
   * @param tb token builder
   * @return new token builder
   */
  private static TokenBuilder resub(final TokenBuilder tb) {
    final TokenBuilder tmp = new TokenBuilder();
    final int s = tb.size();
    for(int c = 0; c < s; c++) {
      final int ch = tb.get(c);
      if(ch == '*') {
        tmp.add(tmp.get(c - 1));
      } else if(ch == '\1') {
        tmp.add('s').add('c').add('h');
      } else if(ch == '\2') {
        tmp.add('c').add('h');
      } else if(ch == '\3') {
        tmp.add('e').add('i');
      } else if(ch == '\4') {
        tmp.add('i').add('e');
      } else if(ch == '\5') {
        tmp.add('i').add('g');
      } else if(ch == '\6') {
        tmp.add('s').add('t');
      } else {
        tmp.add(ch);
      }
    }
    return tmp;
  }

  /**
   * Removes a particle denotion ("ge") from a term.
   * @param tb token builder
   * @return token builder
   */
  private static TokenBuilder part(final TokenBuilder tb) {
    for(int c = 0; c < tb.size() - 4; c++) {
      if(tb.get(c) == 'g' && tb.get(c + 1) == 'e' &&
          tb.get(c + 2) == 'g' && tb.get(c + 3) == 'e') {
        tb.delete(c, 2);
        break;
      }
    }
    return tb;
  }
}
