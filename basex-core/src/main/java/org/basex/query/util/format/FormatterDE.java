package org.basex.query.util.format;

import static org.basex.util.Token.*;

import org.basex.util.*;

/**
 * German language formatter. Can be instantiated via {@link Formatter#get}.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
final class FormatterDE extends Formatter {
  /** Written numbers (1-20). */
  private static final byte[][] WORDS = tokens("null", "eins", "zwei", "drei",
      "vier", "f\u00fcnf", "sechs", "sieben", "acht", "neun", "zehn", "elf",
      "zw\u00f6lf", "dreizehn", "vierzehn", "f\u00fcnfzehn", "sechzehn",
      "siebzehn", "achtzehn", "neunzehn");

  /** Written numbers (20-100). */
  private static final byte[][] WORDS20 = tokens("", "", "zwanzig", "drei\u00dfig",
      "vierzig", "f\u00fcnfzig", "sechzig", "siebzig", "achtzig", "neunzig");

  /** Written numbers (1000000, ...). */
  private static final byte[][] WORDS1000000 = tokens(
      "Million", "Milliarde", "Billion", "Billiarde", "Trillion");

  /** Units (100, 1000, ...). */
  private static final long[] UNITS100 = {
    1000000, 1000000000, 1000000000000L, 1000000000000000L, 1000000000000000000L };

  /** Ordinal Numbers (1-20). */
  private static final byte[][] ORDINALS = tokens("nullt", "erst", "zweit",
      "dritt", "viert", "f\u00fcnft", "sechst", "siebt", "acht", "neunt",
      "zehnt", "elft", "zw\u00f6lft", "dreizehnt", "vierzehnt", "f\u00fcnfzehnt",
      "sechzehnt", "siebzehnt", "achtzehnt", "neunzehnt");

  /** Days. */
  private static final byte[][][] DAYS = {
    tokens("Mo", "Mon", "Montag"),
    tokens("Di", "Die", "Dienstag"),
    tokens("Mi", "Mit", "Mittwoch"),
    tokens("Do", "Don", "Donnerstag"),
    tokens("Fr", "Fre", "Freitag"),
    tokens("Sa", "Sam", "Samstag"),
    tokens("So", "Son", "Sonntag")
  };

  /** Months. */
  private static final byte[][] MONTHS = tokens(
      "Januar", "Februar", "M\u00e4rz", "April", "Mai", "Juni", "Juli",
      "August", "September", "Oktober", "November", "Dezember");

  /** AM/PM Markers. */
  private static final byte[][] AMPM = tokens("a.m.", "p.m.");

  /** Token: und. */
  private static final byte[] UND = token("und");
  /** Token: ein. */
  private static final byte[] EIN = token("ein");
  /** Token: eine. */
  private static final byte[] EINE = token("eine");
  /** Token: e. */
  private static final byte[] E = token("e");
  /** Token: hundert. */
  private static final byte[] HUNDERT = token("hundert");
  /** Token: tausend. */
  private static final byte[] TAUSEND = token("tausend");

  /** Eras: BC, AD. */
  private static final byte[][] ERAS = tokens("v. Chr.", "n. Chr.");

  @Override
  public byte[] word(final long n, final boolean ordinal, final byte[] suffix) {
    final TokenBuilder tb = new TokenBuilder();
    word(tb, n, ordinal, suffix);
    if(!tb.isEmpty()) tb.set(0, (byte) uc(tb.get(0)));
    return tb.finish();
  }

  @Override
  public byte[] suffix(final long n, final boolean ordinal) {
    return ordinal ? E : EMPTY;
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
   * @param n number to be formatted
   * @param ordinal ordinal flag
   * @param suffix suffix (can be {@code null} or empty)
   */
  private static void word(final TokenBuilder tb, final long n, final boolean ordinal,
      final byte[] suffix) {
    if(n == 0 && !tb.isEmpty()) {
      if(ordinal) tb.add("st").add(suffix == null ? E : suffix);
    } else if(n < 20) {
      if(!ordinal) tb.add(WORDS[(int) n]);
      else tb.add(ORDINALS[(int) n]).add(suffix == null ? E : suffix);
    } else if(n < 100) {
      final int r = (int) (n % 10);
      if(r != 0) tb.add(r == 1 ? EIN : WORDS[r]).add(UND);
      tb.add(WORDS20[(int) n / 10]);
      if(ordinal) tb.add("st").add(suffix == null ? E : suffix);
    } else if(n < 1000) {
      if(n < 200) tb.add(EIN);
      else word(tb, n / 100, false, null);
      tb.add(HUNDERT);
      word(tb, n % 100, ordinal, suffix);
    } else if(n < 1000000) {
      final long m = n % 100000;
      if(m >= 1000 && m < 2000) {
        word(tb, n / 1000, false, null);
        tb.delete(tb.size() - 1, tb.size());
      } else {
        word(tb, n / 1000, false, null);
      }
      tb.add(TAUSEND);
      word(tb, n % 1000, ordinal, suffix);
    } else {
      int w = WORDS1000000.length;
      while(--w > 0 && n < UNITS100[w]);
      final long f = UNITS100[w];

      final long i = n / f;
      if(i == 1) tb.add(EINE);
      else word(tb, i, false, null);
      tb.add(' ').add(WORDS1000000[w]);
      final long r = n % f;
      if(ordinal && r == 0) {
        tb.add("st").add(suffix == null ? E : suffix);
      } else if(i > 1) {
        tb.add(w % 2 == 0 ? "en" : "n");
      }
      if(r != 0) {
        tb.add(' ');
        word(tb, r, ordinal, suffix);
      }
    }
  }
}
