package org.basex.query.up.primitives.name;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.up.primitives.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Update primitive for the {@link Function#_DB_COPY} function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class DBCopy extends NameUpdate {
  /** Name of the new database. */
  private final String newName;

  /**
   * Constructor.
   * @param name database to be copied
   * @param newName name of new database
   * @param info input info
   * @param qc query context
   */
  public DBCopy(final String name, final String newName, final InputInfo info,
      final QueryContext qc) {

    super(UpdateType.DBCOPY, name, info, qc);
    this.newName = newName;
  }

  @Override
  public void apply() throws QueryException {
    close();
    close(newName, qc, info);
    try {
      Copy.copy(name, newName, qc.context.soptions, null);
    } catch(final IOException ex) {
      throw UPDBOPTERR_X.get(info, ex);
    }
  }

  @Override
  public void prepare() throws QueryException { }

  @Override
  public String operation() { return "copied"; }

  @Override
  public void databases(final StringList db) {
    super.databases(db);
    db.add(newName);
  }
}
