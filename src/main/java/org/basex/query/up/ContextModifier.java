package org.basex.query.up;

import java.util.HashMap;
import java.util.Map;

import org.basex.data.Data;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.up.primitives.UpdatePrimitive;

/**
 * Base class for the different context modifiers. A context modifier aggregates
 * all updates for a specific context.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public abstract class ContextModifier {
  /** Update primitives, aggregated separately for each database which is
   * referenced during a snapshot. */
  private Map<Data, AggregatedDatabaseUpdates> pendingUpdates;

  /**
   * Constructor.
   */
  public ContextModifier() {
    pendingUpdates = new HashMap<Data, AggregatedDatabaseUpdates>();
  }

  /**
   * Adds an update primitive to this context modifier.
   * @param p update primitive
   * @param ctx query context
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public void add(final UpdatePrimitive p, final QueryContext ctx)
  throws QueryException {
    final Data data = p.data;
    AggregatedDatabaseUpdates dbp = pendingUpdates.get(data);
    if(dbp == null) {
      dbp = new AggregatedDatabaseUpdates(data);
      pendingUpdates.put(data, dbp);
    }
    dbp.add(p);
  }

  /**
   * Checks constraints and applies all update primitives to the databases if
   * no constraints are hurt.
   * @param ctx query context
   * @throws QueryException query exception
   */
  public void applyUpdates(final QueryContext ctx) throws QueryException {
    // constraints are checked first. no updates are applied if any problems
    // are found
    for(final AggregatedDatabaseUpdates c : pendingUpdates.values()) c.check();
    for(final AggregatedDatabaseUpdates c : pendingUpdates.values())
      c.apply(ctx);
  }

  /**
   * Returns the total number of node updates.
   * @return number of updates
   */
  public int size() {
    int s = 0;
    for(final AggregatedDatabaseUpdates c : pendingUpdates.values())
      s += c.nodes.size();
    return s;
  }
}