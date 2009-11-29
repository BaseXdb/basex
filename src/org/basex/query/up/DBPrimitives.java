package org.basex.query.up;

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
public final class DBPrimitives extends Primitives {
  /**
   * Constructor.
   */
  public DBPrimitives() {
    super();
  }
  
  @Override
  public void finish() {
    // get keys (pre values) and sort ascending
    final IntList il = new IntList(op.size());
    for(final int i : op.keySet()) il.add(i);
    il.sort();
    nodes = il.finish();
    finished = true;
  }

  /**
   * Checks constraints and applies all updates to the databases.
   */
  @Override
  public void apply() {
    // apply updates backwards, starting with the highest pre value -> no id's
    // and less table alterations needed
    for(int i = nodes.length - 1; i >= 0; i--) {
      final UpdatePrimitive[] pl = op.get(nodes[i]);
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
