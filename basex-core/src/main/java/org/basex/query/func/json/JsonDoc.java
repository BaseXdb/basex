package org.basex.query.func.json;

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
public class JsonDoc extends ParseJson {
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
    final QueryError error = ex.error();
    return error(ex, error == PARSE_JSON_X || error == DUPLICATE_JSON_X ? JSON_PARSE_X :
      error == OPTION_JSON_X ? JSON_OPTIONS_X : null);
  }
}
