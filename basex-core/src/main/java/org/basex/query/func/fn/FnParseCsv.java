package org.basex.query.func.fn;

import org.basex.build.csv.CsvOptions.*;
import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public class FnParseCsv extends ParseCsv {
  @Override
  protected final CsvFormat format() {
    return CsvFormat.W3;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return parse(qc);
  }
}
