package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.build.csv.CsvOptions.*;
import org.basex.io.*;
import org.basex.io.parse.csv.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public class FnCsvToXml extends Parse {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toTokenOrNull(arg(0), qc);
    final CsvParserOptions options = toOptions(arg(1), new CsvW3Options(), qc).finish(info,
        CsvFormat.W3_XML);
    if(value == null) return Empty.VALUE;

    try {
      return CsvConverter.get(options).convert(new IOContent(value), ii);
    } catch(final IOException ex) {
      throw CSV_ERROR_X.get(ii, ex);
    }
  }
}
