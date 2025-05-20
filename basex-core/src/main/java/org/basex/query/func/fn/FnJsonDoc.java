package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnJsonDoc extends FnParseJson {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return doc(qc);
  }
}
