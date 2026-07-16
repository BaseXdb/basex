package org.basex.build.csv;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.Types.*;

import org.basex.build.csv.CsvOptions.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.options.*;

/**
 * Options for fn:csv-to-arrays.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public class CsvW3ArraysOptions extends Options {
  /** csv-to-arrays option separator. */
  public static final StringOption SEPARATOR = new StringOption("separator", ",");
  /** csv-to-arrays option quote-character. */
  public static final StringOption QUOTE_CHARACTER = new StringOption("quote-character", "\"");
  /** csv-to-arrays option comment-marker. */
  public static final ValueOption COMMENT_MARKER =
      new ValueOption("comment-marker", STRING_ZO, Empty.VALUE);
  /** csv-to-arrays option trim-whitespace. */
  public static final BooleanOption TRIM_WHITESPACE = new BooleanOption("trim-whitespace", false);

  /**
   * Convert the options to a {@link CsvParserOptions} object.
   * @param ii input info (can be {@code null})
   * @param format resulting CSV format
   * @return the CsvParserOptions object
   * @throws QueryException query exception
   */
  public CsvParserOptions finish(final InputInfo ii, final CsvFormat format) throws QueryException {
    final IntSet chars = new IntSet();
    check(get(SEPARATOR), SEPARATOR.name(), chars, ii);
    check(get(QUOTE_CHARACTER), QUOTE_CHARACTER.name(), chars, ii);
    final Value marker = get(COMMENT_MARKER);
    String cm = "";
    if(!marker.isEmpty()) {
      cm = Token.string(((Item) marker).string(ii));
      check(cm, COMMENT_MARKER.name(), chars, ii);
    }

    final CsvParserOptions copts = new CsvParserOptions();
    copts.set(CsvOptions.FORMAT, format);
    copts.set(CsvOptions.SEPARATOR, get(SEPARATOR));
    copts.set(CsvOptions.QUOTE_CHARACTER, get(QUOTE_CHARACTER));
    copts.set(CsvOptions.COMMENT_MARKER, cm);
    copts.set(CsvOptions.TRIM_WHITESPACE, get(TRIM_WHITESPACE));
    copts.set(CsvOptions.STRICT_QUOTING, true);
    return copts;
  }

  /**
   * Checks the value of a single-character option.
   * @param value option value
   * @param name option name
   * @param chars characters that have already been assigned
   * @param ii input info (can be {@code null})
   * @throws QueryException query exception
   */
  private static void check(final String value, final String name, final IntSet chars,
      final InputInfo ii) throws QueryException {
    if(value.codePointCount(0, value.length()) != 1) throw CSV_SINGLECHAR_X_X.get(ii, name, value);
    final int cp = value.codePointAt(0);
    // newlines are reserved for delimiting rows
    if(cp == '\n') throw CSV_NEWLINE_X.get(ii, name);
    if(!chars.add(cp)) throw CSV_DELIMITER_X.get(ii, value);
  }
}
