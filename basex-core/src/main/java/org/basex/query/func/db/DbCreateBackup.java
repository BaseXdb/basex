package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.up.primitives.name.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbCreateBackup extends BackupFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String name = toBackup(arg(0), qc);
    final CreateBackupOptions options = toOptions(arg(1), new CreateBackupOptions(), qc);

    checkCreate(name, qc);
    if(!name.isEmpty() && !qc.context.soptions.dbExists(name)) throw DB_GET1_X.get(info, name);

    final String comment = options.get(CreateBackupOptions.COMMENT);
    final boolean compress = options.get(CreateBackupOptions.COMPRESS);
    qc.updates().add(new BackupCreate(name, comment, compress, qc, info), qc);
    return Empty.VALUE;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return dataLock(arg(0), false, false, visitor) && super.accept(visitor);
  }
}
