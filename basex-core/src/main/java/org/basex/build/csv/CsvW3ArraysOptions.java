package org.basex.build.csv;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.build.csv.CsvOptions.*;
import org.basex.query.*;
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
  /** parse-csv option field-delimiter. */
  public static final StringOption FIELD_DELIMITER = new StringOption("field-delimiter", ",");
  /** parse-csv option row-delimiter. */
  public static final StringOption ROW_DELIMITER = new StringOption("row-delimiter", "\n");
  /** parse-csv option quote-character. */
  public static final StringOption QUOTE_CHARACTER = new StringOption("quote-character", "\"");
  /** parse-csv option trim-whitespace. */
  public static final BooleanOption TRIM_WHITESPACE = new BooleanOption("trim-whitespace", false);

  /**
   * Convert the options to a {@link CsvParserOptions} object.
   * @param ii input info (can be {@code null})
   * @param format resulting CSV format
   * @return the CsvParserOptions object
   * @throws QueryException query exception
   */
  public CsvParserOptions finish(final InputInfo ii, final CsvFormat format) throws QueryException {
    final IntSet delim = new IntSet();
    for(final StringOption opt : Arrays.asList(FIELD_DELIMITER, ROW_DELIMITER, QUOTE_CHARACTER)) {
      final String val = get(opt);
      if(val.codePointCount(0, val.length()) != 1)
        throw CSV_SINGLECHAR_X_X.get(ii, opt.name(), val);
      final int cp = val.codePointAt(0);
      if(!delim.add(cp)) throw CSV_DELIMITER_X.get(ii, val);
    }

    final CsvParserOptions copts = new CsvParserOptions();
    copts.set(CsvOptions.FORMAT, format);
    copts.set(CsvOptions.FIELD_DELIMITER, get(FIELD_DELIMITER));
    copts.set(CsvOptions.ROW_DELIMITER, get(ROW_DELIMITER));
    copts.set(CsvOptions.QUOTE_CHARACTER, get(QUOTE_CHARACTER));
    copts.set(CsvOptions.TRIM_WHITESPACE, get(TRIM_WHITESPACE));
    copts.set(CsvOptions.STRICT_QUOTING, true);
    return copts;
  }
}
