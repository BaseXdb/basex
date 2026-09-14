package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import org.basex.build.csv.*;
import org.basex.build.csv.CsvOptions.*;
import org.basex.io.in.*;
import org.basex.io.parse.csv.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * CSV parse helper functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class ParseCsv extends ParseFn {
  /** Options of fn:csv-to-arrays. */
  private static final Map<String, Option<?>> W3_ARRAYS_OPTIONS = options(Map.of(),
      CsvOptions.SEPARATOR, CsvOptions.QUOTE_CHARACTER, CsvOptions.COMMENT_MARKER,
      CsvOptions.TRIM_WHITESPACE);
  /** Options of fn:parse-csv and fn:csv-to-xml. */
  private static final Map<String, Option<?>> W3_OPTIONS = options(W3_ARRAYS_OPTIONS,
      CsvOptions.HEADER, CsvOptions.SELECT_COLUMNS, CsvOptions.TRIM_ROWS);

  /**
   * Returns the default conversion format.
   * @return format, or {@code null}
   */
  protected abstract CsvFormat format();

  @Override
  protected final Options options(final QueryContext qc) throws QueryException {
    final XQMap map = toEmptyMap(arg(1), qc);
    final CsvParserOptions copts = new CsvParserOptions();
    copts.assign(map, qc, info);

    final CsvFormat format = format();
    if(format != null) {
      // W3 functions: restricted options, literal single characters, strict quoting
      final Map<String, Option<?>> allowed = format == CsvFormat.W3_ARRAYS ?
        W3_ARRAYS_OPTIONS : W3_OPTIONS;
      for(final Item key : map.keys()) {
        if(key instanceof QNm) continue;
        final String name = Token.string(key.string(info));
        if(!allowed.containsKey(name)) {
          throw INVALIDOPTION_X.get(info, Options.similar(name, allowed));
        }
      }
      // header strings are column names
      final Value header = map.get(Str.get(CsvOptions.HEADER.name()));
      if(!header.isEmpty()) copts.set(CsvOptions.HEADER, header);
      copts.checkW3(info);
      copts.set(CsvOptions.STRICT_QUOTING, true);
      copts.set(CsvOptions.FORMAT, format);
    }
    return copts;
  }

  @Override
  final boolean nl() {
    return true;
  }

  @Override
  final QueryError error() {
    return CSV_ERROR_X;
  }

  @Override
  final Value parse(final TextInput ti, final Options options, final QueryContext qc)
      throws QueryException, IOException {
    return CsvConverter.get((CsvParserOptions) options).convert(ti, "", info, qc);
  }

  /**
   * Creates a map with the specified options.
   * @param base options to be copied
   * @param options options to be added
   * @return map
   */
  private static Map<String, Option<?>> options(final Map<String, Option<?>> base,
      final Option<?>... options) {
    final Map<String, Option<?>> map = new HashMap<>(base);
    for(final Option<?> option : options) map.put(option.name(), option);
    return map;
  }
}
