package org.basex.util.ft;

import java.util.*;

import org.basex.util.*;

/**
 * German stemming algorithm, derived from the Apache Lucene project and the
 * report "A Fast and Simple Stemming Algorithm for German Words" by
 * J&ouml;rg Caumanns.
 *
 * @author BaseX Team 2005-21, BSD License
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
    final int wl = word.length;
    return wl == 0 ? Token.EMPTY :
      part(resub(opt(strip(subst(new TokenBuilder(word)))))).finish();
  }

  /**
   * Does some substitutions.
   * @param tb token builder
   * @return modified token builder
   */
  private TokenBuilder subst(final TokenBuilder tb) {
    subst = 0;
    final int s = tb.size();
    final TokenBuilder tmp = new TokenBuilder(s);
    int last = 0, next = s > 0 ? tb.cp(0) : 0;
    for(int c = 0; c < s;) {
      int curr = next;
      c += tb.cl(c);
      next = c < s ? tb.cp(c) : 0;
      int sb = 0;
      if(curr == last) {
        curr = '*';
      } else if(curr == '\u00e4') {
        curr = 'a';
      } else if(curr == '\u00f6') {
        curr = 'o';
      } else if(curr == '\u00fc') {
        curr = 'u';
      } else if(curr == '\u00df') {
        tmp.add('s');
        curr = 's';
        subst++;
      } else if(curr == 's' && next == 'c' && c + 1 < s && tb.get(c + 1) == 'h') {
        curr = '\1';
        sb = 2;
      } else if(curr == 'c' && next == 'h') {
        curr = '\2';
        sb = 1;
      } else if(curr == 'e' && next == 'i') {
        curr = '\3';
        sb = 1;
      } else if(curr == 'i' && next == 'e') {
        curr = '\4';
        sb = 1;
      } else if(curr == 'i' && next == 'g') {
        curr = '\5';
        sb = 1;
      } else if(curr == 's' && next == 't') {
        curr = '\6';
        sb = 1;
      }
      if(sb > 0) {
        c += sb;
        next = c < s ? tb.cp(c) : 0;
        subst += sb;
      }
      last = curr;
      tmp.add(curr);
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
      switch(ch) {
        case '*':  tmp.add(tmp.get(c - 1)); break;
        case '\1': tmp.add('s').add('c').add('h'); break;
        case '\2': tmp.add('c').add('h'); break;
        case '\3': tmp.add('e').add('i'); break;
        case '\4': tmp.add('i').add('e'); break;
        case '\5': tmp.add('i').add('g'); break;
        case '\6': tmp.add('s').add('t'); break;
        default: tmp.add(ch); break;
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
    for(int c = 0; c < tb.size() - 3; c++) {
      if(tb.get(c) == 'g' && tb.get(c + 1) == 'e' && tb.get(c + 2) == 'g' && tb.get(c + 3) == 'e') {
        tb.delete(c, c + 2);
        break;
      }
    }
    return tb;
  }
}
