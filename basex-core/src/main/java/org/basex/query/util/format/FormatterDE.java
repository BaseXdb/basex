package org.basex.query.util.format;

import static org.basex.query.util.format.FormatParser.NumeralType.*;
import static org.basex.util.Token.*;

import org.basex.query.util.format.FormatParser.*;
import org.basex.util.*;

/**
 * German language formatter. Can be instantiated via {@link Formatter#get}.
 *
 * @author BaseX Team, BSD License
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
  /** Stems of ordinal numbers (1000000, ...). */
  private static final byte[][] ORDINALS1000000 = tokens(
      "million", "milliard", "billion", "billiard", "trillion");

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
  /** Token: e. */
  private static final byte[] E = cpToken('e');
  /** Token: st. */
  private static final byte[] ST = token("st");
  /** Declension endings of ordinals. */
  private static final byte[][] ENDINGS = tokens("e", "em", "en", "er", "es");
  /** Ordinal suffix of digits. */
  private static final byte[] DOT = cpToken('.');
  /** Token: hundert. */
  private static final byte[] HUNDERT = token("hundert");
  /** Token: tausend. */
  private static final byte[] TAUSEND = token("tausend");

  /** Eras: BC, AD. */
  private static final byte[][] ERAS = tokens("v. Chr.", "n. Chr.");

  /**
   * Constructor.
   */
  FormatterDE() {
    super(MONTHS, DAYS, AMPM, ERAS);
  }

  @Override
  public byte[] word(final long n, final NumeralType numType, final byte[] modifier) {
    // declension endings of ordinals (others are ignored)
    final byte[] mod = modifier == null ? null : delete(modifier, '-');
    final byte[] suffix = eq(mod, ENDINGS) ? mod : E;
    final TokenBuilder tb = new TokenBuilder();
    word(tb, n, numType, suffix);
    return tb.finish();
  }

  @Override
  protected boolean ordinalWords() {
    return true;
  }

  @Override
  public byte[] suffix(final long n, final NumeralType numType) {
    return numType == ORDINAL ? DOT : EMPTY;
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Creates a word character sequence for the specified number.
   * @param tb token builder
   * @param n number to be formatted
   * @param numType numeral type
   * @param suffix ordinal suffix (can be {@code null})
   */
  private static void word(final TokenBuilder tb, final long n, final NumeralType numType,
      final byte[] suffix) {
    if(n == 0 && !tb.isEmpty()) {
      if(numType == ORDINAL) tb.add(ST).add(suffix);
    } else if(n < 20) {
      if(numType != ORDINAL) tb.add(WORDS[(int) n]);
      else tb.add(ORDINALS[(int) n]).add(suffix);
    } else if(n < 100) {
      final int r = (int) (n % 10);
      if(r != 0) tb.add(r == 1 ? EIN : WORDS[r]).add(UND);
      tb.add(WORDS20[(int) n / 10]);
      if(numType == ORDINAL) tb.add(ST).add(suffix);
    } else if(n < 1000) {
      if(n < 200) tb.add(EIN);
      else word(tb, n / 100, NUMBERING, null);
      tb.add(HUNDERT);
      word(tb, n % 100, numType, suffix);
    } else if(n < 1000000) {
      final long m = n % 100000;
      word(tb, n / 1000, NUMBERING, null);
      if(m >= 1000 && m < 2000) tb.removeLast();
      tb.add(TAUSEND);
      word(tb, n % 1000, numType, suffix);
    } else {
      final int w = unit(n);
      final long f = UNITS1000000[w];

      final long i = n / f, r = n % f;
      // ordinals of round values are compounds (einmillionste, zweimillionste)
      final boolean compound = numType == ORDINAL && r == 0;
      // trailing one: einmillionste, eine Million, einhunderteine Millionen
      if(i % 100 == 1) {
        if(i > 1) word(tb, i - 1, NUMBERING, null);
        tb.add(EIN);
        if(!compound) tb.add(E);
      } else {
        word(tb, i, NUMBERING, null);
      }
      if(compound) {
        tb.add(ORDINALS1000000[w]).add(ST).add(suffix);
      } else {
        tb.add(' ').add(WORDS1000000[w]);
        if(i > 1) tb.add(w % 2 == 0 ? "en" : "n");
      }
      if(r != 0) {
        tb.add(' ');
        word(tb, r, numType, suffix);
      }
    }
  }
}
