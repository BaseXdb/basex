package org.basex.query.up;

import static org.basex.query.util.Err.*;

import java.io.IOException;

import org.basex.core.Prop;
import org.basex.core.cmd.Export;
import org.basex.data.Data;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.query.up.primitives.Primitive;
import org.basex.util.IntList;

/**
 * Holds all update primitives for a specific data reference.
 *
 * @author BaseX Team 2005-11, BSD License
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
  protected void add(final Primitive p) throws QueryException {
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
          --p;
        }
        if(par != -1) il.add(par);
        checkNames(il.toArray());
      } else {
        if(k == Data.ELEM) checkNames(pre);
        --p;
      }
    }
  }

  /**
   * Checks nodes for duplicate attributes and namespace conflicts.
   * @param pres pre values of nodes to check (in descending order)
   * @throws QueryException query exception
   */
  private void checkNames(final int... pres) throws QueryException {
    final NamePool pool = new NamePool();
    final IntList il = new IntList();

    for(final int pre : pres) {
      final NodePrimitives ups = op.get(pre);
      if(ups != null)
        for(final Primitive up : ups) up.update(pool);

      // pre values consist exclusively of element and attribute nodes
      if(d.kind(pre) == Data.ATTR) {
        il.add(pre);
      } else {
        final int ps = pre + d.attSize(pre, Data.ELEM);
        for(int p = pre + 1; p < ps; ++p) {
          final byte[] nm = d.name(p, Data.ATTR);
          if(!il.contains(p)) {
            final QNm name = new QNm(nm);
            final byte[] uri = d.ns.uri(d.ns.uri(nm, p));
            if(uri != null) name.uri(uri);
            pool.add(name, NodeType.ATT);
          }
        }
      }
    }

    // find duplicate attributes
    final QNm dup = pool.duplicate();
    if(dup != null) UPATTDUPL.thrw(null, dup);

    // find namespace conflicts
    if(!pool.nsOK()) UPNSCONFL2.thrw(null);
  }

  /**
   * Identifies unnecessary update operations and removes them from the pending
   * update list.
   */
  private void treeAwareUpdates() {
    /* Tree Aware Updates: Unnecessary updates on the descendant axis of a
     * deleted or replaced node are identified and removed from the pending
     * update list. */
    final int l = nodes.size();
    int ni = 0;
    int c = 0;
    while(ni < l - 1) {
      final int pre = nodes.get(ni++);
      // If a node is deleted or replaced ...
      if(op.get(pre).updatesDestroyIdentity()) {
        final int followingAxisPre = pre + d.size(pre, d.kind(pre));
        // mark obsolete target nodes on the descendant axis.
        while(ni < l && nodes.get(ni) < followingAxisPre) {
          nodes.set(-1, ni++);
          c++;
        }
      }
    }
    // in case nothing changed on the pending update list, return
    if(c == 0) return;
    // Create a new list that contains necessary targets only
    final IntList newNodes = new IntList(nodes.size() - c);
    for(int i = 0; i < nodes.size(); i++) {
      final int pre = nodes.get(i);
      if(pre != -1) newNodes.add(pre);
    }
    nodes = newNodes;
  }

  @Override
  protected void apply(final QueryContext ctx) throws QueryException {
    // apply optimization
    treeAwareUpdates();

    /* Apply updates backwards starting with the highest pre value ->
     * no id's and less table alterations needed. */
    int par = -2;
    // first is the first node of par which is updated. so 'first-1' is the
    // lowest pre value where adjacent text nodes can exist.
    int first = -1;
    for(int i = nodes.size() - 1; i >= 0; i--) {
      final int pre = nodes.get(i);
      final int parT = d.parent(pre, d.kind(pre));
      if(parT != par) {
        /* adjacent text nodes are merged. merges can only be applied directly
         * after the update if no lower pre values or the same pre value
         * are effected. this is not the case for 'replace node', 'delete',
         * 'insert before' and 'insert after' operations. a node that is target
         * of 'insert before' or 'insert after' can still be replaced, or
         * deleted. merging texts would result in the second node also being
         * deleted/replaced. */
        if(first > -1) mergeTexts(par, first);
        first = -1;
        par = parT;
      }
      int add = 0;
      final NodePrimitives prim = op.get(pre);
      prim.optimize();
      if(prim.textAdjacency()) first = pre;
      for(final Primitive up : prim) add += up.apply(add);
    }
    if(first > -1) mergeTexts(par, first);
    d.flush();

    if(d.meta.prop.is(Prop.WRITEBACK) && !d.meta.path.isEmpty()) {
      try {
        Export.export(ctx.context.prop, d, d.meta.path);
      } catch(final IOException ex) {
        UPPUTERR.thrw(null, d.meta.path);
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
          Primitive.mergeTexts(d, p, p + 1)) --l;
      else ++p;
    }
  }

  @Override
  protected boolean parentDeleted(final int n) {
    final NodePrimitives up = op.get(n);

    if(up != null && up.updatesDestroyIdentity())
      return true;

    final int p = d.parent(n, d.kind(n));
    if(p == -1) return false;
    return parentDeleted(p);
  }
}