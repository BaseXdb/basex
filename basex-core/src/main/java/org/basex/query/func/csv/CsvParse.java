package org.basex.query.func.csv;

import static org.basex.query.util.Err.*;

import java.io.*;

import org.basex.build.*;
import org.basex.io.*;
import org.basex.io.parse.csv.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class CsvParse extends CsvFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] input = toToken(exprs[0], qc);
    final CsvParserOptions opts = toOptions(1, Q_OPTIONS, new CsvParserOptions(), qc);
    try {
      final CsvConverter conv = CsvConverter.get(opts);
      conv.convert(new IOContent(input));
      return conv.finish();
    } catch(final IOException ex) {
      throw BXCS_PARSE_X.get(info, ex);
    }
  }
}
