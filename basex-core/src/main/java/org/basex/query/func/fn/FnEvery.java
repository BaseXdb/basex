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
public class FnEvery extends StandardFunc {
  @Override
  public final Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // implementation for dynamic function lookup
    final Iter input = arg(0).iter(qc);
    final FItem predicate = defined(1) ? toFunction(arg(1), 2, qc) : null;

    int p = 0;
    final boolean some = some();
    for(Item item; (item = input.next()) != null;) {
      final Item it = predicate == null ? item :
        predicate.invoke(qc, info, item, Int.get(++p)).item(qc, info);
      if(toBoolean(it) == some) return Bln.get(some);
    }
    return Bln.get(!some);
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0), predicate = arg(1);
    final SeqType st = input.seqType();
    final boolean some = some();
    if(st.zero()) return cc.merge(input, Bln.get(!some), info);

    Expr result = null;
    if(defined(1)) {
      final int al = predicate.funcType() != null ? predicate.funcType().argTypes.length : -1;
      if(al == 1 || al == 2) {
        final IntObjMap<Var> vm = new IntObjMap<>();
        final Var i = cc.copy(new Var(new QNm("item"), null, cc.qc, sc, info), vm);
        final Expr item = new VarRef(info, i).optimize(cc);
        final For fr;
        Expr pos = null;
        if(al == 1) {
          // some : (for $i in INPUT return PREDICATE($i)) = true()
          // every:  not((for $i in INPUT return PREDICATE($i)) = false())
          fr = new For(i, input).optimize(cc);
        } else {
          // some : (for $i at $p in INPUT return PREDICATE($i, $p)) = true()
          // every:  not((for $i at $p in INPUT return PREDICATE($i, $p)) = false())
          final Var p = cc.copy(new Var(new QNm("pos"), null, cc.qc, sc, info), vm);
          fr = new For(i, p, null, input, false).optimize(cc);
          pos = new VarRef(info, p).optimize(cc);
        }
        final Expr[] args = al == 1 ? new Expr[] { item } : new Expr[] { item, pos };
        final Expr rtrn = new DynFuncCall(info, sc, coerce(1, cc, al), args).optimize(cc);
        result = new GFLWOR(info, fr, rtrn).optimize(cc);
      }
    } else {
      // some : INPUT = true()
      // every: not(INPUT = false())
      result = input;
    }
    if(result != null) {
      final Expr cmp = new CmpG(info, result, Bln.get(some), OpG.EQ, null, sc).optimize(cc);
      return some ? cmp : cc.function(Function.NOT, info, cmp);
    }
    return this;
  }

  /**
   * Compare some/all results.
   * @return flag
   */
  boolean some() {
    return false;
  }
}
