package org.basex.query.util.format;

import static org.basex.query.util.format.FormatParser.NumeralType.*;
import static org.basex.util.Token.*;

import org.basex.query.util.format.FormatParser.*;
import org.basex.util.*;

/**
 * French language formatter. Can be instantiated via {@link Formatter#get}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class FormatterFR extends Formatter {
  /** Written numbers (1-20). */
  private static final byte[][] WORDS = tokens("z\u00e9ro", "un", "deux", "trois",
      "quatre", "cinq", "six", "sept", "huit", "neuf", "dix", "onze",
      "douze", "treize", "quatorze", "quinze", "seize",
      "dix-sept", "dix-huit", "dix-neuf");
  /** Written numbers (20-100). */
  private static final byte[][] WORDS20 = tokens("", "", "vingt", "trente",
      "quarante", "cinquante", "soixante", "", "quatre-vingt");
  /** Written numbers (1000000, ...). */
  private static final byte[][] WORDS1000000 = tokens(
      "million", "milliard", "billion", "billiard", "trillion");
  /** Ordinal suffix. */
  private static final byte[] IEME = token("i\u00e8me");
  /** First (masculine). */
  private static final byte[] PREMIER = token("premier");
  /** First (feminine). */
  private static final byte[] PREMIERE = token("premi\u00e8re");
  /** Plural suffix. */
  private static final byte[] S = cpToken('s');
  /** Ordinal suffix of digits: 1. */
  private static final byte[] ER = token("er");
  /** Ordinal suffix of digits: other numbers. */
  private static final byte[] E = cpToken('e');

  /** Days. */
  private static final byte[][][] DAYS = {
    tokens("Lundi"), tokens("Mardi"), tokens("Mercredi"), tokens("Jeudi"),
    tokens("Vendredi"), tokens("Samedi"), tokens("Dimanche")
  };
  /** Months. */
  private static final byte[][] MONTHS = tokens(
    "Janvier", "F\u00e9vrier", "Mars", "Avril", "Mai", "Juin",
    "Juillet", "Ao\u00fbt", "Septembre", "Octobre", "Novembre", "D\u00e9cembre");
  /** AM/PM Markers. */
  private static final byte[][] AMPM = tokens("a.m.", "p.m.");
  /** Eras: BC, AD. */
  private static final byte[][] ERAS = tokens("av. J.-C.", "ap. J.-C.");

  /**
   * Constructor.
   */
  FormatterFR() {
    super(MONTHS, DAYS, AMPM, ERAS);
  }

  @Override
  public byte[] word(final long n, final NumeralType numType, final byte[] modifier) {
    // variants of ordinals: feminine (-e, -ère) and plural (-s)
    final byte[] mod = modifier == null || modifier[0] == '%' ? null : delete(modifier, '-');
    final boolean plural = mod != null && endsWith(mod, 's');
    final int first = mod != null ? ch(mod, 0) : 0;
    final boolean feminine = first == 'e' || first == '\u00e8';
    final byte[] suffix = plural ? S : EMPTY;
    if(n == 1 && numType == ORDINAL) {
      return concat(feminine ? PREMIERE : PREMIER, suffix);
    }

    final TokenBuilder tb = new TokenBuilder();
    word(tb, n, numType, suffix, true);
    return tb.finish();
  }

  @Override
  protected boolean ordinalWords() {
    return true;
  }

  @Override
  public byte[] suffix(final long n, final NumeralType numType) {
    return numType != ORDINAL ? EMPTY : n == 1 ? ER : E;
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Creates a word character sequence for the specified number.
   * @param tb token builder
   * @param n number to be formatted
   * @param numType numeral type
   * @param suffix ordinal suffix (can be {@code null})
   * @param last word appears last
   */
  private static void word(final TokenBuilder tb, final long n, final NumeralType numType,
      final byte[] suffix, final boolean last) {
    if(n == 0 && !tb.isEmpty()) {
      if(numType == ORDINAL) tb.add(IEME).add(suffix);
    } else if(n < 20) {
      addOrdinal(numType, suffix, tb.add(WORDS[(int) n]));
    } else if(n < 60) {
      final int r = (int) (n % 10);
      tb.add(WORDS20[(int) n / 10]);
      if(r == 1) tb.add(" et un");
      else if(r > 1) tb.add('-').add(WORDS[r]);
      addOrdinal(numType, suffix, tb);
    } else if(n < 80) {
      tb.add(WORDS20[6]);
      final int r = (int) (n % 20);
      if(r == 1) tb.add(" et un");
      else if(r == 11) tb.add(" et onze");
      else if(r > 1) tb.add('-').add(WORDS[r]);
      addOrdinal(numType, suffix, tb);
    } else if(n < 100) {
      tb.add(WORDS20[8]);
      final int r = (int) (n % 20);
      if(r > 0) tb.add('-').add(WORDS[r]);
      else if(numType != ORDINAL && last) tb.add(S);
      addOrdinal(numType, suffix, tb);
    } else if(n < 1000) {
      if(n >= 200) {
        word(tb, n / 100, NUMBERING, null, false);
        tb.add(' ');
      }
      tb.add("cent");
      final int r = (int) (n % 100);
      if(r != 0) tb.add(' ');
      else if(n > 100 && numType != ORDINAL && last) tb.add(S);
      word(tb, n % 100, numType, suffix, last);
    } else if(n < 1000000) {
      if(n >= 2000) {
        word(tb, n / 1000, NUMBERING, null, false);
        tb.add(' ');
      }
      final int r = (int) (n % 1000);
      tb.add(numType == ORDINAL && r == 0 ? "mill" : "mille");
      if(r != 0) tb.add(' ');

      word(tb, n % 1000, numType, suffix, last);
    } else {
      final int w = unit(n);
      final long f = UNITS1000000[w];

      final long i = n / f, r = n % f;
      // the unit itself becomes ordinal for round values: millionième, deux millionième
      final boolean ordinal = numType == ORDINAL && r == 0;
      if(i != 1) {
        // plural forms of cent and vingt only before a noun: deux cents millions
        word(tb, i, NUMBERING, null, !ordinal);
        tb.add(' ');
      } else if(!ordinal) {
        tb.add("un ");
      }
      tb.add(WORDS1000000[w]);
      if(ordinal) tb.add(IEME).add(suffix);
      else if(i > 1) tb.add(S);
      if(r != 0) {
        tb.add(' ');
        word(tb, r, numType, suffix, last);
      }
    }
  }

  /**
   * Adds an ordinal suffix.
   * @param numType numeral type
   * @param suffix ordinal suffix (can be {@code null})
   * @param tb token builder
   */
  private static void addOrdinal(final NumeralType numType, final byte[] suffix,
      final TokenBuilder tb) {
    if(numType == ORDINAL) {
      final int l = tb.size() - 1;
      final int c = tb.get(l);
      if(c == 'e') tb.removeLast();
      else if(c == 'f') tb.set(l, (byte) 'v');
      else if(c == 'q') tb.add('u');
      tb.add(IEME).add(suffix);
    }
  }
}
