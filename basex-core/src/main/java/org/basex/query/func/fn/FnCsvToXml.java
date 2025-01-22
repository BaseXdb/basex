package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.build.csv.CsvOptions.*;
import org.basex.io.*;
import org.basex.io.parse.csv.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
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
    final CsvParserOptions options = toOptions(arg(1), new CsvW3Options(), qc).finish(info,
        CsvFormat.W3_XML);
    if(value == null) return Empty.VALUE;

    try {
      final CsvXmlConverter converter = (CsvXmlConverter) CsvConverter.get(options);
      final TokenSet names = new TokenSet();
      final Value columns = options.get(CsvOptions.HEADER);
      if(!columns.type.instanceOf(AtomType.BOOLEAN)) {
        for(final Item columnName : columns) {
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
