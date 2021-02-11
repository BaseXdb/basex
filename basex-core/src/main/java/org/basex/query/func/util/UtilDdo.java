package org.basex.query.func.util;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
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
    final Type type = expr.seqType().type;

    // util:ddo(util:replicate(*, 2))  ->  util:ddo(*)
    if(expr instanceof UtilReplicate && ((UtilReplicate) expr).once() &&
        type instanceof NodeType) return expr.arg(0);

    // replace list with union:
    // util:ddo((<a/>, <b/>))  ->  <a/> | <b/>
    // util:ddo(($a, $a))  ->  $a
    if(expr instanceof List) {
      expr = ((List) expr).toUnion(cc);
      if(expr != exprs[0]) return expr;
    }

    // util:ddo(/a/b/c)  ->  /a/b/c
    if(expr.ddo()) return expr;

    // adopt type of argument
    if(type instanceof NodeType) exprType.assign(type);

    return this;
  }
}
