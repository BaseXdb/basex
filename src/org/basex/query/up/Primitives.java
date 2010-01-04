package org.basex.query.up;

import static org.basex.query.QueryText.*;
import static org.basex.query.up.primitives.PrimitiveType.*;
import java.util.HashMap;
import java.util.Map;
import org.basex.query.QueryException;
import org.basex.query.up.primitives.PrimitiveType;
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
  protected final Map<Integer, UpdatePrimitive[]> op =
    new HashMap<Integer, UpdatePrimitive[]>();
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
   * Finds an update primitive of the given type in an array.
   * @param t type
   * @param up update primitives
   * @return update primitive of type t or null if up contains no element of
   * type t
   */
  protected UpdatePrimitive findPrimitive(final PrimitiveType t,
      final UpdatePrimitive[] up) {
    for(final UpdatePrimitive p : up) if(p.type() == t) return p;
    return null;
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
      op.put(nodes.get(i), shrink(op.get(nodes.get(i))));
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
   * Removes null values from given array.
   * @param up array to shrink
   * @return original array minus null value fields
   */
  private UpdatePrimitive[] shrink(final UpdatePrimitive[] up) {
    int l = 0;
    for(final UpdatePrimitive u : up) if(u != null) l++;
    final UpdatePrimitive[] t = new UpdatePrimitive[l];
    int m = 0;
    for(final UpdatePrimitive u : up) if(u != null) t[m++] = u;
    return t;
  }

  /**
   * Checks recursively if n itself or a parent node is replaced or
   * deleted.
   * @param n node
   * @return true if parent deleted or replaced
   */
  protected abstract boolean parentDeleted(final int n);

  /**
   * Checks constraints and applies all updates to the databases.
   * @throws QueryException query exception
   */
  protected abstract void apply() throws QueryException;
}
