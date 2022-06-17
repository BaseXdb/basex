package org.basex.query.up.primitives.name;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.up.primitives.*;
import org.basex.util.*;

/**
 * Update primitive for the {@link Function#_DB_CREATE_BACKUP} function.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Lukas Kircher
 */
public final class BackupCreate extends NameUpdate {
  /** Comment string. */
  final String comment;
  /** Compress flag. */
  final boolean compress;

  /**
   * Constructor.
   * @param name name of database (empty string for general data)
   * @param comment comment
   * @param compress compress data
   * @param qc query context
   * @param info input info
   */
  public BackupCreate(final String name, final String comment, final boolean compress,
      final QueryContext qc, final InputInfo info) {
    super(UpdateType.BACKUPCREATE, name, qc, info);
    this.comment = comment;
    this.compress = compress;
  }

  @Override
  public void prepare() {
  }

  @Override
  public void apply() throws QueryException {
    try {
      CreateBackup.backup(name, comment, compress, qc.context.soptions, null);
    } catch(final IOException ex) {
      throw UPDBERROR_X.get(info, ex);
    }
  }

  @Override
  public void merge(final Update update) throws QueryException {
    throw DB_CONFLICT2_X_X.get(info, name, operation());
  }

  @Override
  public String operation() {
    return "created";
  }
}
