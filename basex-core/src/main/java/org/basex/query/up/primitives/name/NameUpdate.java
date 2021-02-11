package org.basex.query.up.primitives.name;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Update operation that references databases by their name.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
public abstract class NameUpdate extends Update implements Comparable<NameUpdate> {
  /** Name of the database. */
  final String name;
  /** Query context. */
  final QueryContext qc;

  /**
   * Constructor.
   * @param type type of this operation
   * @param name name of database
   * @param qc query context
   * @param info input info
   */
  NameUpdate(final UpdateType type, final String name, final QueryContext qc,
      final InputInfo info) {

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

  /**
   * Returns an info string.
   * @return info string
   */
  protected abstract String operation();

  @Override
  public void merge(final Update update) throws QueryException {
    throw DB_CONFLICT1_X_X.get(info, name, operation());
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
  final void close() throws QueryException {
    close(name, qc, info);
  }

  /**
   * Closes an existing database.
   * @param name name of database
   * @param qc query context
   * @param ii input info
   * @throws QueryException query exception
   */
  static void close(final String name, final QueryContext qc, final InputInfo ii)
      throws QueryException {

    // close data instance in query processor
    qc.resources.remove(name);
    // check if database is stilled pinned by another process
    if(qc.context.pinned(name)) throw DB_LOCK1_X.get(ii, name);
  }
}
