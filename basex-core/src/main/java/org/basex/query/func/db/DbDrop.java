package org.basex.query.func.db;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.up.primitives.name.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class DbDrop extends DbAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String name = string(toToken(exprs[0], qc));
    if(!Databases.validName(name)) throw BXDB_NAME_X.get(info, name);
    if(!qc.context.soptions.dbExists(name)) throw BXDB_WHICH_X.get(info, name);
    qc.updates().add(new DBDrop(name, info, qc), qc);
    return null;
  }
}
