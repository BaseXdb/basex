package org.basex.query.up;

import java.util.HashMap;
import java.util.Map;
import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.FNode;
import org.basex.query.up.primitives.InsertBefore;
import org.basex.query.up.primitives.PrimitiveType;
import org.basex.query.up.primitives.UpdatePrimitive;
import org.basex.util.IntList;

/**
 * Holds all update primitives for a specific data reference.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class DBPrimitives {
  /** Atomic update operations hashed by the pre value. */
  public Map<Integer, UpdatePrimitive[]> op;
  /** Target nodes of update primitives are fragments. */
  private final boolean f;
  /** Pre values of the target nodes which are updated, sorted ascending. */
  private int[] sortedPre;
  /** Data reference. */
  private final Data d;
  /** Primitives for this database are finished. */
  private boolean finished;

  /**
   * Constructor.
   * @param data Data reference. If data == null, all target nodes
   * are fragments.
   */
  public DBPrimitives(final Data data) {
    d = data;
    f = d == null;
    op = new HashMap<Integer, UpdatePrimitive[]>();
  }

  /**
   * Getter.
   * @return data reference
   */
  public Data data() {
    return d;
  }

  /**
   * Finishes something. Not finished yet.
   */
  private void finish() {
    // get keys (pre values) and sort ascending
    final IntList il = new IntList(op.size());
    for(final int i : op.keySet()) il.add(i);
    il.sort();
    sortedPre = il.finish();
    finished = true;
  }

  /**
   * Getter.
   * @return sorted pre values of target nodes
   */
  public int[] getNodes() {
    if(!finished) finish();
    return sortedPre;
  }

  /**
   * Adds a primitive to a primitive list depending on its type.
   * @param p update primitive
   * @throws QueryException query exception
   */
  public void add(final UpdatePrimitive p) throws QueryException {
    int i;
    if(p.node instanceof DBNode) i = ((DBNode) p.node).pre;
    // Possible to use node id cause nodes in map belong to the same
    // database. Thus there won't be any collisions between dbnodes and
    // fragments.
    else i = ((FNode) p.node).id();
    UpdatePrimitive[] l = op.get(i);
    final int pos = p.type().ordinal();
    if(l == null) {
      l = new UpdatePrimitive[PrimitiveType.values().length];
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
    // apply updates backwards, starting with the highest pre value -> no id's
    // needed and less table alterations
    for(int i = sortedPre.length - 1; i >= 0; i--) {
      final UpdatePrimitive[] pl = op.get(sortedPre[i]);
      // W3C wants us to check fragment constraints as well
      for(final UpdatePrimitive pp : pl) if(pp != null) pp.check();
      if(f) return;
      int add = 0;
      // apply all updates for current database node
      for(final UpdatePrimitive pp : pl) {
        if(pp == null) continue;
        // An 'insert before' update moves the currently updated db node
        // further down, hence increases its pre value by the number of
        // inserted nodes.
        if(pp.type() == PrimitiveType.INSERTBEFORE) {
          add = ((InsertBefore) pp).m.meta.size;
        }
        pp.apply(add);
        // operations cannot be applied to a node which has been replaced
        if(pp.type() == PrimitiveType.REPLACENODE) break;
      }
    }
  }
}