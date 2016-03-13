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
 * @author BaseX Team 2005-16, BSD License
 * @author Lukas Kircher
 */
public final class BackupCreate extends NameUpdate {
  /**
   * Constructor.
   * @param name name of database to be backed up
   * @param info input info
   * @param qc query context
   */
  public BackupCreate(final String name, final InputInfo info, final QueryContext qc) {
    super(UpdateType.BACKUPCREATE, name, info, qc);
  }

  @Override
  public void apply() throws QueryException {
    try {
      CreateBackup.backup(name, qc.context.soptions, null);
    } catch(final IOException ex) {
      throw UPDBOPTERR_X.get(info, ex);
    }
  }

  @Override
  public void prepare() { }

  @Override
  public String operation() { return "backed up"; }
}
