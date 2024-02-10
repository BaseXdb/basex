package org.basex.query.func.fn;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class FnOutermost extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return iter(true, qc);
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  /**
   * Returns the outermost/innermost nodes of a node sequence, i.e. a node is
   * only contained if none of its ancestors/descendants are.
   * @param outer outermost flag
   * @param qc query context
   * @return outermost/innermost nodes
   * @throws QueryException exception
   */
  Iter iter(final boolean outer, final QueryContext qc) throws QueryException {
    final Iter nodes = arg(0).iter(qc);

    final ANodeBuilder list = new ANodeBuilder();
    for(Item item; (item = qc.next(nodes)) != null;) {
      list.add(toNode(item));
    }
    list.ddo();

    // only go further if there are at least two nodes
    final int len = list.size();
    if(len < 2) return list.value(this).iter();

    // after this, the iterator is sorted and duplicate free
    final ANodeBuilder builder = new ANodeBuilder();
    final Data data = list.data();
    if(data != null) {
      // nodes are sorted, so ancestors always come before their descendants
      // the first/last node is thus always included in the output
      final DBNode fst = (DBNode) list.get(outer ? 0 : len - 1);

      if(outer) {
        // skip the subtree of the last added node
        final DBNode dummy = new DBNode(data);
        for(int next = 0, p; next < len; next = p < 0 ? -p - 1 : p) {
          final DBNode nd = (DBNode) list.get(next);
          final int pre = nd.pre();
          dummy.pre(pre + data.size(pre, data.kind(pre)));
          p = list.binarySearch(dummy, next + 1, len - next - 1);
          builder.add(nd);
        }
      } else {
        // skip ancestors of the last added node
        builder.add(fst);
        int before = fst.pre();
        for(int l = len - 1; l-- != 0;) {
          final DBNode nd = (DBNode) list.get(l);
          final int pre = nd.pre();
          if(pre + data.size(pre, data.kind(pre)) <= before) {
            builder.add(nd);
            before = pre;
          }
        }
      }
    } else {
      // multiple documents and/or constructed fragments
      OUTER: for(int l = 0; l < len; l++) {
        final ANode nd = list.get(l);
        final BasicNodeIter ax = outer ? nd.ancestorIter() : nd.descendantIter();
        for(ANode a; (a = ax.next()) != null;) {
          qc.checkStop();
          if(list.contains(a)) continue OUTER;
        }
        builder.add(nd);
      }
    }
    return builder.value(this).iter();
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    final SeqType st = arg(0).seqType();
    return st.zeroOrOne() && st.type instanceof NodeType ? arg(0) : this;
  }

  @Override
  public final boolean ddo() {
    return true;
  }
}
