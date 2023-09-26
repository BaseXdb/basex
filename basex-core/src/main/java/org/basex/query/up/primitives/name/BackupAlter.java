package org.basex.query.up.primitives.name;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.up.primitives.*;
import org.basex.util.*;

/**
 * Update primitive for the {@link Function#_DB_ALTER_BACKUP} function.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class BackupAlter extends NameUpdate {
  /** Name of the new backup. */
  private final String newname;

  /**
   * Constructor.
   * @param name backup to be renamed
   * @param newname name of new backup
   * @param qc query context
   * @param info input info (can be {@code null})
   */
  public BackupAlter(final String name, final String newname, final QueryContext qc,
      final InputInfo info) {
    super(UpdateType.BACKUPALTER, name, qc, info);
    this.newname = newname;
  }

  @Override
  public void prepare() {
  }

  @Override
  public void apply() throws QueryException {
    try {
      AlterBackup.alter(name, newname, qc.context.soptions);
    } catch(final IOException ex) {
      Util.debug(ex);
      throw UPDROPBACK_X_X.get(info, name, operation());
    }
  }

  @Override
  public void merge(final Update update) throws QueryException {
    throw DB_CONFLICT2_X_X.get(info, name, operation());
  }

  @Override
  protected String operation() {
    return "renamed";
  }
}
