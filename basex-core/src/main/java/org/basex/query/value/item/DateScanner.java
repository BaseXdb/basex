package org.basex.query.value.item;

import java.math.*;

import org.basex.util.*;

/**
 * Scanner for the lexical representations of date, time and duration values.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class DateScanner {
  /** Maximum number of digits that always fit into a long value. */
  private static final int MAX_DIGITS = 18;

  /** Input. */
  private final byte[] input;
  /** End position (exclusive). */
  private final int end;
  /** Current position. */
  private int pos;

  /** Start position of the year. */
  int yearStart;
  /** End position of the year. */
  int yearEnd;
  /** Indicates if a timezone was found. */
  boolean hasZone;
  /** Indicates if the timezone is negative. */
  boolean zoneNeg;
  /** Timezone hours. */
  int zoneHours;
  /** Timezone minutes. */
  int zoneMinutes;
  /** Indicates if a duration is negative. */
  boolean neg;
  /** Start positions of the duration components Y, M, D, H, M, S, or -1 (can be {@code null}). */
  int[] starts;
  /** End positions of the duration components (can be {@code null}). */
  int[] ends;

  /**
   * Constructor, skipping leading and trailing whitespace.
   * @param input input
   */
  DateScanner(final byte[] input) {
    int s = 0, e = input.length;
    while(s < e && Token.ws(input[s])) s++;
    while(e > s && Token.ws(input[e - 1])) e--;
    this.input = input;
    pos = s;
    end = e;
  }

  /**
   * Checks if the input has been completely consumed.
   * @return result of check
   */
  boolean done() {
    return pos == end;
  }

  /**
   * Consumes the specified character.
   * @param ch character
   * @return success flag
   */
  boolean consume(final char ch) {
    if(pos == end || input[pos] != ch) return false;
    pos++;
    return true;
  }

  /**
   * Consumes digits.
   * @return number of digits
   */
  private int digits() {
    final int s = pos;
    while(pos < end && Token.digit(input[pos])) pos++;
    return pos - s;
  }

  /**
   * Consumes two digits.
   * @return value, or {@code -1} if no two digits were found
   */
  int twoDigits() {
    if(end - pos < 2 || !Token.digit(input[pos]) || !Token.digit(input[pos + 1])) return -1;
    final int v = (input[pos] - '0') * 10 + input[pos + 1] - '0';
    pos += 2;
    return v;
  }

  /**
   * Consumes a year: an optional minus sign and four or more digits without superfluous zero.
   * @return success flag
   */
  boolean year() {
    yearStart = pos;
    consume('-');
    final int s = pos, n = digits();
    yearEnd = pos;
    return n == 4 || n > 4 && input[s] != '0';
  }

  /**
   * Consumes seconds: two digits and optional fractional digits.
   * @return seconds (can be {@code null})
   */
  BigDecimal seconds() {
    final int s = pos;
    if(twoDigits() == -1 || consume('.') && digits() == 0) return null;
    return decimal(s, pos);
  }

  /**
   * Consumes an optional timezone.
   * @return success flag
   */
  boolean zone() {
    if(pos == end) return true;
    hasZone = true;
    if(consume('Z')) return true;
    zoneNeg = consume('-');
    if(!zoneNeg && !consume('+')) return false;
    zoneHours = twoDigits();
    if(zoneHours == -1 || !consume(':')) return false;
    zoneMinutes = twoDigits();
    return zoneMinutes != -1;
  }

  /**
   * Consumes a duration.
   * @param min offset of the first accepted date component in {@code YMD}
   * @param max offset after the last accepted date component
   * @param time accept hour, minute and second components
   * @return success flag
   */
  boolean duration(final int min, final int max, final boolean time) {
    starts = new int[] { -1, -1, -1, -1, -1, -1 };
    ends = new int[6];
    neg = consume('-');
    if(!consume('P')) return false;
    final int dt = components(min, max);
    if(dt == -1) return false;
    return (time && consume('T') ? components(3, 6) > 0 : dt > 0) && done();
  }

  /**
   * Consumes duration components.
   * @param min offset of the first accepted component in {@code YMDHMS}
   * @param max offset after the last accepted component
   * @return number of components, or {@code -1} if the input is invalid
   */
  private int components(final int min, final int max) {
    int c = min - 1, found = 0;
    while(pos < end && Token.digit(input[pos])) {
      final int s = pos;
      digits();
      final boolean frac = consume('.');
      if(frac && digits() == 0 || pos == end) return -1;
      // designators must be ascending, fractional digits are only allowed for seconds
      final int n = "YMDHMS".indexOf(input[pos], c + 1);
      if(n == -1 || n >= max || frac && n != 5) return -1;
      c = n;
      starts[n] = s;
      ends[n] = pos++;
      found++;
    }
    return found;
  }

  /**
   * Returns the specified substring.
   * @param start start position
   * @param stop end position (exclusive)
   * @return string
   */
  String string(final int start, final int stop) {
    return Token.string(input, start, stop - start);
  }

  /**
   * Returns the integer value of a substring, or {@link Long#MIN_VALUE} if it does not fit.
   * @param start start position
   * @param stop end position (exclusive)
   * @return value
   */
  long integer(final int start, final int stop) {
    return Token.toLong(input, start, stop);
  }

  /**
   * Returns the decimal value of the specified substring.
   * @param start start position
   * @param stop end position (exclusive)
   * @return value
   */
  BigDecimal decimal(final int start, final int stop) {
    if(stop - start > MAX_DIGITS) return new BigDecimal(string(start, stop));
    long v = 0;
    int dot = -1;
    for(int p = start; p < stop; p++) {
      final int ch = input[p];
      if(ch == '.') dot = p;
      else v = v * 10 + ch - '0';
    }
    return BigDecimal.valueOf(v, dot == -1 ? 0 : stop - dot - 1);
  }
}
