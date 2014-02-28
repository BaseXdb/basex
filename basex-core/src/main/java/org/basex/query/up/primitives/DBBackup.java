package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import java.io.*;

import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.util.*;

/**
 * Update primitive for the {@link Function#_DB_BACKUP} function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public class DBBackup extends BasicOperation {
  /** Query context. */
  private final QueryContext qc;

  /**
   * Constructor.
   * @param d  database reference
   * @param ii input info
   * @param c  query context
   */
  public DBBackup(final Data d, final InputInfo ii, final QueryContext c) {
    super(TYPE.DBBACKUP, d, ii);
    qc = c;
  }

  @Override
  public void apply() throws QueryException {
    final String dbname = data.meta.name;
    try {
      CreateBackup.backup(dbname, qc.context, null);
    } catch(IOException e) {
      throw UPDBOPTERR.get(info, e);
    }
  }

  @Override
  public void merge(final BasicOperation o) { }

  @Override
  public void prepare(final MemData tmp) { }

  @Override
  public int size() {
    return 1;
  }
}
