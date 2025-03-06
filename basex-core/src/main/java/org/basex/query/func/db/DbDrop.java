package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.up.primitives.name.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbDrop extends DbAccessFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String name = toName(arg(0), false, qc);

    checkPerm(qc, Perm.CREATE, name);
    if(!qc.context.soptions.dbExists(name)) throw DB_OPEN1_X.get(info, name);

    qc.updates().add(new DBDrop(name, qc, info), qc);
    return Empty.VALUE;
  }
}
