package org.basex.query.up;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.up.primitives.NodeCopy;
import org.basex.query.up.primitives.PrimitiveType;
import org.basex.query.up.primitives.UpdatePrimitive;
import org.basex.util.IntList;
import org.basex.util.ObjectMap;

/**
 * Holds all update primitives for a specific data reference.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
final class DBPrimitives extends Primitives {
  /** Data reference. */
  private final Data d;

  /**
   * Constructor.
   * @param data data reference
   */
  DBPrimitives(final Data data) {
    d = data;
  }

  @Override
  protected void finish() {
    // get keys (pre values) and sort ascending
    final IntList il = new IntList(op.size());
    for(final int i : op.keySet()) il.add(i);
    il.sort();
    nodes = il.finish();
  }

  @Override
  protected void check() throws QueryException {
    super.check();

    int i = nodes.length - 1;
    int par = -1;
    while(i >= 0) {
      // parent has already been checked
      if(par == nodes[i]) {
        if(i > 0) i--; else return;
      }
      int pre = nodes[i];
      final int k = d.kind(pre);
      if(k == Data.ELEM) {
        findAttributeDuplicates(new int[] { pre });
      }
      if(k == Data.ATTR) {
        par = d.parent(pre, k);
        final IntList il = new IntList(1);
        while(i >= 0 && (pre = nodes[i]) > par) {
          il.add(pre);
          i--;
        }
        if(par != -1) il.add(par);
        findAttributeDuplicates(il.finish());
      } else {
        i--;
      }
    }
  }

  /**
   * Checks a node for attribute conflicts.
   * @param pres pre values of nodes to check
   * @throws QueryException query exception
   */
  private void findAttributeDuplicates(final int[] pres) throws QueryException {
    final ObjectMap<Integer> m = new ObjectMap<Integer>();
    final IntList ats = new IntList();
    for(final int pre : pres) {
      // pres consists exclusively of element and attribute nodes
      if(d.kind(pre) == Data.ATTR) {
        ats.add(pre);
        addElementChanges(m, pre);
      } else {
        addElementChanges(m, pre);
        for(int p = pre + 1; p < pre + d.attSize(pre, Data.ELEM); p++) {
          if(!ats.contains(p)) changeAttributePool(m, true, d.attName(p));
        }
      }
    }
    findDuplicates(m);
  }

  /**
   * Determines the resulting attribute set for an element node.
   * @param m map reference
   * @param pre node pre value
   */
  private void addElementChanges(final ObjectMap<Integer> m, final int pre) {
    final UpdatePrimitive[] ups = op.get(pre);
    if(ups == null) return;
    for(final UpdatePrimitive up : ups) {
      if(up == null) continue;
      changeAttributePool(m, true, up.addAtt());
      changeAttributePool(m, false, up.remAtt());
    }
  }

  /**
   * Checks constraints and applies all updates to the databases.
   */
  @Override
  protected void apply() throws QueryException {
    // apply updates backwards, starting with the highest pre value -> no id's
    // and less table alterations needed
    for(int i = nodes.length - 1; i >= 0; i--) {
      int add = 0;
      // apply all updates for current database node
      for(final UpdatePrimitive pp : op.get(nodes[i])) {
        if(pp == null) continue;

        // An 'insert before' update moves the currently updated db node
        // further down, hence increases its pre value by the number of
        // inserted nodes.
        if(pp.type() == PrimitiveType.INSERTBEFORE) {
          add = ((NodeCopy) pp).m.meta.size;
        }
        pp.apply(add);
        // operations cannot be applied to a node which has been replaced
        if(pp.type() == PrimitiveType.REPLACENODE) break;
      }
    }
    d.flush();
  }
}