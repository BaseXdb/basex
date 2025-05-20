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
public final class FnCsvToXml extends ParseCsv {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return parse(qc);
  }

  @Override
  protected CsvFormat format() {
    return CsvFormat.W3_XML;
  }
}
