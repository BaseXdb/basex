package org.basex.query.util.format;

import static org.basex.util.Token.*;

import org.basex.query.util.format.FormatParser.*;
import org.basex.util.*;

/**
 * French language formatter. Can be instantiated via {@link Formatter#get}.
 *
 * @author BaseX Team 2005-24, BSD License
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
  private static final byte[] ORDINAL = token("i\u00e8me");

  /** Units (100, 1000, ...). */
  private static final long[] UNITS100 = {
    1000000, 1000000000, 1000000000000L, 1000000000000000L, 1000000000000000000L };

  /** Days. */
  private static final byte[][] DAYS = tokens(
    "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche");
  /** Months. */
  private static final byte[][] MONTHS = tokens(
    "Janvier", "F\u00e9vrier", "Mars", "Avril", "Mai", "Juin",
    "Juillet", "Ao\u00fbt", "Septembre", "Octobre", "Novembre", "D\u00e9cembre");
  /** AM/PM Markers. */
  private static final byte[][] AMPM = tokens("a.m.", "p.m.");
  /** Eras: BC, AD. */
  private static final byte[][] ERAS = tokens("av. J.-C.", "ap. J.-C.");

  @Override
  public byte[] word(final long n, final NumeralType numType, final byte[] modifier) {
    final byte[] suffix = modifier == null || modifier[0] == '%' ? null : delete(modifier, '-');
    final TokenBuilder tb = new TokenBuilder();
    word(tb, n, numType, suffix, true);
    // create title case
    final TokenParser tp = new TokenParser(tb.next());
    for(boolean u = true; tp.more(); u = false) {
      tb.add(u ? uc(tp.next()) : lc(tp.next()));
    }
    return tb.finish();
  }

  @Override
  public byte[] suffix(final long n, final NumeralType numType) {
    return EMPTY;
  }

  @Override
  public byte[] month(final int n, final int min, final int max) {
    return format(MONTHS[n], min, max);
  }

  @Override
  public byte[] day(final int n, final int min, final int max) {
    return format(DAYS[n], min, max);
  }

  @Override
  public byte[] ampm(final boolean am) {
    return AMPM[am ? 0 : 1];
  }

  @Override
  public byte[] calendar() {
    return ERAS[1];
  }

  @Override
  public byte[] era(final long year) {
    return ERAS[year <= 0 ? 0 : 1];
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Creates a word character sequence for the specified number.
   * @param tb token builder
   * @param n number to be formatted
   * @param numType numeral type
   * @param suffix suffix
   * @param last words appears last
   */
  private static void word(final TokenBuilder tb, final long n, final NumeralType numType,
      final byte[] suffix, final boolean last) {
    if(n == 0 && !tb.isEmpty()) {
      if(numType == NumeralType.ORDINAL) tb.add(ORDINAL).add(suffix == null ? EMPTY : suffix);
    } else if(n < 20) {
      if(numType != NumeralType.ORDINAL) tb.add(WORDS[(int) n]);
      else if(n == 1) tb.add(tb.isEmpty() ? "premier" : "unième");
      else suffix(numType, suffix, tb.add(WORDS[(int) n]));
    } else if(n < 60) {
      final int r = (int) (n % 10);
      tb.add(WORDS20[(int) n / 10]);
      if(r == 1) tb.add(" et un");
      else if(r > 1) tb.add('-').add(WORDS[r]);
      suffix(numType, suffix, tb);
    } else if(n < 80) {
      tb.add(WORDS20[6]);
      final int r = (int) (n % 20);
      if(r == 1) tb.add(" et un");
      else if(r == 11) tb.add(" et onze");
      else if(r > 1) tb.add('-').add(WORDS[r]);
      suffix(numType, suffix, tb);
    } else if(n < 100) {
      tb.add(WORDS20[8]);
      final int r = (int) (n % 20);
      if(r > 0) tb.add('-').add(WORDS[r]);
      else if(numType != NumeralType.ORDINAL && last) tb.add("s");
      suffix(numType, suffix, tb);
    } else if(n < 1000) {
      if(n >= 200) {
        word(tb, n / 100, NumeralType.NUMBERING, null, false);
        tb.add(' ');
      }
      tb.add("cent");
      final int r = (int) (n % 100);
      if(r != 0) tb.add(' ');
      else if(n > 100 && numType != NumeralType.ORDINAL && last) tb.add('s');
      if(r == 1 && numType == NumeralType.ORDINAL) suffix(numType, suffix, tb.add(WORDS[1]));
      else word(tb, n % 100, numType, suffix, last);
    } else if(n < 1000000) {
      if(n >= 2000) {
        word(tb, n / 1000, NumeralType.NUMBERING, null, false);
        tb.add(' ');
      }
      final int r = (int) (n % 1000);
      tb.add(numType == NumeralType.ORDINAL && r == 0 ? "mill" : "mille");
      if(r != 0) tb.add(' ');

      word(tb, n % 1000, numType, suffix, last);
    } else {
      int w = WORDS1000000.length;
      while(--w > 0 && n < UNITS100[w]);
      final long f = UNITS100[w];

      final long i = n / f;
      if(i != 1) {
        word(tb, i, NumeralType.NUMBERING, null, true);
        tb.add(' ');
      } else if(numType != NumeralType.ORDINAL) {
        tb.add("un ");
      }
      tb.add(WORDS1000000[w]);
      final long r = n % f;
      if(numType == NumeralType.ORDINAL && r == 0) {
        tb.add(ORDINAL).add(suffix == null ? EMPTY : suffix);
      }
      else if(i > 1 && numType != NumeralType.ORDINAL) tb.add("s");
      if(r != 0) {
        tb.add(' ');
        word(tb, r, numType, suffix, last);
      }
    }
  }

  /**
   * Adds an ordinal suffix.
   * @param numType numeral type
   * @param suffix suffix
   * @param tb token builder
   */
  private static void suffix(final NumeralType numType, final byte[] suffix,
      final TokenBuilder tb) {
    if(numType == NumeralType.ORDINAL) {
      final int l = tb.size() - 1;
      final int c = tb.get(l);
      if(c == 'e') tb.delete(l, l + 1);
      else if(c == 'f') tb.set(l, (byte) 'v');
      else if(c == 'q') tb.add('u');
      tb.add(ORDINAL);
      if(suffix != null) tb.add(suffix);
    }
  }
}
