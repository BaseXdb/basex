package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Update primitive for the {@link Function#_DB_ALTER} function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class DBAlter extends NameUpdate {
  /** Name of the new database. */
  protected final String newName;

  /**
   * Constructor.
   * @param name database to be renamed
   * @param newName name of new database
   * @param info input info
   * @param qc query context
   */
  public DBAlter(final String name, final String newName, final InputInfo info,
      final QueryContext qc) {

    super(UpdateType.DBALTER, name, info, qc);
    this.newName = newName;
  }

  @Override
  public void apply() throws QueryException {
    close();
    close(newName, qc, info);
    if(!AlterDB.alter(name, newName, qc.context)) throw UPDBERROR.get(info, name, operation());
  }

  @Override
  public void prepare() throws QueryException { }

  @Override
  public String operation() { return "renamed"; }

  @Override
  public void databases(final StringList db) {
    super.databases(db);
    db.add(newName);
  }
}
