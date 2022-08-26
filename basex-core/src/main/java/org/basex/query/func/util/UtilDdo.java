package org.basex.query.func.util;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class UtilDdo extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter nodes = exprs[0].iter(qc);
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
  protected Expr opt(final CompileContext cc) throws QueryException {
    Expr nodes = exprs[0];

    // replace list with union:
    // util:ddo((<a/>, <b/>))  ->  <a/> | <b/>
    // util:ddo(($a, $a))  ->  $a
    if(nodes instanceof List) {
      nodes = ((List) nodes).toUnion(cc);
      if(nodes != exprs[0]) return nodes;
    }

    final Type type = nodes.seqType().type;
    if(type instanceof NodeType) {
      // util:ddo(replicate(*, 2))  ->  util:ddo(*)
      if(REPLICATE.is(nodes) && ((FnReplicate) nodes).singleEval(false)) return nodes.arg(0);
      // util:ddo(reverse(*))  ->  util:ddo(*)
      if(REVERSE.is(nodes) || SORT.is(nodes)) return cc.function(_UTIL_DDO, info, nodes.arg(0));
      // util:ddo(/a/b/c)  ->  /a/b/c
      if(nodes.ddo()) return nodes;
      // adopt type of argument
      exprType.assign(type);
    }
    return this;
  }
}
