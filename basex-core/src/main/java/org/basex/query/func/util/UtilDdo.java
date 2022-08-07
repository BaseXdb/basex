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
    final Iter iter = exprs[0].iter(qc);
    final Value value = iter.iterValue();
    if(value instanceof DBNodeSeq) return value;

    final ANodeBuilder nb = new ANodeBuilder();
    for(Item item; (item = qc.next(iter)) != null;) nb.add(toNode(item));
    return nb.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    Expr expr = exprs[0];

    // replace list with union:
    // util:ddo((<a/>, <b/>))  ->  <a/> | <b/>
    // util:ddo(($a, $a))  ->  $a
    if(expr instanceof List) {
      expr = ((List) expr).toUnion(cc);
      if(expr != exprs[0]) return expr;
    }

    final Type type = expr.seqType().type;
    if(type instanceof NodeType) {
      // util:ddo(replicate(*, 2))  ->  util:ddo(*)
      if(REPLICATE.is(expr) && ((FnReplicate) expr).singleEval(false)) return expr.arg(0);
      // util:ddo(reverse(*))  ->  util:ddo(*)
      if(REVERSE.is(expr) || SORT.is(expr)) return cc.function(_UTIL_DDO, info, expr.arg(0));
      // util:ddo(/a/b/c)  ->  /a/b/c
      if(expr.ddo()) return expr;
      // adopt type of argument
      exprType.assign(type);
    }
    return this;
  }
}
