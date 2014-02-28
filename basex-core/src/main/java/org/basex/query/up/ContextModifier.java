package org.basex.query.up;

import static org.basex.query.util.Err.*;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.util.list.*;

/**
 * Base class for the different context modifiers. A context modifier aggregates
 * all updates for a specific context.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public abstract class ContextModifier {
  /** Update primitives, aggregated separately for each database which is
   * referenced during a snapshot. */
  private final Map<Data, DatabaseUpdates> pendingUpdates =
    new HashMap<Data, DatabaseUpdates>();
  /** Holds DBBackup operations which are applied before any other update operation. */
  private final Map<Data, DBBackup> dbBackups = new HashMap<Data, DBBackup>();
  /** Holds DBCreate operations which are not associated with a database. */
  private final Map<String, DBCreate> dbCreates = new HashMap<String, DBCreate>();
  /** Holds DBRestore operations which are applied after all other update operations. They may not
   * be associated with a database. */
  private final Map<String, DBRestore> dbRestores = new HashMap<String, DBRestore>();
  /** Temporary data reference, containing all XML fragments to be inserted. */
  private MemData tmp;

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
    if(p instanceof DBBackup) {
      final DBBackup c = (DBBackup) p;
      final DBBackup o = dbBackups.get(c.data);
      if(o != null) o.merge(c);
      dbBackups.put(c.data, c);
      return;
    }

    if(p instanceof DBCreate) {
      final DBCreate c = (DBCreate) p;
      final DBCreate o = dbCreates.get(c.dbName);
      if(o != null) o.merge(c);
      dbCreates.put(c.dbName, c);
      return;
    }

    if(p instanceof DBRestore) {
      final DBRestore c = (DBRestore) p;
      final DBRestore o = dbRestores.get(c.backupName);
      if(o != null) o.merge(c);
      dbRestores.put(c.backupName, c);
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
    for(final DBBackup ba : dbBackups.values()) db.add(ba.data.meta.name);
    for(final DBCreate cr : dbCreates.values()) db.add(cr.dbName);
    for(final DBRestore re : dbRestores.values()) db.add(re.dbName);
  }

  /**
   * Checks constraints and applies all update primitives to the databases if
   * no constraints are hurt.
   * @throws QueryException query exception
   */
  final void apply() throws QueryException {
    final Collection<DatabaseUpdates> updates = pendingUpdates.values();
    final Collection<DBBackup> backups = dbBackups.values();
    final Collection<DBCreate> creates = dbCreates.values();
    final Collection<DBRestore> restores = dbRestores.values();

    // create temporary mem data instance if not available yet (break after first operation)
    if(tmp == null) {
      for(final DatabaseUpdates c : updates) {
        tmp = new MemData(c.data().meta.options);
        break;
      }
    }

    // gather databases that have to be write-locked and check constraints
    final Set<Data> dataWriteLocks = new HashSet<Data>();
    for(final DatabaseUpdates c : updates) {
      c.check(tmp);
      dataWriteLocks.add(c.data());
    }
    for(final DBBackup b : backups) {
      b.prepare(null);
      dataWriteLocks.add(b.data);
    }
    for(final DBCreate c : creates) {
      c.prepare(null);
      try {
        dataWriteLocks.add(c.qc.resource.database(c.dbName, c.info));
      // if the database doesn't exist, we don't have to lock it
      } catch(QueryException e) { }
    }
    for(final DBRestore r : restores) {
      r.prepare(null);
      try {
        dataWriteLocks.add(r.qc.resource.database(r.dbName, r.info));
      // if the database doesn't exist, we don't have to lock it
      } catch(QueryException e) { }
    }

    // try to acquire write locks and keep track of the number of acquired locks in order to
    // release them in case of error
    int i = 0;
    try {
      for(final Data d : dataWriteLocks) {
        if(!d.startUpdate())
          throw BXDB_OPENED.get(null, d.meta.name);
        i++;
      }

      // first backup databases
      for(final DBBackup b : backups) b.apply();
      // apply update primitives and other database operations
      for(final DatabaseUpdates c : updates) c.apply();
      // create databases and lock them afterwards
      for(final DBCreate c : creates) {
        c.apply();
        // the created database should exist now, so we want to lock it - also to be able to release
        // the write lock afterwards. If we'd release the lock now, we cannot know whether the
        // lock has also been acquired by another update. We'd have to track this as well..
        final Data d = c.qc.resource.database(c.dbName, c.info);
        if(dataWriteLocks.add(d)) {
          d.startUpdate();
          i++;
        }
      }
      // restore databases
      for(final DBRestore r : restores) r.apply();

    } finally {
      // remove write locks/updating files
      for(final Data d : dataWriteLocks) {
        // in case of crash remove only the already acquired write locks
        if(i-- == 0) break;
        d.finishUpdate();
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
    for(final DBBackup b : dbBackups.values()) s += b.size();
    for(final DBCreate c : dbCreates.values()) s += c.size();
    for(final DBRestore c : dbRestores.values()) s += c.size();
    return s;
  }
}
