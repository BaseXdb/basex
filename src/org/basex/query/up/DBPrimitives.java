package org.basex.query.up;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.FNode;
import org.basex.query.up.UpdatePrimitive.Type;

/**
 * Holds all update primitives for a specific data reference.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class DBPrimitives {
  /** Atomic update operations hashed after pre value. */
  private Map<Integer, UpdatePrimitive[]> op;
  /** Target nodes of update primitives are fragments. */
  private final boolean f;

  /**
   * Constructor.
   * @param fragment target nodes are fragments
   */
  public DBPrimitives(final boolean fragment) {
    f = fragment;
    op = new HashMap<Integer, UpdatePrimitive[]>();
  }

  /**
   * Adds a primitive to a primitive list depending on its type.
   * @param p update primitive
   * @throws QueryException query exception
   */
  public void addPrimitive(final UpdatePrimitive p) throws QueryException {
    Integer i;
    if(p.node instanceof DBNode) i = ((DBNode) p.node).pre;
    // possible to use node id 'cause nodes in map belong to the same
    // database. thus there won't be any collisions between dbnodes and 
    // fragments
    else i = ((FNode) p.node).id();
    UpdatePrimitive[] l = op.get(i);
    // [LK] same node, same kind of operation - what you wanna do?
    if(l == null) {
      l = new UpdatePrimitive[Type.values().length];
      l[p.type().ordinal()] = p;
      op.put(i, l);
    } else l[p.type().ordinal()].merge(p);
  }

  /**
   * Applies all updates to the data reference.
   * @throws QueryException query exception
   */
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
    for(int i = l - 1; i >= 0; i--) {
      final UpdatePrimitive[] pl = op.get(p[i]);
      for(final UpdatePrimitive pp : pl) if(pp != null) pp.check();
      if(f) return;
      for(final UpdatePrimitive pp : pl) 
        if(pp != null && pp.node instanceof DBNode) pp.apply();
    }
  }
}
