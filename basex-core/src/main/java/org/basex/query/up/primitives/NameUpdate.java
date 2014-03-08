package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Update operation that references databases by their name. The targeted database need not
 * be opened.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public abstract class NameUpdate extends Update implements Comparable<NameUpdate> {
  /** Name of the database. */
  protected final String name;
  /** Query context. */
  protected final QueryContext qc;

  /**
   * Constructor.
   * @param type type of this operation
   * @param name name of database
   * @param info input info
   * @param qc query context
   */
  protected NameUpdate(final UpdateType type, final String name, final InputInfo info,
      final QueryContext qc) {

    super(type, info);
    this.name = name;
    this.qc = qc;
  }

  /**
   * Prepares this operation.
   * @throws QueryException exception
   */
  public abstract void prepare() throws QueryException;

  /**
   * Applies this operation.
   * @throws QueryException exception
   */
  public abstract void apply() throws QueryException;

  @Override
  public final void merge(final Update up) throws QueryException {
    throw BXDB_ONCE.get(info, name);
  }

  /**
   * Adds all databases to be updated to the specified list.
   * @param db databases
   */
  public void databases(final StringList db) {
    db.add(name);
  }

  /**
   * Returns the name of the database.
   * @return name
   */
  public String name() {
    return name;
  }

  @Override
  public final int size() {
    return 1;
  }

  @Override
  public final int compareTo(final NameUpdate o) {
    return type.ordinal() - o.type.ordinal();
  }

  /**
   * Closes an existing database.
   * @throws QueryException query exception
   */
  public final void close() throws QueryException {
    // close data instance in query processor
    qc.resource.removeData(name);
    // check if database is stilled pinned by another process
    if(qc.context.pinned(name)) throw BXDB_OPENED.get(info, name);
  }
}
