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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DBCopy extends NameUpdate {
  /** Names of the new databases. */
  private final StringList targets = new StringList();

  /**
   * Constructor.
   * @param name database to be copied
   * @param target name of new database
   * @param qc query context
   * @param info input info
   */
  public DBCopy(final String name, final String target, final QueryContext qc,
      final InputInfo info) {

    super(UpdateType.DBCOPY, name, qc, info);
    targets.add(target);
  }

  @Override
  public void prepare() {
  }

  @Override
  public void apply() throws QueryException {
    close();

    try {
      for(final String target : targets) {
        close(target, qc, info);
        Copy.copy(name, target, qc.context.soptions, null);
      }
    } catch(final IOException ex) {
      throw UPDBERROR_X.get(info, ex);
    }
  }

  @Override
  public void merge(final Update update) throws QueryException {
    for(final String target : ((DBCopy) update).targets) {
      if(targets.contains(target)) throw DB_CONFLICT1_X_X.get(info, target, operation());
      targets.add(target);
    }
  }

  @Override
  public void databases(final StringList db) {
    super.databases(db);
    for(final String target : targets) db.add(target);
  }

  @Override
  public String operation() {
    return "copied";
  }
}
