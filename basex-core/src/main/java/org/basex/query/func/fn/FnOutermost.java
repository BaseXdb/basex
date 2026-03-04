package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnOutermost extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return iter(true, qc);
  }

  /**
   * Returns the outermost/innermost nodes of a node sequence, i.e. a node is
   * only contained if none of its ancestors/descendants are.
   * @param outer outermost flag
   * @param qc query context
   * @return outermost/innermost nodes
   * @throws QueryException exception
   */
  final Iter iter(final boolean outer, final QueryContext qc) throws QueryException {
    final Iter nodes = arg(0).iter(qc);

    final GNodeBuilder list = new GNodeBuilder();
    for(Item item; (item = qc.next(nodes)) != null;) {
      list.add(toGNode(item));
    }
    list.ddo();

    // only go further if there are at least two nodes
    final int len = list.size();
    if(len < 2) return list.value(this).iter();

    // after this, the iterator is sorted and duplicate free
    final GNodeBuilder builder = new GNodeBuilder();
    OUTER: for(int l = 0; l < len; l++) {
      final GNode node = list.get(l);
      final BasicNodeIter iter = outer ? node.ancestorIter(false) : node.descendantIter(false);
      for(GNode nd; (nd = iter.next()) != null;) {
        qc.checkStop();
        if(list.contains(nd)) continue OUTER;
      }
      builder.add(node);
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
