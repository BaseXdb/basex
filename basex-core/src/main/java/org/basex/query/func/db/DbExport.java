package org.basex.query.func.db;

import static org.basex.query.QueryError.*;
import java.io.*;

import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class DbExport extends DbAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = toData(qc);
    final String path = toString(arg(1), qc);
    final Item options = arg(2).item(qc, info);

    final SerializerOptions sopts = FuncOptions.serializer(options, info);
    try {
      Export.export(data, path, sopts, null);
    } catch(final IOException ex) {
      throw SER_X.get(info, ex);
    }
    return Empty.VALUE;
  }
}
