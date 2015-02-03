package org.basex.util.ft;

import java.util.*;

import org.basex.util.*;

/**
 * Indonesian stemming algorithm, derrived from Lucene 3.3.0 Indonesian stemmer
 * implementation which is based on report "A Study of Stemming Effects on
 * Information Retrieval in Bahasa Indonesia" by Fadillah Z Tala.
 * http://www.illc.uva.nl/Publications/ResearchReports/MoL-2003-02.text.pdf
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Andria Arisal
 */
final class IndonesianStemmer extends InternalStemmer {
  /** Private variables. */
  private int numSyllables;
  /** Flags. */
  private int flags;
  /**
   * Flag for ke- prefix.
   * e.g.: <b>ke-</b>terang-an
   */
  private static final int REMOVED_KE = 1;
  /**
   * Flag for peng- prefix.
   * e.g.: <b>peng-</b>umum-an
   */
  private static final int REMOVED_PENG = 2;
  /**
   * Flag for di- prefix.
   * e.g.: <b>di-</b>keluar-kan
   */
  private static final int REMOVED_DI = 4;
  /**
   * Flag for meng- prefix.
   * e.g.: <b>meng-</b>ukur
   */
  private static final int REMOVED_MENG = 8;
  /**
   * Flag for ter- prefix.
   * e.g.: <b>ter-</b>jadi
   */
  private static final int REMOVED_TER = 16;
  /**
   * Flag for ber- prefix.
   * e.g.: <b>ber-</b>tanding
   */
  private static final int REMOVED_BER = 32;
  /**
   * Flag for pe- prefix.
   * e.g.: <b>pe-</b>nyanyi
   */
  private static final int REMOVED_PE = 64;

  /**
   * Constructor.
   * @param fti full-text iterator
   */
  IndonesianStemmer(final FTIterator fti) {
    super(fti);
  }

  @Override
  Stemmer get(final Language lang, final FTIterator fti) {
    return new IndonesianStemmer(fti);
  }

  @Override
  Collection<Language> languages() {
    return collection("in");
  }

  @Override
  protected byte[] stem(final byte[] word) {
    final TokenBuilder tb = new TokenBuilder(word);
    numSyllables = countSyllables(tb);
    int s = tb.size();

    if(numSyllables > 2) remParticle(tb);
    if(numSyllables > 2) remPossesivePronous(tb);
    if(numSyllables > 2) remFirstPrefix(tb);

    final int n = tb.size();
    if(n != s) {
      // a rule is fired
      if(numSyllables > 2) {
        remSuffix(tb);
        s = tb.size();
      }
      // a rule is fired again
      if(n != s && numSyllables > 2) {
        remSecondPrefix(tb);
        return tb.finish();
      }
      return tb.finish();
    }
    // fail
    if(numSyllables > 2) remSecondPrefix(tb);
    if(numSyllables > 2) remSuffix(tb);
    return tb.finish();
  }

  /**
   * Count syllabes. [p.9] A syllable contains at least one vowel
   * @param tb token builder
   * @return integer
   */
  private static int countSyllables(final TokenBuilder tb) {
    int t = 0;
    final int s = tb.size();
    for(int c = 0; c < s; c++) {
      final byte ch = tb.get(c);
      if(isVowel(ch)) t++;
    }
    return t;
  }

  /**
   * Check whether the character input is vowel. (a, e, i, o, u)
   * @param c char
   * @return boolean
   */
  private static boolean isVowel(final byte c) {
    switch(c) {
      case 'a':
      case 'e':
      case 'i':
      case 'o':
      case 'u':
        return true;
      default:
        return false;
    }
  }

