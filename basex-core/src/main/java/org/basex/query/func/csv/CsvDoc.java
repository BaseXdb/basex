package org.basex.query.func.csv;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.fn.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class CsvDoc extends ParseCsv {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    try {
      return doc(qc, null);
    } catch(final QueryException ex) {
      throw error(ex);
    }
  }

  /**
   * Adapts the error code.
   * @param ex exception to be adapted
   * @return new exception
   */
  final QueryException error(final QueryException ex) {
    return error(ex, ex.error().toString().startsWith(ErrType.FOCV.name()) ? CSV_PARSE_X : null);
  }
}
