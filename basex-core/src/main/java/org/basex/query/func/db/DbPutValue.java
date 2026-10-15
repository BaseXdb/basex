package org.basex.query.func.db;

import org.basex.data.*;
import org.basex.index.resource.*;
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
public final class DbPutValue extends DbNew {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Data data = toData(qc);
    final Value input = arg(1).value(qc);
    final String path = toDbPath(arg(2), qc);
    putFile(data, path, ResourceType.VALUE, new DBPut(data, input.shrink(qc), path, info),
        toEmptyMap(arg(3), qc), qc);
    return Empty.VALUE;
  }
}
