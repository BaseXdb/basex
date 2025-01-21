package org.basex.query.func.db;

import static org.basex.query.QueryError.*;
import java.io.*;

import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbExport extends DbAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
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
