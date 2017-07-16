package org.basex.query.expr;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.gflwor.GFLWOR.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.query.value.map.Map;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Unary lookup expression.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class Lookup extends Arr {
  /**
   * Constructor.
   * @param info input info
   * @param expr expression and optional context
   */
  public Lookup(final InputInfo info, final Expr... expr) {
    super(info, expr);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    if(exprs.length != 2) return this;

    final Expr keys = exprs[0], expr = exprs[1];
    if(keys.isValue() && (expr instanceof Map || expr instanceof Array)) {
      // guaranteed to be fully evaluated
      return optPre(value(cc.qc), cc);
    }

    final Type tp = expr.seqType().type;
    final boolean map = tp instanceof MapType, array = tp instanceof ArrayType;
    if(!map && !array) return this;

    final boolean oneInput = expr.size() == 1 || expr.seqType().one();
    SeqType rt = ((FuncType) tp).type;
    if(rt != null) {
      // map lookup may result in empty sequence
      if(map && !rt.mayBeZero()) rt = rt.withOcc(rt.one() ? Occ.ZERO_ONE : Occ.ZERO_MORE);
      // wildcard or more than one input
      if(keys == Str.WC || !oneInput)
        rt = rt.withOcc(rt.mayBeZero() ? Occ.ZERO_MORE : Occ.ONE_MORE);
      seqType = rt;
    }

    if(keys != Str.WC) {
      if(oneInput) {
        // one function, rewrite to for-each or function call
        final Expr opt = keys.size() == 1 || keys.seqType().one()
            ? new DynFuncCall(info, cc.sc(), expr, keys).optimize(cc)
            : cc.function(Function.FOR_EACH, info, exprs);
        return optPre(opt, cc);
      }

      if(keys.isValue()) {
        // keys are constant, so we do not duplicate work in the inner loop
        final LinkedList<Clause> clauses = new LinkedList<>();
        final Var f = cc.vs().addNew(new QNm("f"), null, false, cc.qc, info);
        clauses.add(new For(f, null, null, expr, false));
        final Var k = cc.vs().addNew(new QNm("k"), null, false, cc.qc, info);
        clauses.add(new For(k, null, null, keys, false));
        final VarRef rf = new VarRef(info, f), rk = new VarRef(info, k);
        final DynFuncCall ret = new DynFuncCall(info, cc.sc(), rf, rk);
        return optPre(new GFLWOR(info, clauses, ret), cc).optimize(cc);
      }
    }

    return this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    final Expr keys = exprs[0];
    final Iter iter = exprs.length == 1 ? ctxValue(qc).iter() : qc.iter(exprs[1]);

    // iterate through all map/array inputs
    for(Item it; (it = iter.next()) != null;) {
      qc.checkStop();

      if(!(it instanceof Map || it instanceof Array)) throw LOOKUP_X.get(info, it);
      final FItem fit = (FItem) it;

      if(keys == Str.WC) {
        // wildcard: add all values
        if(fit instanceof Map) {
          ((Map) fit).values(vb);
        } else {
          for(final Value val : ((Array) it).members()) vb.add(val);
        }
      } else if(keys instanceof Item) {
        // single key
        vb.add(fit.invokeValue(qc, info, (Item) keys));
      } else {
        // dynamic key(s)
        final Iter ir = qc.iter(keys);
        for(Item key; (key = ir.next()) != null;) {
          vb.add(fit.invokeValue(qc, info, key));
        }
      }
    }
    return vb.value();
  }

  @Override
  public boolean has(final Flag flag) {
    return exprs.length == 1 && flag == Flag.CTX || super.has(flag);
  }

  @Override
  public Lookup copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Lookup(info, copyAll(cc, vm, exprs)));
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    if(exprs.length > 1) sb.append('(').append(exprs[1]).append(')');

    final Expr keys = exprs[0];
    if(keys == Str.WC) return sb.append("?*").toString();

    if(keys instanceof Str) {
      final Str str = (Str) keys;
      if(XMLToken.isNCName(str.string())) return sb.append('?').append(str.toJava()).toString();
    } else if(keys instanceof Int) {
      final long val = ((Int) keys).itr();
      if(val >= 0) return sb.append('?').append(val).toString();
    }

    return sb.append(" ? (").append(keys).append(')').toString();
  }
}
