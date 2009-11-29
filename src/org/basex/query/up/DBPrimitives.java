package org.basex.query.up;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.up.primitives.InsertBefore;
import org.basex.query.up.primitives.PrimitiveType;
import org.basex.query.up.primitives.UpdatePrimitive;
import org.basex.util.IntList;
import org.basex.util.Token;

/**
 * Holds all update primitives for a specific data reference.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class DBPrimitives extends Primitives {
  /** Data reference. */
  private final Data d;
  
  /**
   * Constructor.
   * @param data data reference
   */
  public DBPrimitives(final Data data) {
    super();
    d = data;
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
  
  @Override
  public void check() throws QueryException {
    super.check();
    
    int i = nodes.length - 1;
    int par;
    int k;
    int pre;
    while(i >= 0) {
      pre = nodes[i];
      k = d.kind(pre);
      if(k == Data.ELEM) findAttributeDuplicates(new int[] {pre});
      if(k == Data.ATTR) {
        par = d.parent(pre, Data.ATTR);
        final IntList il = new IntList(1);
        while(i >= 0 && (pre = nodes[i]) > par) {
          il.add(pre);
          i--;
        }
        il.add(par);
        findAttributeDuplicates(il.finish());
      } else i--;
    }
  }
  
  /**
   * Checks a node for attribute conflicts.
   * @param pres pre values of nodes to check
   * @throws QueryException query exception
   */
  private void findAttributeDuplicates(int[] pres) throws QueryException {
    final Map<String, Integer> m = new HashMap<String, Integer>();
    final Set<Integer> ats = new HashSet<Integer>();
    for(final int pre : pres) {
      // pres consists exclusively of element and attribute nodes
      if(d.kind(pre) == Data.ATTR) {
        ats.add(pre);
        addAttributeChanges(m, pre);
      } else {
        addElementChanges(m, pre);
        int p = pre + 1;
        while(p < pre + d.attSize(pre, Data.ELEM) && !ats.contains(p)) {
          changeAttributePool(m, true, Token.string(d.attName(p++)));
        }
      }
    }
    
    findDuplicates(m);
    m.clear();
  }
  
  /**
   * Determines the resulting attribute set for an attribute node.
   * @param m map reference holding attribute pool
   * @param pre node pre value
   */
  public void addAttributeChanges(final Map<String, Integer> m, 
      final int pre) {
    for(final UpdatePrimitive up : op.get(pre)) {
      if(up == null) continue;
      changeAttributePool(m, true, up.addAtt());
      changeAttributePool(m, false, up.remAtt());
    }
  }
  
  /**
   * Determines the resulting attribute set for an element node.
   * @param m map reference
   * @param pre node pre value
   */
  public void addElementChanges(final Map<String, Integer> m, 
  final int pre)  {
    final UpdatePrimitive[] ups = op.get(pre);
    if(ups == null) return;
    for(final UpdatePrimitive up: ups) {
      if(up == null) continue;
      if(up.type() == PrimitiveType.DELETE || 
          up.type() == PrimitiveType.REPLACENODE) {
        m.clear();
        return;
      }
      changeAttributePool(m, true, up.addAtt());
      changeAttributePool(m, false, up.remAtt());
    }
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
    d.flush();
  }
}