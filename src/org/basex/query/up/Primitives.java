package org.basex.query.up;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.FNode;

/**
 * Holds all update primitives for a specific data reference. The distinct
 * primitives are hold separately to support fast checking of update
 * constraints.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class Primitives {
  /** Atomic update operations hashed after pre value. */
  private Map<Integer, UpdatePrimitive> op;

  /**
   * Constructor.
   */
  public Primitives() {
    // [LK] only a single update operation per db node possible at the moment
    op = new HashMap<Integer, UpdatePrimitive>();
  }

  /**
   * Adds a primitive to a primitive list depending on its type.
   * @param p update primitive
   */
  public void addPrimitive(final UpdatePrimitive p) {
    if(p.node instanceof DBNode)
      op.put(((DBNode) p.node).pre, p);
    // [LK] check fragment constraints?
    if(p.node instanceof FNode)
      op.put(((FNode) p.node).id(), p);
  }

  /**
   * Applies all updates to the data reference.
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public void apply() throws QueryException {
    // [LK] sort operations after pre values of primitives, eliminate
    // unnecessary ones and apply backwards
    // [LK] trgt / rpl node must be different nodes
    // [LK] check parent of replaced node
    // [LK] check for duplicate attributes
    // [LK] merge text nodes
    final int l = op.size();
    final Integer[] t = new Integer[l];
    op.keySet().toArray(t);
    final int[] p = new int[l];
    for(int i = 0; i < l; i++) p[i] = t[i];
    Arrays.sort(p);
    for(int i = l - 1; i >= 0; i--) op.get(new Integer(p[i])).apply();
  }
}
