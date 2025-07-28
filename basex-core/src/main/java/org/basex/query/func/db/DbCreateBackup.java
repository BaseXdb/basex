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
public final class DbCreateBackup extends BackupFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String name = toBackup(arg(0), qc);
    final CreateBackupOptions options = toOptions(arg(1), new CreateBackupOptions(), qc);

    checkPerm(qc, Perm.CREATE, name);
    if(!name.isEmpty() && !qc.context.soptions.dbExists(name)) throw DB_GET1_X.get(info, name);

    final String comment = options.get(CreateBackupOptions.COMMENT);
    final boolean compress = options.get(CreateBackupOptions.COMPRESS);
    qc.updates().add(new BackupCreate(name, comment, compress, qc, info), qc);
    return Empty.VALUE;
  }
}
