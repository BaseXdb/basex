package org.basex.query.func.json;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.fn.*;
import org.basex.query.value.*;
import org.basex.util.*;

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
    Util.debug(ex);
    final QueryError error = ex.error();
    QueryError err = null;
    if(error == PARSE_JSON_X) err = JSON_PARSE_X_X_X;
    else if(error == DUPLICATE_JSON_X) err = JSON_DUPL_X_X_X;
    else if(error == OPTION_JSON_X) err = JSON_OPTIONS_X;
    if(err == null) return ex;
    Util.debug(ex);
    return err.get(info, ex.getLocalizedMessage());
  }
}
