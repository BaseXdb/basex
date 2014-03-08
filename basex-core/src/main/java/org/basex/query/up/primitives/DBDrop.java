package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.util.*;

/**
 * Update primitive for the {@link Function#_DB_DROP} function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public final class DBDrop extends NameUpdate {
  /**
   * Constructor.
   * @param name name of database
   * @param info input info
   * @param qc query context
   */
  public DBDrop(final String name, final InputInfo info, final QueryContext qc) {
    super(UpdateType.DBDROP, name, info, qc);
  }

  @Override
  public void prepare() { }

  @Override
  public void apply() throws QueryException {
    close();
    // check if database files can be safely removed
    if(!DropDB.drop(name, qc.context)) throw UPDBERROR.get(info, name, operation());
  }

  @Override
  public String operation() { return "dropped"; }
}