  /**
   * Remove particles. (-kah, -lah, -pun, -tah)
   * @param tb token builder
   */
  private void remParticle(final TokenBuilder tb) {
    final int tl = tb.size();
    if(tl > 3) {
      final int c1 = tb.get(tl - 1), c2 = tb.get(tl - 2), c3 = tb.get(tl - 3);
      if(c3 == 'k' && c2 == 'a' && c1 == 'h' || c3 == 'l' && c2 == 'a' && c1 == 'h' ||
         c3 == 'p' && c2 == 'u' && c1 == 'n' || c3 == 't' && c2 == 'a' && c1 == 'h') {
        numSyllables--;
        tb.size(tl - 3);
      }
    }
  }

  /**
   * Remove possessive pronouns. (-ku, -mu, -nya)
   * @param tb token builder
   */
  private void remPossesivePronous(final TokenBuilder tb) {
    final int tl = tb.size();

    if(tl > 3) {
      final int c1 = tb.get(tl - 1);
      final int c2 = tb.get(tl - 2);
      final int c3 = tb.get(tl - 3);

      if(c2 == 'k' && c1 == 'u' || c2 == 'm' && c1 == 'u') {
        numSyllables--;
        tb.size(tl - 2);
      } else if(c3 == 'n' && c2 == 'y' && c1 == 'a') {
        numSyllables--;
        tb.size(tl - 3);
      }
    }
  }

  /**
   * Remove suffixes. (-kan, -an, -i (but not -si))
   * @param tb token builder
   */
  private void remSuffix(final TokenBuilder tb) {
    final int tl = tb.size();

    if(tl > 3) {
      final int c1 = tb.get(tl - 1);
      final int c2 = tb.get(tl - 2);
      final int c3 = tb.get(tl - 3);

      if(c3 == 'k' && c2 == 'a' && c1 == 'n'
          && (flags & REMOVED_KE) == 0 && (flags & REMOVED_PENG) == 0
          && (flags & REMOVED_PE) == 0) {
        numSyllables--;
        tb.size(tl - 3);
      } else if(c2 == 'a' && c1 == 'n' && (flags & REMOVED_DI) == 0
          && (flags & REMOVED_MENG) == 0 && (flags & REMOVED_TER) == 0) {
        numSyllables--;
        tb.size(tl - 2);
      } else if(c1 == 'i' && c2 != 's' && (flags & REMOVED_BER) == 0
          && (flags & REMOVED_KE) == 0 && (flags & REMOVED_PENG) == 0) {
        numSyllables--;
        tb.size(tl - 1);
      }
    }
  }

