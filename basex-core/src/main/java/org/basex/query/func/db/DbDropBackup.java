package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.name.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbDropBackup extends BackupFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String name = toBackup(arg(0), qc);

    checkCreate(name, qc);
    final StringList backups = qc.context.databases.backups(name);
    if(backups.isEmpty()) throw DB_NOBACKUP_X.get(info, name);

    final Updates updates = qc.updates();
    for(final String backup : backups) {
      updates.add(new BackupDrop(backup, qc, info), qc);
    }
    return Empty.VALUE;
  }
}
