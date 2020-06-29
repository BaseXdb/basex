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
 * @author BaseX Team 2005-20, BSD License
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
    // replace list with union. examples:
    // util:ddo((<a/>, <b/>))  ->  <a/>, <b/>
    // util:ddo(($a, $a))  ->  $a
    Expr expr = exprs[0];
    if(expr instanceof List) {
      expr = ((List) expr).toUnion(cc);
      if(expr != exprs[0]) return expr;
    }

    final SeqType st = expr.seqType();
    final Type type = st.type;
    if(type instanceof NodeType) {
      // util:ddo(<a/>)  ->  <a/>
      if(expr.ddo() || st.zeroOrOne()) return expr;
      // adopt type of argument
      exprType.assign(type);
    }

    return this;
  }
}
