package org.basex.query.up;

import java.util.HashMap;
import java.util.Map;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.up.primitives.PrimitiveType;
import org.basex.query.up.primitives.UpdatePrimitive;
import org.basex.util.IntList;

/**
 * Holds all update primitives for a specific data reference.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
abstract class Primitives {
  /** Atomic update operations hashed by the pre value. */
  protected final Map<Integer, UpdatePrimitive[]> op =
    new HashMap<Integer, UpdatePrimitive[]>();
  /** Pre values of the target nodes which are updated, sorted ascending. */
  protected IntList nodes;
  /** Static put counter. */
  protected static int putCount;

  /**
   * Adds a primitive to a primitive list depending on its type.
   * @param p update primitive
   * @throws QueryException query exception
   */
  protected final void add(final UpdatePrimitive p) throws QueryException {
    int i;
    if(p.node instanceof DBNode) i = ((DBNode) p.node).pre;
    // Assign negative ids to put operations (hmm..)
    else if(p.type() == PrimitiveType.PUT) i = --putCount;
    // Possible to use node id because nodes in map belong to the same
    // database. Thus there won't be any collisions between dbnodes and
    // fragments.
    else i = p.node.id();

    UpdatePrimitive[] l = op.get(i);
    final int pos = p.type().ordinal();
    if(l == null) {
      l = new UpdatePrimitive[PrimitiveType.values().length];
      l[pos] = p;
      op.put(i, l);
    } else if(l[pos] == null) {
      l[pos] = p;
    } else {
      l[pos].merge(p);
    }
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
        if(p != null) p.prepare();
      }
    }
  };

  /**
   * Checks constraints and applies all updates to the databases.
   * @throws QueryException query exception
   */
  protected abstract void apply() throws QueryException;
}
