package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnGenerate extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final FItem step = toFunction(arg(1), 2, qc);

    return new Iter() {
      final HofArgs args = new HofArgs(2, step);
      Item item;

      @Override
      public Item next() throws QueryException {
        final Expr expr = item == null ? arg(0) : invoke(step, args.set(0, item).inc(), qc);
        item = expr.item(qc, info);
        return item != Empty.VALUE ? item : null;
      }
    };
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr init = arg(0), step = arg(1);
    SeqType st = init.seqType();
    if(st.zero()) return init;

    if(step instanceof FuncItem && st.one()) {
      SeqType ost;
      do {
        final SeqType[] types = { st, Types.INTEGER_O };
        arg(1, arg -> refineFunc(step, cc, types));
        ost = st;
        st = st.union(arg(1).funcType().declType);
      } while(!st.eq(ost));
      exprType.assign(st.with(Occ.ONE_OR_MORE));
    }
    return this;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    final Expr init = arg(0), step = arg(1);

    Expr expr = this;
    if(mode.oneOf(Simplify.DISTINCT, Simplify.PREDICATE)) {
      // 'x' = generate('y', fn { 'y' }) → 'x' = 'y'
      // distinct-values(generate(1, identity#1)) → distinct-values(1)
      if(init instanceof Item && step instanceof FuncItem fi && fi.arity() < 3 && (
          fi.expr.equals(init) || fi.funcName().eq(Function.IDENTITY.definition().name))) {
        expr = init;
      }
    }
    return cc.simplify(this, expr, mode);
  }
}
