package org.basex.query.up;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.query.up.primitives.name.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Base class for the different context modifiers. A context modifier aggregates
 * all updates for a specific context.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
abstract class ContextModifier {
  /** Update primitives, aggregated separately for each database. */
  private final Map<Data, DataUpdates> dbUpdates = new HashMap<>();
  /** Update primitives, aggregated separately for each database name. */
  private final Map<String, NameUpdates> nameUpdates = new HashMap<>();
  /** Update primitives, aggregated separately for each user name. */
  private final Map<String, UserUpdates> userUpdates = new HashMap<>();
  /** Temporary data reference, containing all XML fragments to be inserted. */
  private MemData memData;

  /**
   * Adds a data reference to list which keeps track of the nodes copied
   * within a transform expression.
   * @param data reference
   */
  abstract void addData(Data data);

  /**
   * Adds an update primitive to this context modifier.
   * @param update update primitive
   * @param qc query context
   * @throws QueryException query exception
   */
  synchronized void add(final Update update, final QueryContext qc) throws QueryException {
    if(update instanceof DataUpdate) {
      // create temporary mem data instance if not available yet
      if(memData == null) memData = new MemData(qc.context.options);
      final DataUpdate dataUp = (DataUpdate) update;
      dbUpdates.computeIfAbsent(dataUp.data(), d -> new DataUpdates(d, qc)).add(dataUp, memData);
    } else if(update instanceof NameUpdate) {
      final NameUpdate nameUp = (NameUpdate) update;
      nameUpdates.computeIfAbsent(nameUp.name(), n -> new NameUpdates()).add(nameUp);
    } else if(update instanceof UserUpdate) {
      final UserUpdate userUp = (UserUpdate) update;
      userUpdates.computeIfAbsent(userUp.name(), n -> new UserUpdates()).add(userUp);
    } else {
      throw Util.notExpected("Unknown update type: " + update);
    }
  }

  /**
   * Adds the names of all databases to be updated to the specified list.
   * Called by a single thread after query evaluation.
   * @param db databases
   */
  synchronized void databases(final StringList db) {
    for(final Data data : dbUpdates.keySet()) {
      if(!data.inMemory()) db.add(data.meta.name);
    }
    for(final NameUpdates up : nameUpdates.values()) {
      up.databases(db);
    }
  }

  /**
   * Prepares the update operations and adds all databases to be updated to the specified list.
   * Called by a single thread after query evaluation.
   * @param qc query context
   * @param datas updated data references
   * @throws QueryException query exception
   */
  final synchronized void prepare(final HashSet<Data> datas, final QueryContext qc)
      throws QueryException {

    for(final DataUpdates up : dbUpdates.values()) {
      // create temporary mem data instance if not available yet
      if(memData == null) memData = new MemData(qc.context.options);
      up.prepare(memData, qc);
      datas.add(up.data());
    }
    for(final NameUpdates up : nameUpdates.values()) up.prepare();
  }

  /**
   * Applies all updates. Called by a single thread after query evaluation.
   * @param qc query context
   * @throws QueryException query exception
   */
  final synchronized void apply(final QueryContext qc) throws QueryException {
    final Context ctx = qc.context;
    for(final UserUpdates up : userUpdates.values()) up.apply();
    if(!userUpdates.isEmpty()) ctx.users.write();

    // apply initial updates based on database names
    for(final NameUpdates up : nameUpdates.values()) up.apply(true);

    // try to acquire write locks and keep track of the number of acquired locks in order to
    // release them in case of error. write locks prevent other JVMs from accessing currently
    // updated databases, but they cannot provide perfect safety.
    final Set<Data> datas = new HashSet<>();
    try {
      for(final Data data : dbUpdates.keySet()) {
        data.startUpdate(ctx.options);
        datas.add(data);
      }
      // apply node and database update
      for(final DataUpdates up : dbUpdates.values()) {
        up.apply(qc);
      }
    } catch(final IOException ex) {
      throw DB_LOCK2_X.get(null, ex);
    } finally {
      // remove locks: in case of a crash, remove only already acquired write locks
      for(final Data data : datas) {
        data.finishUpdate(ctx.options);
      }
    }

    // apply remaining updates based on database names
    for(final NameUpdates up : nameUpdates.values()) {
      up.apply(false);
    }
  }

  /**
   * Returns the total number of update operations.
   * @return number of updates
   */
  final synchronized int size() {
    int size = 0;
    for(final DataUpdates up : dbUpdates.values()) size += up.size();
    for(final NameUpdates up : nameUpdates.values()) size += up.size();
    for(final UserUpdates up : userUpdates.values()) size += up.size();
    return size;
  }
}
