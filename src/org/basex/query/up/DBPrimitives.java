package org.basex.query.up;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.FNode;
import org.basex.query.up.primitives.InsertBefore;
import org.basex.query.up.primitives.UpdatePrimitive;
import org.basex.query.up.primitives.UpdatePrimitive.Type;

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
  public void add(final UpdatePrimitive p) throws QueryException {
    Integer i;
    if(p.node instanceof DBNode) i = ((DBNode) p.node).pre;
    // possible to use node id 'cause nodes in map belong to the same
    // database. thus there won't be any collisions between dbnodes and 
    // fragments
    else i = ((FNode) p.node).id();
    UpdatePrimitive[] l = op.get(i);
    final int pos = p.type().ordinal();
    if(l == null) {
      l = new UpdatePrimitive[Type.values().length];
      l[pos] = p;
      op.put(i, l);
    } else if(l[pos] == null) l[pos] = p;
    else l[pos].merge(p);
  }

  /**
   * Checks constraints and applies all updates to the data bases.
   * @throws QueryException query exception
   */
  public void apply() throws QueryException {
    // [LK] merge text nodes
    final int l = op.size();
    final Integer[] t = new Integer[l];
    op.keySet().toArray(t);
    final int[] p = new int[l];
    for(int i = 0; i < l; i++) p[i] = t[i];
    Arrays.sort(p);
    for(int i = l - 1; i >= 0; i--) {
      final UpdatePrimitive[] pl = op.get(p[i]);
      // fragment constraints need to be checked
      for(final UpdatePrimitive pp : pl) if(pp != null) pp.check();
      if(f) return;
      int add = 0;
      for(final UpdatePrimitive pp : pl) {
        if(pp == null) continue;
        if(pp.type() == Type.INSERTBEFORE) {
          add = ((InsertBefore) pp).m.size(0, Data.DOC) - 1;
        }
        pp.apply(add);
        if(pp.type() == Type.REPLACENODE) break;
      }
    }
  }
}
