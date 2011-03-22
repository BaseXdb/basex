package org.basex.query.up;

import static org.basex.query.util.Err.*;
import static org.basex.query.up.primitives.PrimitiveType.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.up.primitives.Put;
import org.basex.query.up.primitives.Primitive;
import org.basex.util.IntList;
import org.basex.util.IntMap;

/**
 * Holds all update primitives for a specific data reference.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
abstract class Primitives {
  /** Atomic update operations hashed by the pre value. */
  protected final IntMap<NodePrimitives> op = new IntMap<NodePrimitives>();
  /** Pre values of the target nodes which are updated, sorted ascending. */
  protected IntList nodes = new IntList(0);
  /** Ids of fn:put target nodes. */
  protected final IntList putIds = new IntList(1);

  /**
   * Adds a primitive to a primitive list depending on its type.
   * @param p update primitive
   * @throws QueryException query exception
   */
  protected abstract void add(final Primitive p) throws QueryException;

  /**
   * Adds the primitive to the set.
   * @param id id key
   * @param up update primitive
   * @throws QueryException query exception
   */
  protected final void add(final int id, final Primitive up)
      throws QueryException {

    if(up instanceof Put) putIds.add(id);
    NodePrimitives np = op.get(id);
    if(np == null) {
      np = new NodePrimitivesContainer();
      op.add(id, np);
    }
    np.add(up);
  }

  /**
   * Checks updates for violations.
   * @throws QueryException query exception
   */
  protected final void check() throws QueryException {
    // get and sort keys (pre/id values)
    final int s = op.size();
    nodes = new IntList(s);
    for(int i = 1; i <= op.size(); i++) nodes.add(op.key(i));
    nodes.sort();

    for(int i = 0; i < s; ++i) {
      for(final Primitive p : op.get(nodes.get(i))) {
        p.prepare();
        // check if the identity of all target nodes of fn:put operations is
        // still available after the execution of updates. that includes parent
        // nodes
        if(p.type() == PUT && parentDeleted(nodes.get(i))) {
          UPFOTYPE.thrw(p.input, p);
        }
      }
    }
  }

  /**
   * Checks updates for violations.
   * @param ctx query context reference
   * @throws QueryException query exception
   */
  protected abstract void check(final QueryContext ctx) throws QueryException;

  /**
   * Checks recursively if n itself or a parent node is replaced or
   * deleted.
   * @param n node
   * @return true if parent deleted or replaced
   */
  protected abstract boolean parentDeleted(final int n);

  /**
   * Checks constraints and applies all updates to the databases.
   * @param ctx query context
   * @throws QueryException query exception
   */
  protected abstract void apply(final QueryContext ctx) throws QueryException;
}
