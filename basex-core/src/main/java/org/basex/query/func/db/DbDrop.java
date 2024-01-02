package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.up.primitives.name.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class DbDrop extends DbAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String name = toName(arg(0), false, qc);
    if(!qc.context.soptions.dbExists(name)) throw DB_OPEN1_X.get(info, name);
    qc.updates().add(new DBDrop(name, qc, info), qc);
    return Empty.VALUE;
  }
}
