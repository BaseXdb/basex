package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.build.csv.CsvOptions.*;
import org.basex.io.*;
import org.basex.io.parse.csv.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public class FnCsvToArrays extends Parse {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] value = toTokenOrNull(arg(0), qc);
    final CsvParserOptions options = toOptions(arg(1), new CsvW3ArraysOptions(), qc).finish(info,
        CsvFormat.W3_ARRAYS);
    if(value == null) return Empty.VALUE;

    try {
      final XQMap map = (XQMap) CsvConverter.get(options).convert(new IOContent(value), info);
      return map.get(CsvXQueryConverter.RECORDS);
    } catch(final IOException ex) {
      throw CSV_ERROR_X.get(info, ex);
    }
  }
}
