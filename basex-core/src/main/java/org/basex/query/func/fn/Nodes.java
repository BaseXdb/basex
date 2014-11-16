package org.basex.query.func.fn;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Node functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
abstract class Nodes extends StandardFunc {
  /**
   * Returns the outermost/innermost nodes of a node sequence, i.e. a node is
   * only contained if none of its ancestors/descendants are.
   * @param qc query context
   * @param outer outermost flag
   * @return outermost/innermost nodes
   * @throws QueryException exception
   */
  Iter most(final QueryContext qc, final boolean outer) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    final NodeSeqBuilder nc = new NodeSeqBuilder().check();
    for(Item it; (it = iter.next()) != null;) nc.add(toNode(it));
    final int len = (int) nc.size();

    // only go further if there are at least two nodes
    if(len < 2) return nc;

    // after this, the iterator is sorted and duplicate free
    if(nc.dbnodes()) {
      // nodes are sorted, so ancestors always come before their descendants
      // the first/last node is thus always included in the output
      final DBNode fst = (DBNode) nc.get(outer ? 0 : len - 1);
      final Data data = fst.data;
      final ANode[] nodes = nc.nodes.clone();

      if(outer) {
        // skip the subtree of the last added node
        nc.size(0);
        final DBNode dummy = new DBNode(fst.data);
        final NodeSeqBuilder src = new NodeSeqBuilder(nodes, len);
        for(int next = 0, p; next < len; next = p < 0 ? -p - 1 : p) {
          final DBNode nd = (DBNode) nodes[next];
          dummy.pre = nd.pre + data.size(nd.pre, data.kind(nd.pre));
          p = src.binarySearch(dummy, next + 1, len - next - 1);
          nc.add(nd);
        }
      } else {
        // skip ancestors of the last added node
        nc.nodes[0] = fst;
        nc.size(1);
        int before = fst.pre;
        for(int i = len - 1; i-- != 0;) {
          final DBNode nd = (DBNode) nodes[i];
          if(nd.pre + data.size(nd.pre, data.kind(nd.pre)) <= before) {
            nc.add(nd);
            before = nd.pre;
          }
        }
        // nodes were added in reverse order, correct that
        Array.reverse(nc.nodes, 0, (int) nc.size());
      }
      return nc;
    }

    // multiple documents and/or constructed fragments
    final NodeSeqBuilder out = new NodeSeqBuilder(new ANode[len], 0);
    OUTER: for(int i = 0; i < len; i++) {
      final ANode nd = nc.nodes[i];
      final AxisIter ax = outer ? nd.ancestor() : nd.descendant();
      for(ANode a; (a = ax.next()) != null;)
        if(nc.indexOf(a, false) != -1) continue OUTER;
      out.add(nd);
    }
    return out;
  }
}
