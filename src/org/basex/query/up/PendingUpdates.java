package org.basex.query.up;

import java.util.HashMap;
import java.util.Map;
import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.FNode;

/**
 * Holds all update operations and primitives a snapshot contains, checks
 * constraints and finally executes them.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class PendingUpdates {
  /** Update primitives which target nodes are DBNodes. */
  private final Map<Data, Primitives> dbPrimitives;
  /** Update primitives which target nodes are fragments. */
  private final Primitives fragPrimitives;

  /**
   * Constructor.
   */
  public PendingUpdates() {
    dbPrimitives = new HashMap<Data, Primitives>();
    fragPrimitives = new Primitives();
  }

  /**
   * Adds an update primitive to the corresponding primitive list.
   * @param p primitive to add
   */
  public void addPrimitive(final UpdatePrimitive p) {
    if(p.node instanceof FNode) fragPrimitives.addPrimitive(p);
    else if(p.node instanceof DBNode) {
      final Data d = ((DBNode) p.node).data;
      Primitives dp = dbPrimitives.get(d);
      if(dp == null) {
        dp = new Primitives();
        dbPrimitives.put(d, dp);
      }
      dp.addPrimitive(p);
    }
  }

  /**
   * Checks constraints and applies all update primitives to the databases if
   * no constraints are hurt.
   * XQueryUP specification 3.2.2
   * @throws QueryException query exception
   */
  public void applyUpdates() throws QueryException {
    // Only primitives which target nodes are database nodes are processed.
    final Primitives[] dp = new Primitives[dbPrimitives.size()];
    dbPrimitives.values().toArray(dp);
    for(final Primitives p : dp) p.apply();
  }
}
