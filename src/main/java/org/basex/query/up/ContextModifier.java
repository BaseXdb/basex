package org.basex.query.up;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.util.list.*;

/**
 * Base class for the different context modifiers. A context modifier aggregates
 * all updates for a specific context.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public abstract class ContextModifier {
  /** Update primitives, aggregated separately for each database which is
   * referenced during a snapshot. */
  private final Map<Data, DatabaseUpdates> pendingUpdates =
    new HashMap<Data, DatabaseUpdates>();

  /**
   * Adds an update primitive to this context modifier.
   * @param p update primitive
   * @param ctx query context
   * @throws QueryException query exception
   */
  abstract void add(final Operation p, final QueryContext ctx)
    throws QueryException;

  /**
   * Adds an update primitive to this context modifier.
   * Will be called by {@link #add(Operation, QueryContext)}.
   * @param p update primitive
   * @throws QueryException query exception
   */
  final void add(final Operation p) throws QueryException {
    final Data data = p.getData();
    DatabaseUpdates dbp = pendingUpdates.get(data);
    if(dbp == null) {
      dbp = new DatabaseUpdates(data);
      pendingUpdates.put(data, dbp);
    }
    dbp.add(p);
  }

  /**
   * Returns all updated databases to the specified list.
   * @param db databases
   */
  void databases(final StringList db) {
    for(final DatabaseUpdates du : pendingUpdates.values()) {
      db.add(du.data().meta.name);
    }
    db.sort(false, true).unique();
  }

  /**
   * Checks constraints and applies all update primitives to the databases if
   * no constraints are hurt.
   * @throws QueryException query exception
   */
  final void apply() throws QueryException {
    // checked constraints
    final Collection<DatabaseUpdates> updates = pendingUpdates.values();
    for(final DatabaseUpdates c : updates) c.check();

    int i = 0;
    try {
      // mark disk database instances as updating
      for(final DatabaseUpdates c : updates) {
        c.startUpdate();
        i++;
      }

      // apply updates
      for(final DatabaseUpdates c : updates) c.apply();

    } catch(final QueryException ex) {
      throw ex;
    } finally {
      // remove write locks and updating files
      for(final DatabaseUpdates c : updates) {
        if(i-- == 0) break;
        c.finishUpdate();
      }
    }
  }

  /**
   * Returns the total number of update operations.
   * @return number of updates
   */
  final int size() {
    int s = 0;
    for(final DatabaseUpdates c : pendingUpdates.values()) s += c.size();
    return s;
  }
}
