package org.basex.query.up;

import static org.basex.query.QueryText.*;
import static org.basex.query.up.primitives.PrimitiveType.*;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.core.proc.Export;
import org.basex.data.Data;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
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
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
  protected void check(final QueryContext ctx) throws QueryException {
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
        checkNames(ctx, il.finish());
      } else {
        if(k == Data.ELEM) checkNames(ctx, pre);
        p--;
      }
    }
  }

  /**
   * Checks nodes for duplicate attributes.
   * @param ctx query context reference
   * @param pres pre values of nodes to check (in descending order)
   * @throws QueryException query exception
   */
  private void checkNames(final QueryContext ctx, final int... pres)
    throws QueryException {
    final NamePool pool = new NamePool();
    final IntList il = new IntList();

    for(final int pre : pres) {
      final UpdatePrimitive[] ups = op.get(pre);
      if(ups != null)
        for(final UpdatePrimitive up : ups) up.update(pool);

      // pre values consist exclusively of element and attribute nodes
      if(d.kind(pre) == Data.ATTR) {
        il.add(pre);
      } else {
        final int ps = pre + d.attSize(pre, Data.ELEM);
        for(int p = pre + 1; p < ps; p++) {
          final byte[] nm = d.name(p, Data.ATTR);
          // use Uri(name, uri) constructor for attributes with namespaces
          // [LK] run some more tests
          if(!il.contains(p)) pool.add(new QNm(nm, ctx), Type.ATT);
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
  protected void apply(final QueryContext ctx) throws QueryException {
    // apply updates backwards, starting with the highest pre value -> no id's
    // and less table alterations needed
    int par = -2;
    boolean check = false;
    // first is the first node of par which is updated. So 'first-1' is the
    // lowest pre value where adjacent text nodes can exist.
    int first = -1;
    for(int i = nodes.size() - 1; i >= 0; i--) {
      final int pre = nodes.get(i);
      final int parT = d.parent(pre, d.kind(pre));
      if(parT != par) {
        // Adjacent text nodes are merged. Merges can only be applied directly
        // after the update if no lower pre values are effected. This is not
        // the case for 'replace node', 'delete' and 'insert before' operations.
        if(check && parT < par) mergeTexts(par, first);
        check = false;
        par = parT;
      }
      first = pre;
      final UpdatePrimitive[] upd = op.get(pre);
      for(int j = 0, add = 0; j < upd.length; j++) {
        final UpdatePrimitive p = upd[j];
        final PrimitiveType t = p.type();
        p.apply(add);
        check = t == INSERTBEFORE || t == REPLACENODE || t == DELETE;
        if(t == INSERTBEFORE) add = ((NodeCopy) p).md.meta.size;
        if(t == REPLACENODE) break;
      }
    }
    if(check) mergeTexts(par, first);
    d.flush();

    if(d.meta.prop.is(Prop.WRITEBACK)) {
      try {
        Export.export(ctx.context, d);
      } catch(final IOException ex) {
        Err.or(UPPUTERR, d.meta.file);
      }
    }
  }

  /**
   * Merges any adjacent child text nodes for the given node.
   * @param par pre value of parent node
   * @param first first node to check
   */
  private void mergeTexts(final int par, final int first) {
    if(par < 0) return;
    int l = par + d.size(par, d.kind(par));
    // 'first-1' could be a text node
    int p = first == -1 ? par + 1 : first - 1;
    while(p < l) {
      final int k = d.kind(p);
      if(k == Data.ELEM) p += d.size(p, k);
      else if(p < l - 1 && k == Data.TEXT &&
          UpdatePrimitive.mergeTexts(d, p, p + 1)) l--;
      else p++;
    }
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
}