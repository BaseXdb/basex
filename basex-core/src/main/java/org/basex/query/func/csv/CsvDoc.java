package org.basex.query.func.csv;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CsvDoc extends CsvParse {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    try {
      return doc(qc);
    } catch(final QueryException ex) {
      throw error(ex);
    }
  }
}
