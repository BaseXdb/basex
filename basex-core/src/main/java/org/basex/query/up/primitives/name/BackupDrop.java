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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BackupDrop extends NameUpdate {
  /**
   * Constructor.
   * @param name name of backup file to be dropped
   * @param qc query context
   * @param info input info
   */
  public BackupDrop(final String name, final QueryContext qc, final InputInfo info) {
    super(UpdateType.BACKUPDROP, name, qc, info);
  }

  @Override
  public void prepare() {
  }

  @Override
  public void apply() throws QueryException {
    if(!DropBackup.drop(name, qc.context.soptions))
      throw UPDROPBACK_X_X.get(info, name, operation());
  }

  @Override
  public void merge(final Update update) throws QueryException {
    throw DB_CONFLICT2_X_X.get(info, name, operation());
  }

  @Override
  protected String operation() {
    return "dropped";
  }
}
