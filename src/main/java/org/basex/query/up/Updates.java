package org.basex.query.up;

import static org.basex.query.util.Err.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.basex.core.User;
import org.basex.core.Commands.CmdPerm;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.FNode;
import org.basex.query.up.primitives.Primitive;

/**
 * Holds all update operations and primitives a snapshot contains, checks
 * constraints and finally executes the updates.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class Updates {
  /** Update primitives with {@link DBNode} instances as targets. */
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
  private final Set<Data> refs;

  /**
   * Constructor.
   * @param transform update operations are triggered by a transform expression
   */
  public Updates(final boolean transform) {
    t = transform;
    refs = t ? new HashSet<Data>() : null;
  }

  /**
   * Adds a data reference to the reference list.
   * @param d data reference to add
   */
  void addDataReference(final Data d) {
    refs.add(d);
  }

  /**
   * Adds an update primitive to the corresponding primitive list. Update
   * primitives with fragments as target nodes are treated differently,
   * because they don't effect any existing databases. They may not hurt
   * any constraints however.
   * @param p primitive to add
   * @param ctx query context
   * @throws QueryException query exception
   */
  public void add(final Primitive p, final QueryContext ctx)
      throws QueryException {

    final boolean frag = p.node instanceof FNode;
    if(t && (frag || !refs.contains(((DBNode) p.node).data)))
      UPNOTCOPIED.thrw(p.input, p.node);

    if(frag && fdata == null) fdata = new MemData(ctx.context.prop);
    final Data d = frag ? fdata : ((DBNode) p.node).data;

    Primitives prim = primitives.get(d);
    if(prim == null) {
      // check permissions
      if(!t && !frag && !ctx.context.perm(User.WRITE, d.meta))
        PERMNO.thrw(p.input, CmdPerm.WRITE);

      prim = frag ? new FragPrimitives() : new DBPrimitives(d);
      primitives.put(d, prim);
    }
    prim.add(p);
  }

  /**
   * Checks constraints and applies all update primitives to the databases if
   * no constraints are hurt.
   * @param ctx query context
   * @throws QueryException query exception
   */
  public void apply(final QueryContext ctx) throws QueryException {
    // constraints are checked first. no updates are applied if any problems
    // are found
    for(final Primitives p : primitives.values()) p.check(ctx);
    for(final Primitives p : primitives.values()) p.apply(ctx);
  }

  /**
   * Returns the number of node updates.
   * @return number of updates
   */
  public int size() {
    int s = 0;
    for(final Primitives p : primitives.values()) s += p.nodes.size();
    return s;
  }
}
