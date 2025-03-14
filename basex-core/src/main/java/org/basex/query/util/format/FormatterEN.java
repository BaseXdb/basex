package org.basex.query.util.format;

import static org.basex.query.util.format.FormatParser.NumeralType.*;
import static org.basex.util.Token.*;

import org.basex.query.util.format.FormatParser.*;
import org.basex.util.*;

/**
 * English language formatter. Can be instantiated via {@link Formatter#get}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class FormatterEN extends Formatter {
  /** Written numbers (1-20). */
  private static final byte[][] WORDS = tokens("Zero", "One", "Two", "Three",
      "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven",
      "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
      "Seventeen", "Eighteen", "Nineteen");

  /** Written numbers (20-100). */
  private static final byte[][] WORDS10 = tokens("", "Ten", "Twenty", "Thirty",
      "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety");

  /** Written numbers (100, 1000, ...). */
  private static final byte[][] WORDS100 = tokens("Hundred", "Thousand",
      "Million", "Billion", "Trillion", "Quadrillion", "Quintillion");

  /** Units (100, 1000, ...). */
  private static final long[] UNITS100 = { 100, 1000, 1000000, 1000000000,
    1000000000000L, 1000000000000000L, 1000000000000000000L };

  /** Ordinal Numbers (1-20). */
  private static final byte[][] ORDINALS = tokens("Zeroth", "First", "Second",
      "Third", "Fourth", "Fifth", "Sixth", "Seventh", "Eighth", "Ninth",
      "Tenth", "Eleventh", "Twelfth", "Thirteenth", "Fourteenth", "Fifteenth",
      "Sixteenth", "Seventeenth", "Eighteenth", "Nineteenth");

  /** Ordinal Numbers (20-100). */
  private static final byte[][] ORDINALS10 = tokens("", "Tenth", "Twentieth",
      "Thirtieth", "Fortieth", "Fiftieth", "Sixtieth", "Seventieth",
      "Eightieth", "Ninetieth");

  /** Days. */
  private static final byte[][][] DAYS = {
    tokens("M", "Mon", "Monday"),
    tokens("Tu", "Tue", "Tues", "Tuesday"),
    tokens("W", "Wed", "Weds", "Wednesday"),
    tokens("Th", "Thu", "Thur", "Thurs", "Thursday"),
    tokens("F", "Fri", "Friday"),
    tokens("Sa", "Sat", "Saturday"),
    tokens("Su", "Sun", "Sunday")
  };

  /** Months. */
  private static final byte[][] MONTHS = tokens(
      "January", "February", "March", "April", "May", "June", "July",
      "August", "September", "October", "November", "December");

  /** AM/PM Markers. */
  private static final byte[][] AMPM = tokens("AM", "PM");
  /** Ordinal suffixes (st, nr, rd, th). */
  private static final byte[][] ORDSUFFIX = tokens("st", "nd", "rd", "th");
  /** Eras: BC, AD. */
  private static final byte[][] ERAS = tokens("BC", "AD");

  @Override
  public byte[] word(final long n, final NumeralType numType, final byte[] suffix) {
    final TokenBuilder tb = new TokenBuilder();
    word(tb, n, numType);
    return tb.finish();
  }

  @Override
  public byte[] suffix(final long n, final NumeralType numType) {
    if(numType != NumeralType.ORDINAL) return EMPTY;
    final int f = (int) (n % 10);
    return ORDSUFFIX[f > 0 && f < 4 && n % 100 / 10 != 1 ? f - 1 : 3];
  }

  @Override
  public byte[] month(final int n, final int min, final int max) {
    return format(MONTHS[n], min, max);
  }

  @Override
  public byte[] day(final int n, final int min, final int max) {
    final TokenBuilder tb = new TokenBuilder();
    final byte[][] formats = DAYS[n];
    int f = formats.length;
    while(--f > 0 && max < formats[f].length);
    tb.add(formats[f]);
    while(tb.size() < min) tb.add(' ');
    return tb.finish();
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
   * @param number number to be formatted
   * @param numType numeral type
   */
  private static void word(final TokenBuilder tb, final long number, final NumeralType numType) {
    if(number == 0 && !tb.isEmpty()) {
    } else if(number < 20) {
      tb.add((numType == ORDINAL ? ORDINALS : WORDS)[(int) number]);
    } else if(number < 100) {
      final int r = (int) (number % 10);
      if(r == 0) {
        tb.add((numType == ORDINAL ? ORDINALS10 : WORDS10)[(int) number / 10]);
      } else {
        tb.add(WORDS10[(int) number / 10]).add('-');
        tb.add((numType == ORDINAL ? ORDINALS : WORDS)[r]);
      }
    } else {
      for(int w = WORDS100.length - 1; w >= 0; w--) {
        final long f = UNITS100[w];
        if(number < f) continue;

        word(tb, number / f, NUMBERING);
        tb.add(' ').add(WORDS100[w]);
        final long r = number % f;
        if(r == 0) {
          if(numType == ORDINAL) tb.add(ORDSUFFIX[3]);
        } else {
          tb.add(' ');
        }
        word(tb, r, numType);
        break;
      }
    }
  }
}
