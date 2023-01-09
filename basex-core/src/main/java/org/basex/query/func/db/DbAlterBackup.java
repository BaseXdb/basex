package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.name.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class DbAlterBackup extends DbAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String name = toName(0, false, qc), newname = toName(1, false, qc);
    if(name.equals(newname)) throw DB_CONFLICT4_X.get(info, name, newname);

    final StringList backups = qc.context.databases.backups(name);
    if(backups.isEmpty()) throw DB_NOBACKUP_X.get(info, name);

    final Updates updates = qc.updates();
    for(final String backup : backups) {
      updates.add(new BackupAlter(backup, newname, qc, info), qc);
    }
    return Empty.VALUE;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return dataLock(visitor, true, 0) && dataLock(visitor, true, 1) && visitAll(visitor, exprs);
  }
}
