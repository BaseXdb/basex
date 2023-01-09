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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class DbCreateBackup extends BackupFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String name = toName(0, true, qc);
    final CreateBackupOptions opts = toOptions(1, new CreateBackupOptions(), qc);
    if(!name.isEmpty() && !qc.context.soptions.dbExists(name)) throw DB_OPEN1_X.get(info, name);

    final String comment = opts.get(CreateBackupOptions.COMMENT);
    final boolean compress = opts.get(CreateBackupOptions.COMPRESS);
    qc.updates().add(new BackupCreate(name, comment, compress, qc, info), qc);
    return Empty.VALUE;
  }
}
