package org.basex.query.func.fn;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public class FnOutermost extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return most(qc, true);
  }

  /**
   * Returns the outermost/innermost nodes of a node sequence, i.e. a node is
   * only contained if none of its ancestors/descendants are.
   * @param qc query context
   * @param outer outermost flag
   * @return outermost/innermost nodes
   * @throws QueryException exception
   */
  NodeIter most(final QueryContext qc, final boolean outer) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    final ANodeBuilder list = new ANodeBuilder();
    for(Item it; (it = qc.next(iter)) != null;) list.add(toNode(it));
    list.check();

    // only go further if there are at least two nodes
    final int len = list.size();
    if(len < 2) return list.iter();

    // after this, the iterator is sorted and duplicate free
    final ANodeBuilder res = new ANodeBuilder();
    if(list.dbnodes()) {
      // nodes are sorted, so ancestors always come before their descendants
      // the first/last node is thus always included in the output
      final DBNode fst = (DBNode) list.get(outer ? 0 : len - 1);
      final Data data = fst.data();

      if(outer) {
        // skip the subtree of the last added node
        final DBNode dummy = new DBNode(data);
        for(int next = 0, p; next < len; next = p < 0 ? -p - 1 : p) {
          final DBNode nd = (DBNode) list.get(next);
          final int pre = nd.pre();
          dummy.pre(pre + data.size(pre, data.kind(pre)));
          p = list.binarySearch(dummy, next + 1, len - next - 1);
          res.add(nd);
        }
      } else {
        // skip ancestors of the last added node
        res.add(fst);
        int before = fst.pre();
        for(int l = len - 1; l-- != 0;) {
          final DBNode nd = (DBNode) list.get(l);
          final int pre = nd.pre();
          if(pre + data.size(pre, data.kind(pre)) <= before) {
            res.add(nd);
            before = pre;
          }
        }
      }
    } else {
      // multiple documents and/or constructed fragments
      OUTER: for(int l = 0; l < len; l++) {
        final ANode nd = list.get(l);
        final BasicNodeIter ax = outer ? nd.ancestor() : nd.descendant();
        for(ANode a; (a = ax.next()) != null;) {
          qc.checkStop();
          if(list.contains(a)) continue OUTER;
        }
        res.add(nd);
      }
    }
    return res.iter();
  }
}
