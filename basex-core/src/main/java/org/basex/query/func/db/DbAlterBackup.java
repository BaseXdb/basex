package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.core.users.*;
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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbAlterBackup extends DbAccessFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String name = toName(arg(0), false, qc), newname = toName(arg(1), false, qc);
    if(name.equals(newname)) throw DB_CONFLICT4_X.get(info, name, newname);

    checkPerm(qc, Perm.CREATE, name);
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
    return dataLock(arg(0), true, visitor) && dataLock(arg(1), true, visitor) &&
        visitAll(visitor, exprs);
  }
}
