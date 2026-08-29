package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.name.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbAlterBackup extends DbAccessFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String name = toName(arg(0), qc), newname = toName(arg(1), qc);
    if(name.equals(newname)) throw DB_CONFLICT4_X.get(info, name, newname);

    checkCreate(name, qc);
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
    return backupLock(arg(0), true, visitor) && backupLock(arg(1), true, visitor) &&
        visitAll(visitor, exprs);
  }
}
