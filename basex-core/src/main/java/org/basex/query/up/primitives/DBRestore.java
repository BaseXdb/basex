package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import java.io.*;

import org.basex.core.cmd.*;
import org.basex.data.*;
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
public class DBRestore extends BasicOperation {
  /** Name of the backup to restore. Can either be the database name or the complete name of a
   * backup file. */
  public final String backupName;
  /** Name of the database to restore. */
  public final String dbName;
  /** Query context. */
  public final QueryContext qc;

  /**
   * Constructor.
   * @param dbn  database name
   * @param bnm  name of backup file or database to restore
   * @param ii   input info
   * @param c    query context
   */
  public DBRestore(final String dbn, final String bnm, final InputInfo ii,
      final QueryContext c) {
    super(TYPE.DBRESTORE, null, ii);
    backupName = bnm;
    dbName = dbn;
    qc = c;
  }

  @Override
  public void apply() throws QueryException {
    /*  All checks for DBRestore are performed here, e.g. finding the latest backup file.
     *  This allows to create a backup, run some updates and restore the o.g. database state in case
     *  the update fails all within the same snapshot. */

    // close data instance in query processor
    qc.resource.removeData(dbName);
    // check if database is stilled pinned by another process
    if(qc.context.pinned(dbName)) throw BXDB_OPENED.get(info, dbName);

    // get backup file and try to restore
    IOFile backupFile = Restore.backupFile(backupName, qc.context);
    try {
      Restore.restore(backupFile, null, qc.context.globalopts);
    } catch(IOException e) {
      throw UPDBOPTERR.get(info, e);
    }
  }

  @Override
  public void merge(final BasicOperation o) throws QueryException {
    final String n = ((DBRestore) o).backupName;
    if(!backupName.equals(n))
      throw UPDBRESTOREMRG.get(info, backupName + ", " + n);
  }

  @Override
  public void prepare(final MemData tmp) { }

  @Override
  public int size() {
    return 1;
  }
}
