package org.basex.query.up;

import static org.basex.core.Text.*;
import java.util.HashMap;
import java.util.Map;
import org.basex.core.Main;
import org.basex.core.User;
import org.basex.core.Commands.CmdPerm;
import org.basex.data.Data;
import org.basex.query.QueryContext;
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
  private final Map<Data, DBPrimitives> dbs;
  /** Update primitives which target nodes are fragments. */
  private final DBPrimitives frags;

  /**
   * Constructor.
   */
  public PendingUpdates() {
    dbs = new HashMap<Data, DBPrimitives>();
    frags = new DBPrimitives(true);
  }

  /**
   * Adds an update primitive to the corresponding primitive list.
   * @param p primitive to add
   * @param ctx query context
   * @throws QueryException query exception
   */
  public void add(final UpdatePrimitive p, final QueryContext ctx)
      throws QueryException {

    if(p.node instanceof FNode) {
      frags.add(p);
    } else if(p.node instanceof DBNode) {
      final Data d = ((DBNode) p.node).data;
      
      DBPrimitives dp = dbs.get(d);
      if(dp == null) {
        // check permissions
        User user = ctx.context.user;
        final User us = d.meta.users.get(user.name);
        if(us != null) user = us;
        if(!user.perm(User.READ))
          throw new QueryException(Main.info(PERMNO, CmdPerm.READ));

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
    // only constraints are checked for fragment primitives
    frags.apply();
    for(final Data d : dbs.keySet().toArray(new Data[dbs.size()])) {
      dbs.get(d).apply();
      d.flush();
    }
  }
}
