package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.build.csv.CsvOptions.*;
import org.basex.io.in.*;
import org.basex.io.parse.csv.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.util.options.*;

/**
 * CSV parse helper functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class ParseCsv extends ParseFn {
  @Override
  public final boolean hasCNS() {
    return super.hasCNS() && !format(CsvOptions.FORMAT, CsvFormat.W3);
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
}
