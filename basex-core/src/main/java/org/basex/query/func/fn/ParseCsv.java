package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.build.csv.CsvOptions.*;
import org.basex.io.in.*;
import org.basex.io.parse.csv.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * CSV parse helper functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class ParseCsv extends ParseFn {
  /**
   * Returns the default conversion format.
   * @return format
   */
  protected abstract CsvFormat format();

  @Override
  protected final Options options(final QueryContext qc) throws QueryException {
    final CsvFormat format = format();
    final Options copts = format == CsvFormat.W3 || format == CsvFormat.W3_XML ?
      new CsvW3Options() : format == CsvFormat.W3_ARRAYS ? new CsvW3ArraysOptions() :
      new CsvParserOptions();
    return toOptions(arg(1), copts, qc);
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

    // parse options
    final CsvFormat format = format();
    final Options copts = format == CsvFormat.W3 || format == CsvFormat.W3_XML ?
      new CsvW3Options() : format == CsvFormat.W3_ARRAYS ? new CsvW3ArraysOptions() :
      new CsvParserOptions();
    toOptions(arg(1), copts, qc);

    // transfer to common CSV options instance
    final CsvParserOptions cpopts;
    if(format == CsvFormat.W3 || format == CsvFormat.W3_XML || format == CsvFormat.W3_ARRAYS) {
      cpopts = ((CsvW3ArraysOptions) copts).finish(info, format);
    } else {
      cpopts = (CsvParserOptions) copts;
      final Value hdr = copts.get(CsvOptions.HEADER);
      if(hdr.size() == 1 && hdr.seqType().type.isStringOrUntyped()) {
        final Boolean b = Strings.toBoolean(string(((Item) hdr).string(null)));
        if(b != null) copts.put(CsvOptions.HEADER, Bln.get(b));
      }
    }
    if(format != null) cpopts.set(CsvOptions.FORMAT, format);

    // convert data
    final CsvConverter converter = CsvConverter.get(cpopts);
    return converter.convert(ti, "", info, qc);
  }
}
