package org.basex.query.up.primitives.name;

import static org.basex.query.QueryError.*;

import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.up.primitives.*;
import org.basex.util.*;

/**
 * Update primitive for the {@link Function#_DB_DROP_BACKUP} function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class BackupDrop extends NameUpdate {
  /**
   * Constructor.
   * @param name name of backup file to be dropped
   * @param info input info
   * @param qc query context
   */
  public BackupDrop(final String name, final InputInfo info, final QueryContext qc) {
    super(UpdateType.BACKUPDROP, name, info, qc);
  }

  @Override
  public void merge(final Update update) throws QueryException {
    throw BXDB_ONCEBACK_X_X.get(info, name, operation());
  }

  @Override
  public void apply() throws QueryException {
    if(!DropBackup.drop(name, qc.context.soptions))
      throw UPDROPBACK_X_X.get(info, name, operation());
  }

  @Override
  public void prepare() { }

  @Override
  protected String operation() { return "dropped"; }
}
