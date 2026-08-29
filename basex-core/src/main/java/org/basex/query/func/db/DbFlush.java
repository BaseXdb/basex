package org.basex.query.func.db;

import org.basex.query.*;
import org.basex.query.up.primitives.db.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbFlush extends DbAccessFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    qc.updates().add(new DBFlush(toData(qc), qc, info), qc);
    return Empty.VALUE;
  }
}
