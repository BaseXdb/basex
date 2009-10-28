package org.basex.query.up;

import java.util.HashMap;
import java.util.Map;
import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.FNode;
import org.basex.query.up.primitives.UpdatePrimitive;

/**
 * Holds all update operations and primitives a snapshot contains, checks
 * constraints and finally executes them. Update primitives with fragment
 * as target node are only checked for constraints.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class PendingUpdates {
  /** Update primitives which target nodes are DBNodes. */
  private final Map<Data, DBPrimitives> dbPrimitives;
  /** Update primitives which target nodes are fragments. */
  private final DBPrimitives fragPrimitives;

  /**
   * Constructor.
   */
  public PendingUpdates() {
    dbPrimitives = new HashMap<Data, DBPrimitives>();
    fragPrimitives = new DBPrimitives(true);
  }

  /**
   * Adds an update primitive to the corresponding primitive list.
   * @param p primitive to add
   * @throws QueryException query exception
   */
  public void add(final UpdatePrimitive p) throws QueryException {
    if(p.node instanceof FNode) {
      fragPrimitives.add(p);
    } else if(p.node instanceof DBNode) {
      final Data d = ((DBNode) p.node).data;

      DBPrimitives dp = dbPrimitives.get(d);
      if(dp == null) {
        dp = new DBPrimitives(false);
        dbPrimitives.put(d, dp);
      }
      dp.add(p);
    }
  }

  /**
   * Checks constraints and applies all update primitives to the databases if
   * no constraints are hurt.
   * XQueryUP specification 3.2.2
   * @throws QueryException query exception
   */
  public void apply() throws QueryException {
    // only constraints are checked for fragment primitives
    fragPrimitives.apply();
    final DBPrimitives[] dp = new DBPrimitives[dbPrimitives.size()];
    dbPrimitives.values().toArray(dp);
    for(final DBPrimitives p : dp) p.apply();
  }
}
