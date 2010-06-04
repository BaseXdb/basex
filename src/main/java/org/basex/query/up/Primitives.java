package org.basex.query.up;

import static org.basex.query.QueryText.*;
import static org.basex.query.up.primitives.PrimitiveType.*;
import java.util.HashMap;
import java.util.Map;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.up.primitives.Put;
import org.basex.query.up.primitives.UpdatePrimitive;
import org.basex.query.util.Err;
import org.basex.util.IntList;

/**
 * Holds all update primitives for a specific data reference.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Kircher
 */
abstract class Primitives {
  /** Atomic update operations hashed by the pre value. */
  protected final Map<Integer, NodePrimitives> op =
    new HashMap<Integer, NodePrimitives>();
  /** Pre values of the target nodes which are updated, sorted ascending. */
  protected IntList nodes;
  /** Ids of fn:put target nodes. */
  protected final IntList putIds = new IntList();

  /**
   * Adds a primitive to a primitive list depending on its type.
   * @param p update primitive
   * @throws QueryException query exception
   */
  protected abstract void add(final UpdatePrimitive p) throws QueryException;

  /**
   * Adds the primitive to the set.
   * @param i id key
   * @param p update primitive
   * @throws QueryException query exception
   */
  protected final void add(final int i, final UpdatePrimitive p)
      throws QueryException {

    if(p instanceof Put) putIds.add(i);
    NodePrimitives l = op.get(i);
    if(l == null) {
      l = new NodePrimitivesContainer();
      op.put(i, l);
    }
    l.add(p);
  }

  /**
   * Checks updates for violations.
   * @throws QueryException query exception
   */
  protected void check() throws QueryException {
    // get and sort keys (pre/id values)
    final int s = op.size();
    nodes = new IntList(s);
    for(final int i : op.keySet()) nodes.add(i);
    nodes.sort();

    for(int i = 0; i < s; i++) {
      for(final UpdatePrimitive p : op.get(nodes.get(i))) {
        p.prepare();
        // Check if the identity of all target nodes of fn:put operations is
        // still available after the execution of updates. That includes parent
        // nodes.
        if(p.type() == PUT && parentDeleted(nodes.get(i))) Err.or(UPFOEMPT, p);
      }
    }
  };

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
