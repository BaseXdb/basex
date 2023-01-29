package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpG.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class FnAll extends StandardFunc {
  @Override
  public final Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // implementation for dynamic function lookup
    final Iter input = exprs[0].iter(qc);
    final FItem predicate = exprs.length > 1 ? toFunction(exprs[1], 1, qc) : null;

    final boolean some = some();
    for(Item item; (item = input.next()) != null;) {
      final Item it = predicate != null ? predicate.invoke(qc, info, item).item(qc, info) : item;
      if(toBoolean(it) ^ !some) return Bln.get(some);
    }
    return Bln.get(!some);
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = exprs[0];
    final SeqType st = input.seqType();
    final boolean some = some();
    if(st.zero()) return cc.merge(input, Bln.get(!some), info);

    // create FLWOR expression
    // some(INPUT, PREDICATE)  ->  (for $i in INPUT return PREDICATE($i)) = true()
    // all(INPUT, PREDICATE)  ->  not((for $i in INPUT return PREDICATE($i)) = false())
    final Var var = cc.copy(new Var(new QNm("item"), null, cc.qc, sc, info), new IntObjMap<>());
    final For fr = new For(var, input).optimize(cc);

    final Expr func = exprs.length > 1 ?
      coerceFunc(exprs[1], cc, SeqType.BOOLEAN_O, st.with(Occ.EXACTLY_ONE)) : null;
    final Expr ref = new VarRef(info, var).optimize(cc);
    final Expr rtrn = func != null ? new DynFuncCall(info, sc, func, ref).optimize(cc) : ref;
    final Expr flwor = new GFLWOR(info, fr, rtrn).optimize(cc);

    final Expr cmp = new CmpG(info, flwor, Bln.get(some), OpG.EQ, null, sc).optimize(cc);
    return some ? cmp : cc.function(Function.NOT, info, cmp);
  }

  /**
   * Compare some/all results.
   * @return flag
   */
  boolean some() {
    return false;
  }
}
