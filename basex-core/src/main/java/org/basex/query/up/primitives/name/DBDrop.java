package org.basex.query.up.primitives.name;

import static org.basex.query.QueryError.*;

import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.up.primitives.*;
import org.basex.util.*;

/**
 * Update primitive for the {@link Function#_DB_DROP} function.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
public final class DBDrop extends NameUpdate {
  /**
   * Constructor.
   * @param name name of database
   * @param qc query context
   * @param info input info
   */
  public DBDrop(final String name, final QueryContext qc, final InputInfo info) {
    super(UpdateType.DBDROP, name, qc, info);
  }

  @Override
  public void prepare() {
  }

  @Override
  public void apply() throws QueryException {
    close();
    // check if database files can be safely removed
    if(!DropDB.drop(name, qc.context.soptions)) throw UPDBERROR_X_X.get(info, name, operation());
  }

  @Override
  protected String operation() {
    return "dropped";
  }
}
