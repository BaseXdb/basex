package org.basex.query.func.db;

import static org.basex.query.QueryError.*;
import java.io.*;

import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbExport extends DbAccessFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Data data = toData(qc);
    final String path = toString(arg(1), qc);
    final SerializerOptions options = toSerializerOptions(arg(2), qc);

    try {
      Export.export(data, path, options, null);
    } catch(final IOException ex) {
      throw SERPARAM_X.get(info, ex);
    }
    return Empty.VALUE;
  }
}
