package org.basex.query.up;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.util.HashMap;
import java.util.Map;
import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.QNm;
import org.basex.query.up.primitives.NodeCopy;
import org.basex.query.up.primitives.PrimitiveType;
import org.basex.query.up.primitives.UpdatePrimitive;
import org.basex.query.util.Err;
import org.basex.util.Atts;
import org.basex.util.IntList;

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
  protected void check() throws QueryException {
    super.check();

    // check attribute duplicates
    int p = nodes.size() - 1;
    int par = -1;
    while(p >= 0) {
      // parent of a previous attribute has already been checked
      if(par == nodes.get(p) && --p < 0) break;

      int pre = nodes.get(p);
      final int k = d.kind(pre);
      if(k == Data.ATTR) {
        par = d.parent(pre, k);
        final IntList il = new IntList();
        while(p >= 0 && (pre = nodes.get(p)) > par) {
          il.add(pre);
          p--;
        }
        if(par != -1) il.add(par);
        findDuplicates(il.finish());
      } else {
        if(k == Data.ELEM) findDuplicates(pre);
        p--;
      }
    }
  }

  /**
   * Checks nodes for duplicate attributes.
   * @param pres pre values of nodes to check (in descending order)
   * @throws QueryException query exception
   */
  private void findDuplicates(final int... pres) throws QueryException {
    final Map<QNm, Integer> m = new HashMap<QNm, Integer>();
    final IntList ats = new IntList();
    for(final int pre : pres) {
      // pre values consists exclusively of element and attribute nodes
      if(d.kind(pre) == Data.ATTR) {
        ats.add(pre);
        addElementChanges(m, pre);
      } else {
        addElementChanges(m, pre);
        final int ps = pre + d.attSize(pre, Data.ELEM);
        for(int p = pre + 1; p < ps; p++) {
          if(!ats.contains(p)) {
            changePool(m, true, new QNm(d.name(p, Data.ATTR)));
          }
        }
      }
    }
    for(final QNm n : m.keySet()) if(m.get(n) > 1) Err.or(UPATTDUPL, n);

    // find namespace conflicts
    final Atts at = new Atts();
    for(final QNm name : m.keySet()) {
      final byte[] an = name.pref();
      final byte[] uri = name.uri.str();
      final int ai = at.get(an);
      if(ai == -1) at.add(an, uri);
      else if(!eq(uri, at.val[ai])) Err.or(UPNSCONFL2);
    }
  }

  /**
   * Determines the resulting attribute set for an element node.
   * @param m map reference
   * @param pre node pre value
   */
  private void addElementChanges(final Map<QNm, Integer> m, final int pre) {
    final UpdatePrimitive[] ups = op.get(pre);
    if(ups == null) return;
    for(final UpdatePrimitive up : ups) {
      if(up == null) continue;
      changePool(m, true, up.addAtt());
      changePool(m, false, up.remAtt());
    }
  }

  /**
   * Checks constraints and applies all updates to the databases.
   */
  @Override
  protected void apply() throws QueryException {
    // apply updates backwards, starting with the highest pre value -> no id's
    // and less table alterations needed
    for(int i = nodes.size() - 1; i >= 0; i--) {
      int add = 0;
      // apply all updates for current database node
      for(final UpdatePrimitive pp : op.get(nodes.get(i))) {
        if(pp == null) continue;

        // An 'insert before' update moves the currently updated db node
        // further down, hence increases its pre value by the number of
        // inserted nodes.
        if(pp.type() == PrimitiveType.INSERTBEFORE) {
          add = ((NodeCopy) pp).md.meta.size;
        }
        pp.apply(add);
        // operations cannot be applied to a node which has been replaced
        if(pp.type() == PrimitiveType.REPLACENODE) break;
      }
    }
    d.flush();
  }
}