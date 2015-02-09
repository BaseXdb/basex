package org.basex.query.func.db;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.up.primitives.name.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DbRestore extends DbAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // extract database name from backup file
    final String name = string(toToken(exprs[0], qc));
    if(!Databases.validName(name)) throw BXDB_NAME_X.get(info, name);

    // find backup with or without date suffix
    final StringList backups = qc.context.databases.backups(name);
    if(backups.isEmpty()) throw BXDB_NOBACKUP_X.get(info, name);

    final String backup = backups.get(0);
    final String db = Databases.name(backup);
    qc.resources.updates().add(new DBRestore(db, backup, qc, info), qc);
    return null;
  }
}
