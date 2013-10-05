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
  /** Holds DBCreate primitives which are not associated with a database. */
  private final Map<String, DBCreate> dbCreates = new HashMap<String, DBCreate>();
  /** Temporary data reference, containing all XML fragments to be inserted. */
  MemData tmp;

  /**
   * Adds an update primitive to this context modifier.
   * @param p update primitive
   * @param ctx query context
   * @throws QueryException query exception
   */
  abstract void add(final Operation p, final QueryContext ctx) throws QueryException;

  /**
   * Adds an update primitive to this context modifier.
   * Will be called by {@link #add(Operation, QueryContext)}.
   * @param p update primitive
   * @throws QueryException query exception
   */
  final void add(final Operation p) throws QueryException {
    if(p instanceof DBCreate) {
      final DBCreate c = (DBCreate) p;
      final DBCreate o = dbCreates.get(c.name);
      if(o != null) o.merge(c);
      dbCreates.put(c.name, c);
      return;
    }

    final Data data = p.getData();
    DatabaseUpdates dbp = pendingUpdates.get(data);
    if(dbp == null) {
      dbp = new DatabaseUpdates(data);
      pendingUpdates.put(data, dbp);
    }

    // create temporary mem data instance if not available yet
    if(tmp == null) tmp = new MemData(data.meta.options);
    dbp.add(p, tmp);
  }

  /**
   * Adds all databases to be updated to the specified list.
   * @param db databases
   */
  void databases(final StringList db) {
    for(final DatabaseUpdates du : pendingUpdates.values()) {
      final Data d = du.data();
      if(!d.inMemory()) db.add(d.meta.name);
    }
    for(final DBCreate dc : dbCreates.values()) db.add(dc.name);
  }

  /**
   * Checks constraints and applies all update primitives to the databases if
   * no constraints are hurt.
   * @throws QueryException query exception
   */
  final void apply() throws QueryException {
    // checked constraints
    final Collection<DatabaseUpdates> updates = pendingUpdates.values();
    final Collection<DBCreate> creates = dbCreates.values();

    // create temporary mem data instance if not available yet
    if(tmp == null) {
      for(final DatabaseUpdates c : updates) {
        tmp = new MemData(c.data().meta.options);
        break;
      }
    }

    for(final DatabaseUpdates c : updates) c.check(tmp);
    for(final DBCreate c : creates) c.prepare(null);

    int i = 0;
    try {
      // mark disk database instances as updating
      for(final DatabaseUpdates c : updates) {
        c.startUpdate();
        i++;
      }
      // apply updates
      for(final DatabaseUpdates c : updates) c.apply();
    } finally {
      // remove write locks and updating files
      for(final DatabaseUpdates c : updates) {
        if(i-- == 0) break;
        c.finishUpdate();
      }
    }
    // create databases
    for(final DBCreate c : creates) c.apply();
  }

  /**
   * Returns the total number of update operations.
   * @return number of updates
   */
  final int size() {
    int s = 0;
    for(final DatabaseUpdates c : pendingUpdates.values()) s += c.size();
    for(final DBCreate c : dbCreates.values()) s += c.size();
    return s;
  }
}
