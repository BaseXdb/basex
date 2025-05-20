package org.basex.query.func.json;

import static org.basex.query.QueryError.*;

import org.basex.build.json.JsonOptions.*;
import org.basex.query.*;
import org.basex.query.func.fn.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class JsonParse extends ParseJson {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    try {
      return parse(qc);
    } catch(final QueryException ex) {
      throw error(ex);
    }
  }

  @Override
  protected final JsonFormat format() {
    return null;
  }

  /**
   * Adapts the error code.
   * @param ex exception to be adapted
   * @return new exception
   */
  final QueryException error(final QueryException ex) {
    final QueryError error = ex.error();
    return error(ex, error == OPTION_JSON_X ? JSON_OPTIONS_X :
        ex.matches(ErrType.FOJS) ? JSON_PARSE_X : null);
  }
}