  /**
   * Remove first order prefixes. (meng-, meny-, men-, mem-, me-, peng-, peny-,
   * pen-, pem-, di-, ter-, ke-)
   * @param tb token builder
   */
  private void remFirstPrefix(final TokenBuilder tb) {
    final int tl = tb.size();

    if(tl > 4) {
      final int c1 = tb.get(0);
      final int c2 = tb.get(1);
      final int c3 = tb.get(2);
      final int c4 = tb.get(3);

      if(c1 == 'm' && c2 == 'e' && c3 == 'n' && c4 == 'g') {
        flags |= REMOVED_MENG;
        numSyllables--;
        tb.delete(0, 4);
      } else if(c1 == 'm' && c2 == 'e' && c3 == 'n' && c4 == 'y'
          && tl > 4 && isVowel(tb.get(4))) {
        flags |= REMOVED_MENG;
        final byte setS = 's';
        tb.set(3, setS);
        numSyllables--;
        tb.delete(0, 3);
      } else if(c1 == 'm' && c2 == 'e' && c3 == 'n') {
        flags |= REMOVED_MENG;
        numSyllables--;
        tb.delete(0, 3);
      } else if(c1 == 'm' && c2 == 'e' && c3 == 'm' && tl > 3
          && isVowel(tb.get(3))) {
        flags |= REMOVED_MENG;
        final byte setP = 'p';
        tb.set(2, setP);
        numSyllables--;
        tb.delete(0, 2);
      } else if(c1 == 'm' && c2 == 'e' && c3 == 'm') {
        flags |= REMOVED_MENG;
        numSyllables--;
        tb.delete(0, 3);
      } else if(c1 == 'm' && c2 == 'e') {
        flags |= REMOVED_MENG;
        numSyllables--;
        tb.delete(0, 2);
      } else if(c1 == 'p' && c2 == 'e' && c3 == 'n' && c4 == 'g') {
        flags |= REMOVED_PENG;
        numSyllables--;
        tb.delete(0, 4);
      } else if(c1 == 'p' && c2 == 'e' && c3 == 'n' && c4 == 'y'
          && tl > 4 && isVowel(tb.get(4))) {
        flags |= REMOVED_PENG;
        final byte setS = 's';
        tb.set(3, setS);
        numSyllables--;
        tb.delete(0, 3);
      } else if(c1 == 'p' && c2 == 'e' && c3 == 'n' && tl > 4
          && isVowel(tb.get(3))) {
        flags |= REMOVED_PENG;
        final byte setT = 't';
        tb.set(2, setT);
        numSyllables--;
        tb.delete(0, 2);
      } else if(c1 == 'p' && c2 == 'e' && c3 == 'n') {
        flags |= REMOVED_PENG;
        numSyllables--;
        tb.delete(0, 3);
      } else if(c1 == 'p' && c2 == 'e' && c3 == 'm' && tl > 4
          && isVowel(tb.get(3))) {
        flags |= REMOVED_PENG;
        final byte setP = 'p';
        tb.set(2, setP);
        numSyllables--;
        tb.delete(0, 2);
      } else if(c1 == 'p' && c2 == 'e' && c3 == 'm') {
        flags |= REMOVED_PENG;
        numSyllables--;
        tb.delete(0, 3);
      } else if(c1 == 'd' && c2 == 'i') {
        flags |= REMOVED_DI;
        numSyllables--;
        tb.delete(0, 2);
      } else if(c1 == 't' && c2 == 'e' && c3 == 'r') {
        flags |= REMOVED_TER;
        numSyllables--;
        tb.delete(0, 3);
      } else if(c1 == 'k' && c2 == 'e') {
        flags |= REMOVED_KE;
        numSyllables--;
        tb.delete(0, 2);
      }
    }
  }

  /**
   * Remove second order prefixes. (ber-, bel-, be-, per-, pel-, pe-)
   * @param tb token builder
   */
  private void remSecondPrefix(final TokenBuilder tb) {
    final int tl = tb.size();

    if(tl > 4) {
      final int c1 = tb.get(0);
      final int c2 = tb.get(1);
      final int c3 = tb.get(2);
      final int c4 = tb.get(3);

      if(c1 == 'b' && c2 == 'e' && c3 == 'r') {
        flags |= REMOVED_BER;
        numSyllables--;
        tb.delete(0, 3);
      } else if(c1 == 'b' && c2 == 'e' && c3 == 'l' && c4 == 'a' && tl > 7) {
        if(tb.get(4) == 'j' && tb.get(5) == 'a' && tb.get(6) == 'r') {
          flags |= REMOVED_BER;
          numSyllables--;
          tb.delete(0, 3);
        }
      } else if(c1 == 'b' && c2 == 'e' && !isVowel(tb.get(2)) && c4 == 'e' && tl > 4) {
        if(tb.get(4) == 'r') {
          flags |= REMOVED_BER;
          numSyllables--;
          tb.delete(0, 2);
        }
      } else if(c1 == 'p' && c2 == 'e' && c3 == 'r') {
        flags |= REMOVED_PE;
        numSyllables--;
        tb.delete(0, 3);
      } else if(c1 == 'b' && c2 == 'e' && c3 == 'l' && c4 == 'a' && tl > 7) {
        if(tb.get(4) == 'j' && tb.get(5) == 'a' && tb.get(6) == 'r') {
          flags |= REMOVED_PE;
          numSyllables--;
          tb.delete(0, 3);
        }
      } else if(c1 == 'p' && c2 == 'e') {
        flags |= REMOVED_PE;
        numSyllables--;
        tb.delete(0, 2);
      }
    }
  }
}
