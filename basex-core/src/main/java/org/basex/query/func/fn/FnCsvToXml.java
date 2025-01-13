package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.io.*;
import org.basex.io.parse.csv.*;
import org.basex.query.*;
import org.basex.query.func.fn.FnParseCsv.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.hash.*;

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
    final ParseCsvOptions options = toOptions(arg(1), new ParseCsvOptions(), qc);
    options.validate(ii);

    if(value == null) return Empty.VALUE;
    final CsvParserOptions copts = options.toCsvParserOptions();
    try {
      final CsvXmlConverter converter = new CsvXmlConverter(copts);
      final TokenSet names = new TokenSet();
      if(options.columnNames != null) {
        for(final Item columnName : options.columnNames) {
          final byte[] token = toZeroToken(columnName, qc);
          converter.header(names.add(token) ? token : Token.EMPTY, false);
        }
      }
      return converter.convert(new IOContent(value), ii);
    } catch(final IOException ex) {
      throw CSV_ERROR_X.get(ii, ex);
    }
  }
}
