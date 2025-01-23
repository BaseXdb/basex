package org.basex.query.func.json;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JsonParse extends JsonDoc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    try {
      return parse(qc, null);
    } catch(final QueryException ex) {
      throw error(ex);
    }
  }
}
