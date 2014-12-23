package org.basex.query.up.primitives.name;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.up.primitives.*;
import org.basex.util.*;

/**
 * Update primitive for the {@link Function#_DB_RESTORE} function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public final class DBRestore extends NameUpdate {
  /** Backup to restore. */
  private final String backup;

  /**
   * Constructor.
   * @param name database name
   * @param backup backup file
   * @param qc query context
   * @param info input info
   */
  public DBRestore(final String name, final String backup, final QueryContext qc,
      final InputInfo info) {

    super(UpdateType.DBRESTORE, name, info, qc);
    this.backup = backup;
  }

  @Override
  public void apply() throws QueryException {
    close();
    // restore backup
    try {
      Restore.restore(name, backup, qc.context.soptions, null);
    } catch(final IOException ex) {
      throw UPDBOPTERR_X.get(info, ex);
    }
  }

  @Override
  public void prepare() throws QueryException { }

  @Override
  public String operation() { return "restored"; }
}
