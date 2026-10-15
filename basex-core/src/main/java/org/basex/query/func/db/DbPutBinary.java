package org.basex.query.func.db;

import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.query.*;
import org.basex.query.up.primitives.db.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbPutBinary extends DbNew {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Data data = toData(qc);
    final Item input = arg(1).unwrappedItem(qc, info);
    final String path = toDbPath(arg(2), qc);
    final Object source = toBinarySource(input, qc);
    putFile(data, path, ResourceType.BINARY, new DBPutBinary(data, source, path, info),
        toEmptyMap(arg(3), qc), qc);
    return Empty.VALUE;
  }
}
