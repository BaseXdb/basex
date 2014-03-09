package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import java.io.*;

import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.util.*;

/**
 * Update primitive for the {@link Function#_DB_RESTORE} function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public class DBRestore extends NameUpdate {
  /** Backup file to restore. */
  public final IOFile backup;

  /**
   * Constructor.
   * @param name database name
   * @param backup backup file
   * @param qc query context
   * @param info input info
   */
  public DBRestore(final String name, final IOFile backup, final QueryContext qc,
      final InputInfo info) {

    super(UpdateType.DBRESTORE, name, info, qc);
    this.backup = backup;
  }

  @Override
  public void apply() throws QueryException {
    close();
    // restore backup
    try {
      Restore.restore(name, backup, null, qc.context);
    } catch(final IOException ex) {
      throw UPDBOPTERR.get(info, ex);
    }
  }

  @Override
  public void prepare() throws QueryException { }

  @Override
  public String operation() { return "restored"; }
}
