package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnDistinctOrderedNodes extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter nodes = arg(0).iter(qc);
    if(nodes.valueIter()) {
      final Value value = nodes.value(qc, null);
      if(value instanceof DBNodeSeq) return value;
    }

    final ANodeBuilder nb = new ANodeBuilder();
    for(Item item; (item = qc.next(nodes)) != null;) {
      nb.add(toNode(item));
    }
    return nb.value(this);
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    final Item item = arg(0).iter(qc).next();
    if(item == null) return false;
    toNode(item);
    return true;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    Expr nodes = arg(0);

    // replace list with union:
    // distinct-ordered-nodes((<a/>, <b/>))  ->  <a/> | <b/>
    // distinct-ordered-nodes(($a, $a))  ->  $a
    if(nodes instanceof final List list) {
      nodes = list.toUnion(cc);
      if(nodes != arg(0)) return nodes;
    }

    final Type type = nodes.seqType().type;
    if(type instanceof NodeType) {
      // distinct-ordered-nodes(replicate(*, 2))  ->  distinct-ordered-nodes(*)
      if(REPLICATE.is(nodes) && ((FnReplicate) nodes).singleEval(false))
        return nodes.arg(0);
      // distinct-ordered-nodes(reverse(*))  ->  distinct-ordered-nodes(*)
      if(REVERSE.is(nodes) || SORT.is(nodes))
        return cc.function(DISTINCT_ORDERED_NODES, info, nodes.arg(0));
      // distinct-ordered-nodes(/a/b/c)  ->  /a/b/c
      if(nodes.ddo())
        return nodes;
      // adopt type of input
      exprType.assign(type);
    }

    exprType.data(nodes);
    return this;
  }
}
