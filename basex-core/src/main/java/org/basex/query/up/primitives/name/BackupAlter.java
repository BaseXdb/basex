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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BackupAlter extends NameUpdate {
  /** Name of the new backup. */
  private final String newName;

  /**
   * Constructor.
   * @param name backup to be renamed
   * @param newName name of new backup
   * @param qc query context
   * @param info input info
   */
  public BackupAlter(final String name, final String newName, final QueryContext qc,
      final InputInfo info) {
    super(UpdateType.BACKUPALTER, name, qc, info);
    this.newName = newName;
  }

  @Override
  public void prepare() {
  }

  @Override
  public void apply() throws QueryException {
    try {
      AlterBackup.alter(name, newName, qc.context.soptions);
    } catch(final IOException ex) {
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
