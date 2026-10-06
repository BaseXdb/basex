package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnJvalue extends ContextFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final JNode jnode = toJNodeOrNull(context(qc), qc);
    return jnode != null ? jnode.value : Empty.VALUE;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final boolean context = contextAccess();
    final Expr input = context ? cc.qc.focus.value : arg(0);
    // jvalue(jtree(E)) → E
    if(JTREE.is(input)) return input.arg(0);

    // adopt value type: jvalue(jtree({ 'a': 1 })/a) → xs:integer?
    final SeqType st = input != null ? input.seqType() : null;
    if(st != null && st.type instanceof final NodeType nt &&
        nt.test instanceof final JNodeTest jt) {
      exprType.assign(context || st.one() ? jt.valueType : jt.valueType.union(Occ.ZERO));
    }
    return this;
  }
}
