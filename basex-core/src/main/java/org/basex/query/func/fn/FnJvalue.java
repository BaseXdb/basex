package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
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
    final Item item = contextArg(qc).item(qc, info);
    return item instanceof final JNode jnode ? jnode.value : item;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final boolean context = usesContext();
    final Expr input = contextArg(cc);
    // jvalue(jtree(E)) → E
    if(JTREE.is(input)) return input.arg(0);

    final SeqType st = input != null ? input.seqType() : null;
    // jvalue(1) → 1
    // $strings[jvalue() = 'a'] → $strings[. = 'a']
    if(st != null && !st.mayBeJNode() && st.zeroOrOne()) return contextExpr(input, cc);
    // adopt value type: jvalue(jtree({ 'a': 1 })/a) → xs:integer?
    final SeqType vt = st != null ? st.valueType() : null;
    if(vt != null) exprType.assign(context || st.one() ? vt : vt.union(Occ.ZERO));
    return this;
  }
}
