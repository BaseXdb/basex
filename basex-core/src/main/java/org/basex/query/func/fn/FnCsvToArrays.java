package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import org.basex.build.csv.*;
import org.basex.build.csv.CsvOptions.*;
import org.basex.io.*;
import org.basex.io.parse.csv.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Gunther Rademacher
 */
public class FnCsvToArrays extends Parse {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] value = toTokenOrNull(arg(0), qc);
    if(value == null) return Empty.VALUE;
    final IO io = new IOContent(value);
    final CsvToArraysOptions options = toOptions(arg(1), new CsvToArraysOptions(), qc);
    options.validate(info);

    final CsvParserOptions cpo = new CsvParserOptions();
    cpo.set(CsvOptions.SEPARATOR, options.get(CsvToArraysOptions.FIELD_DELIMITER));
    cpo.set(CsvOptions.ROW_DELIMITER, options.get(CsvToArraysOptions.ROW_DELIMITER));
    cpo.set(CsvOptions.QUOTE_CHARACTER, options.get(CsvToArraysOptions.QUOTE_CHARACTER));
    cpo.set(CsvOptions.TRIM_WHITSPACE, options.get(CsvToArraysOptions.TRIM_WHITESPACE));
    cpo.set(CsvOptions.FORMAT, CsvFormat.XQUERY);
    cpo.set(CsvOptions.QUOTES, true);
    cpo.set(CsvOptions.STRICT_QUOTING, true);

    try {
      final XQMap map = (XQMap) CsvConverter.get(cpo).convert(io, info);
      return map.get(CsvXQueryConverter.RECORDS);
    } catch(final IOException ex) {
      throw CSV_ERROR_X.get(info, ex);
    }
  }

  /**
   * Options for fn:parse-csv.
   */
  public static class CsvToArraysOptions extends Options {
    /** parse-csv option field-delimiter. */
    public static final StringOption FIELD_DELIMITER = new StringOption("field-delimiter", ",");
    /** parse-csv option row-delimiter. */
    public static final StringOption ROW_DELIMITER = new StringOption("row-delimiter", "\n");
    /** parse-csv option quote-character. */
    public static final StringOption QUOTE_CHARACTER = new StringOption("quote-character", "\"");
    /** parse-csv option trim-whitespace. */
    public static final BooleanOption TRIM_WHITESPACE = new BooleanOption("trim-whitespace", false);

    /**
     * Check for error conditions in the current settings.
     * @param ii input info
     * @throws QueryException query exception
     */
    public void validate(final InputInfo ii) throws QueryException {
      final IntSet delim = new IntSet();
      for(final StringOption opt : Arrays.asList(FIELD_DELIMITER, ROW_DELIMITER, QUOTE_CHARACTER)) {
        final String val = get(opt);
        if(val.codePointCount(0, val.length()) != 1)
          throw CSV_SINGLECHAR_X_X.get(ii, opt.name(), val);
        final int cp = val.codePointAt(0);
        if(!delim.add(cp)) throw CSV_DELIMITER_X.get(ii, val);
      }
    }
  }
}
