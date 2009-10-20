package org.basex.query.up;

import static org.basex.query.QueryText.*;
import java.util.HashMap;
import java.util.Map;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.FNode;
import org.basex.query.up.primitives.UpdatePrimitive;
import org.basex.query.util.Err;

/**
 * Holds all update operations and primitives a snapshot contains, checks
 * constraints and finally executes them. Update primtives which target node
 * is a fragment are only checked for constraints.
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
  public void addPrimitive(final UpdatePrimitive p) throws QueryException {
    if(p.node instanceof FNode) fragPrimitives.addPrimitive(p);
    else if(p.node instanceof DBNode) {
      final Data d = ((DBNode) p.node).data;
      if(d instanceof MemData) Err.or(MMUP);

      DBPrimitives dp = dbPrimitives.get(d);
      if(dp == null) {
        dp = new DBPrimitives(false);
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
    // only constraints are checked for fragment primitives
    fragPrimitives.apply();
    final DBPrimitives[] dp = new DBPrimitives[dbPrimitives.size()];
    dbPrimitives.values().toArray(dp);
    for(final DBPrimitives p : dp) p.apply();
  }
}
