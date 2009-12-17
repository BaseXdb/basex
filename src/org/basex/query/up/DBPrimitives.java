package org.basex.query.up;

import static org.basex.query.QueryText.*;
import static org.basex.query.up.primitives.PrimitiveType.*;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.query.up.primitives.NodeCopy;
import org.basex.query.up.primitives.PrimitiveType;
import org.basex.query.up.primitives.UpdatePrimitive;
import org.basex.query.util.Err;
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
  protected void add(final UpdatePrimitive p) throws QueryException {
    add(((DBNode) p.node).pre, p);
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
        checkNames(il.finish());
      } else {
        if(k == Data.ELEM) checkNames(pre);
        p--;
      }
    }
  }

  /**
   * Checks nodes for duplicate attributes.
   * @param pres pre values of nodes to check (in descending order)
   * @throws QueryException query exception
   */
  private void checkNames(final int... pres) throws QueryException {
    final NamePool pool = new NamePool();
    final IntList il = new IntList();

    for(final int pre : pres) {
      final UpdatePrimitive[] ups = op.get(pre);
      if(ups != null) {
        for(final UpdatePrimitive up : ups) {
          if(up != null) up.update(pool);
        }
      }

      // pre values consists exclusively of element and attribute nodes
      if(d.kind(pre) == Data.ATTR) {
        il.add(pre);
      } else {
        final int ps = pre + d.attSize(pre, Data.ELEM);
        for(int p = pre + 1; p < ps; p++) {
          if(!il.contains(p)) pool.add(new QNm(d.name(p, Data.ATTR)), Type.ATT);
        }
      }
    }

    // find duplicate attributes
    final QNm dup = pool.duplicate();
    if(dup != null) Err.or(UPATTDUPL, dup);

    // find namespace conflicts
    if(!pool.nsOK()) Err.or(UPNSCONFL2);
  }

  @Override
  protected void apply() throws QueryException {
    // apply updates backwards, starting with the highest pre value -> no id's
    // and less table alterations needed
    for(int i = nodes.size() - 1; i >= 0; i--) {
      final UpdatePrimitive[] upd = op.get(nodes.get(i));
      UpdatePrimitive p;
      int j = 0;
      int add = 0;
      while(j < upd.length) {
        p = upd[j++];
        final PrimitiveType t = p.type();
        p.apply(add);
        if(t == INSERTBEFORE) add = ((NodeCopy) p).md.meta.size;
        if(t == REPLACENODE) break;
      }
    }
    
    d.flush();
  }
  
  @Override
  protected boolean parentDeleted(final int n) {
    final UpdatePrimitive[] up = op.get(n);
    
    if(up != null) 
      for(final UpdatePrimitive pr : up) if(pr.type() == REPLACENODE ||
        pr.type() == DELETE) return true;

    final int p = d.parent(n, d.kind(n));
    if(p == -1) return false;
    return parentDeleted(p);
  }

  @Override
  protected int getId(final Nod n) {
    if(n == null) return -1;
    return ((DBNode) n).pre;
  }
}