package org.basex.query.up.primitives.name;

import static org.basex.query.QueryError.*;

import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.up.primitives.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Update primitive for the {@link Function#_DB_ALTER} function.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DBAlter extends NameUpdate {
  /** Name of the new database. */
  private final String newName;

  /**
   * Constructor.
   * @param name database to be renamed
   * @param newName name of new database
   * @param qc query context
   * @param info input info
   */
  public DBAlter(final String name, final String newName, final QueryContext qc,
      final InputInfo info) {

    super(UpdateType.DBALTER, name, qc, info);
    this.newName = newName;
  }

  @Override
  public void prepare() {
  }

  @Override
  public void apply() throws QueryException {
    close();
    close(newName, qc, info);
    if(!AlterDB.alter(name, newName, qc.context.soptions))
      throw UPDBERROR_X_X.get(info, name, operation());
  }

  @Override
  protected String operation() {
    return "renamed";
  }

  @Override
  public void databases(final StringList db) {
    super.databases(db);
    db.add(newName);
  }
}
