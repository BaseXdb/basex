package org.basex.query.up;

import static org.basex.core.Text.*;
import static org.basex.query.QueryText.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.basex.core.Main;
import org.basex.core.User;
import org.basex.core.Commands.CmdPerm;
import org.basex.data.Data;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.FNode;
import org.basex.query.up.primitives.UpdatePrimitive;
import org.basex.query.util.Err;

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
  private final Map<Data, DBPrimitives> dbs;
  /** Update primitives which target nodes are fragments. */
  private final DBPrimitives frags;
  /** The update operations are part of a transform expression. */
  private final boolean t;
  /** Holds all data references created in the copy clause of a transform
   * expression. Adding an update primitive will cause a query exception
   * (XUDY0014) if the data reference of the target node is not part of this
   * set. 
   */
  private Set<Data> refs;

  /**
   * Constructor.
   * @param transform update operations are triggered by a transform expression
   */
  public PendingUpdates(final boolean transform) {
    t = transform;
    if(t) refs = new HashSet<Data>(); 
    dbs = new HashMap<Data, DBPrimitives>();
    frags = new DBPrimitives(true);
  }
  
  /**
   * Adds a data reference to the reference list.
   * @param d data reference to add
   */
  public void addDataReference(final Data d) {
    refs.add(d);
  }

  /**
   * Adds an update primitive to the corresponding primitive list. Update
   * primitives which target nodes are fragments are treated differently,
   * because they don't effect any existing databases. They may not hurt
   * any constraints however.
   * 
   * @param p primitive to add
   * @param ctx query context
   * @throws QueryException query exception
   */
  public void add(final UpdatePrimitive p, final QueryContext ctx)
      throws QueryException {

    final boolean frag = p.node instanceof FNode;
    if(t && 
        (!refs.contains(((DBNode) p.node).data) || frag)) 
      Err.or(UPWRONGTRG, p);
    
    if(frag) {
      frags.add(p);
    } else if(p.node instanceof DBNode) {
      final Data d = ((DBNode) p.node).data;
      
      DBPrimitives dp = dbs.get(d);
      if(dp == null) {
        // check permissions
        if(ctx.context.perm(User.WRITE, d) != -1)
          throw new QueryException(Main.info(PERMNO, CmdPerm.WRITE));

        dp = new DBPrimitives(false);
        dbs.put(d, dp);
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
    // for fragment primitives only constraints are checked
    frags.apply();
    for(final Data d : dbs.keySet().toArray(new Data[dbs.size()])) {
      dbs.get(d).apply();
      d.flush();
    }
  }
}
