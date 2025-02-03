package org.basex.query.func.fn;

import org.basex.build.json.JsonOptions.*;
import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnJsonDoc extends ParseJson {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return doc(qc, JsonFormat.W3);
  }
}
