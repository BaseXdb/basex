package org.basex.query.func.csv;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.io.*;
import org.basex.io.parse.csv.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class CsvParse extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = exprs[0].atomItem(qc, info);
    final CsvParserOptions opts = toOptions(1, new CsvParserOptions(), qc);
    if(item == Empty.VALUE) return Empty.VALUE;

    try {
      return CsvConverter.get(opts).convert(new IOContent(toToken(item)));
    } catch(final IOException ex) {
      throw CSV_PARSE_X.get(info, ex);
    }
  }
}
