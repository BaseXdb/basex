package org.basex.query.up;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.query.up.primitives.db.*;
import org.basex.query.up.primitives.name.*;
import org.basex.query.value.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Base class for the different context modifiers. A context modifier aggregates
 * all updates for a specific context.
 *
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
abstract class ContextModifier {
  /** Update primitives, aggregated separately for each database. */
  private final Map<Data, DataUpdates> dbUpdates = new HashMap<>();
  /** Update primitives, aggregated separately for each database name. */
  private final Map<String, NameUpdates> nameUpdates = new HashMap<>();
  /** Update primitives, aggregated separately for each username. */
  private final Map<String, UserUpdates> userUpdates = new HashMap<>();
  /** Temporary data of the outermost modifier with fragments to insert (can be {@code null}). */
  private MemData memData;
  /** File paths targeted by fn:put, with the input info of the first call. */
  private final Map<String, InputInfo> putPaths = new HashMap<>();
  /** Cached outputs (can be {@code null}). */
  private ValueBuilder output;
  /** Outer context modifier of a nested modifier (can be {@code null}). */
  final ContextModifier outer;

  /**
   * Constructor.
   * @param outer outer context modifier (can be {@code null})
   */
  ContextModifier(final ContextModifier outer) {
    this.outer = outer;
  }

  /**
   * Adds a data reference to list which keeps track of the nodes copied
   * within a transform expression.
   * @param data reference
   */
  abstract void addData(Data data);

  /**
   * Creates a nested context modifier.
   * @return context modifier
   */
  abstract ContextModifier nest();

  /**
   * Adopts the update primitives, put paths and outputs of a nested context modifier.
   * @param nested nested context modifier
   * @throws QueryException query exception
   */
  synchronized void add(final ContextModifier nested) throws QueryException {
    for(final Map.Entry<String, InputInfo> entry : nested.putPaths.entrySet()) {
      addPutPath(entry.getKey(), entry.getValue());
    }
    merge(dbUpdates, nested.dbUpdates, DataUpdates::add);
    merge(nameUpdates, nested.nameUpdates, NameUpdates::add);
    merge(userUpdates, nested.userUpdates, UserUpdates::add);
    if(nested.output != null) {
      if(output == null) output = nested.output;
      else output.add(nested.output.value());
    }
  }

  /**
   * Adds the entries of a nested map to the specified map.
   * @param <K> key type
   * @param <V> value type
   * @param map target map
   * @param nested nested map
   * @param add function for adding the updates of a value to an existing value
   * @throws QueryException query exception
   */
  private static <K, V> void merge(
      final Map<K, V> map,
      final Map<K, V> nested,
      final QueryBiConsumer<V, V> add) throws QueryException {
    for(final Map.Entry<K, V> entry : nested.entrySet()) {
      final V value = map.putIfAbsent(entry.getKey(), entry.getValue());
      if(value != null) add.accept(value, entry.getValue());
    }
  }

  /**
   * Adds output.
   * @param value value to be added
   * @param info input info (can be {@code null})
   * @param qc query context
   */
  abstract void addOutput(Value value, InputInfo info, QueryContext qc);

  /**
   * Caches output.
   * @param value value to be added
   * @param qc query context
   */
  final synchronized void cacheOutput(final Value value, final QueryContext qc) {
    if(output == null) output = new ValueBuilder(qc);
    output.add(value);
  }

  /**
   * Adds the cached outputs of this and all outer context modifiers to the specified builder.
   * @param vb value builder
   * @param reset reset cache
   */
  final synchronized void output(final ValueBuilder vb, final boolean reset) {
    if(outer != null) outer.output(vb, reset);
    if(output != null) {
      final Value value = output.value();
      vb.add(value);
      if(reset) output = null;
      else output.add(value);
    }
  }

  /**
   * Registers a file path targeted by fn:put.
   * @param path file path
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  private void addPutPath(final String path, final InputInfo info) throws QueryException {
    if(putPaths.putIfAbsent(path, info) != null) throw UPURIDUP_X.get(info, path);
  }

  /**
   * Returns the temporary data instance of the outermost context modifier.
   * @param qc query context
   * @return data instance
   */
  private MemData memData(final QueryContext qc) {
    if(outer != null) return outer.memData(qc);
    if(memData == null) memData = new MemData(qc.context.sharedMeta());
    return memData;
  }

  /**
   * Adds an update primitive to this context modifier.
   * @param update update primitive
   * @param qc query context
   * @throws QueryException query exception
   */
  synchronized void add(final Update update, final QueryContext qc) throws QueryException {
    switch(update) {
      case final DataUpdate dataUp -> {
        if(dataUp instanceof final Put put) {
          for(final String path : put.paths()) addPutPath(path, put.info());
        }
        final MemData md = memData(qc);
        dbUpdates.computeIfAbsent(dataUp.data(), d -> new DataUpdates(d, qc)).add(dataUp, md);
      }
      case final NameUpdate nameUp ->
        nameUpdates.computeIfAbsent(nameUp.name(), n -> new NameUpdates()).add(nameUp);
      case final UserUpdate userUp ->
        userUpdates.computeIfAbsent(userUp.name(), n -> new UserUpdates()).add(userUp);
      case null, default -> throw Util.notExpected("Unknown update type: " + update);
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
   * @param datas updated data references
   * @param qc query context
   * @throws QueryException query exception
   */
  final synchronized void prepare(final HashSet<Data> datas, final QueryContext qc)
      throws QueryException {

    check();
    for(final DataUpdates up : dbUpdates.values()) {
      up.prepare(memData(qc), qc);
      datas.add(up.data());
    }
    for(final NameUpdates up : nameUpdates.values()) up.prepare();
  }

  /**
   * Checks if the node updates create conflicting namespace bindings.
   * @throws QueryException query exception
   */
  final synchronized void checkNamespaces() throws QueryException {
    for(final DataUpdates up : dbUpdates.values()) up.checkNamespaces();
  }

  /**
   * Checks if the update primitives can be applied.
   * @throws QueryException query exception
   */
  abstract void check() throws QueryException;

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
    for(final NameUpdates up : nameUpdates.values()) up.apply(false);
  }

  /**
   * Indicates if no update primitives have been added.
   * @return result of check
   */
  final synchronized boolean isEmpty() {
    return dbUpdates.isEmpty() && nameUpdates.isEmpty() && userUpdates.isEmpty();
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
