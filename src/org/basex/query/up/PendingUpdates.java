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
import org.basex.data.MemData;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.FNode;
import org.basex.query.up.primitives.UpdatePrimitive;
import org.basex.query.util.Err;

/**
 * Holds all update operations and primitives a snapshot contains, checks
 * constraints and finally executes the updates.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class PendingUpdates {
  /** Update primitives which target nodes are DBNodes. */
  private final Map<Data, Primitives> primitives =
    new HashMap<Data, Primitives>();
  /** Data dummy for fragment updates. */
  private Data fdata;

  /** The update operations are part of a transform expression. */
  private final boolean t;
  /** Holds all data references created by the copy clause of a transform
   * expression. Adding an update primitive that is declared within the modify
   * clause of this transform expression will cause a query exception
   * (XUDY0014) if the data reference of the corresponding target node is not
   * part of this set, hence the target node has not been copied.
   */
  private Set<Data> refs;

  /**
   * Constructor.
   * @param transform update operations are triggered by a transform expression
   */
  public PendingUpdates(final boolean transform) {
    t = transform;
    if(t) refs = new HashSet<Data>();
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
   * primitives with fragments as target nodes are treated differently,
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
    if(t && (frag || !refs.contains(((DBNode) p.node).data)))
      Err.or(UPNOTCOPIED, p.node);

    if(frag && fdata == null) fdata = new MemData(ctx.context.prop);
    final Data d = frag ? fdata : ((DBNode) p.node).data;

    Primitives prim = primitives.get(d);
    if(prim == null) {
      // check permissions
      if(!t && !frag && ctx.context.perm(User.WRITE, d.meta) != -1)
        throw new QueryException(Main.info(PERMNO, CmdPerm.WRITE));

      prim = frag ? new FragmentPrimitives() : new DBPrimitives(d);
      primitives.put(d, prim);
    }
    prim.add(p);
  }

  /**
   * Checks constraints and applies all update primitives to the databases if
   * no constraints are hurt.
   * XQueryUP specification 3.2.2
   * @throws QueryException query exception
   */
  public synchronized void apply() throws QueryException {
    // Constraints are checked first. No updates are applied if any problems
    // are found.
    for(final Primitives p : primitives.values()) p.check();
    for(final Primitives p : primitives.values()) p.apply();
  }
}
