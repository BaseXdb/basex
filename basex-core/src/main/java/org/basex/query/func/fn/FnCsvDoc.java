package org.basex.query.func.fn;

import org.basex.build.csv.CsvOptions.*;
import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnCsvDoc extends ParseCsv {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return doc(qc, CsvFormat.W3);
  }
}
