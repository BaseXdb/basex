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
import org.basex.query.value.seq.*;
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

    final Expr key = exprs[0], fs = exprs[1];
    if(key.isValue() && (fs instanceof Map || fs instanceof Array)) {
      // guaranteed to be fully evaluated
      return optPre(value(cc.qc), cc);
    }

    final Type tp = fs.seqType().type;
    final boolean map = tp instanceof MapType, array = tp instanceof ArrayType;
    if(!map && !array) return this;

    final boolean oneInput = fs.size() == 1 || fs.seqType().one();
    SeqType rt = ((FuncType) tp).type;
    if(rt != null) {
      // map lookup may result in empty sequence
      if(map && !rt.mayBeZero()) rt = rt.withOcc(rt.one() ? Occ.ZERO_ONE : Occ.ZERO_MORE);
      // wildcard or more than one input
      if(key == Str.WC || !oneInput) rt = rt.withOcc(rt.mayBeZero() ? Occ.ZERO_MORE : Occ.ONE_MORE);
      seqType = rt;
    }

    if(key != Str.WC) {
      if(oneInput) {
        // one function, rewrite to for-each or function call
        final Expr opt = key.size() == 1 || key.seqType().one()
            ? new DynFuncCall(info, cc.sc(), fs, key).optimize(cc)
            : cc.function(Function.FOR_EACH, info, exprs);
        return optPre(opt, cc);
      }

      if(key.isValue()) {
        // keys are constant, so we do not duplicate work in the inner loop
        final LinkedList<Clause> clauses = new LinkedList<>();
        final Var f = cc.vs().addNew(new QNm("f"), null, false, cc.qc, info);
        clauses.add(new For(f, null, null, fs, false));
        final Var k = cc.vs().addNew(new QNm("k"), null, false, cc.qc, info);
        clauses.add(new For(k, null, null, key, false));
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
    final Expr key = exprs[0];
    if(key == Str.WC) return wildCard(qc);
    if(key instanceof Item) return lookup((Item) key, qc);

    final ValueBuilder vb = new ValueBuilder();
    final Iter iter = exprs.length == 1 ? ctxValue(qc).iter() : qc.iter(exprs[1]);
    for(Item ctx; (ctx = iter.next()) != null;) {
      qc.checkStop();
      final Iter keys = qc.iter(key);
      final FItem f = mapOrArray(ctx);
      for(Item k; (k = keys.next()) != null;) {
        vb.add(f.invokeValue(qc, info, k));
      }
    }
    return vb.value();
  }

  /**
   * Checks if the given item is either a map or an array.
   * @param it item to check
   * @return cast item if check succeeded
   * @throws QueryException if the item is neither a map nor an array
   */
  private FItem mapOrArray(final Item it) throws QueryException {
    if(it instanceof Map || it instanceof Array) return (FItem) it;
    throw LOOKUP_X.get(info, it);
  }

  /**
   * Evaluates the {@code ?*} construct.
   * @param qc query context
   * @return all entries of all input values
   * @throws QueryException query exception
   */
  private Value wildCard(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    for(final Item it : exprs.length == 1 ? ctxValue(qc) : qc.value(exprs[1])) {
      if(it instanceof Map) {
        ((Map) it).values(vb);
      } else if(it instanceof Array) {
        for(final Value val : ((Array) it).members()) vb.add(val);
      } else {
        throw LOOKUP_X.get(info, it);
      }
    }
    return vb.value();
  }

  /**
   * Fast path for the case where the key is a single item.
   * @param key key to look up
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Value lookup(final Item key, final QueryContext qc) throws QueryException {
    final Iter iter = exprs.length == 1 ? ctxValue(qc).iter() : qc.iter(exprs[1]);

    final Item fst = iter.next();
    if(fst == null) return Empty.SEQ;

    final Value fstVal = mapOrArray(fst).invokeValue(qc, info, key);
    Item it = iter.next();
    if(it == null) return fstVal;

    final ValueBuilder vb = new ValueBuilder().add(fstVal);
    do {
      qc.checkStop();
      vb.add(mapOrArray(it).invokeValue(qc, info, key));
    } while((it = iter.next()) != null);
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

    final Expr key = exprs[0];
    if(key == Str.WC) return sb.append("?*").toString();

    if(key instanceof Str) {
      final Str str = (Str) key;
      if(XMLToken.isNCName(str.string())) return sb.append('?').append(str.toJava()).toString();
    } else if(key instanceof Int) {
      final long val = ((Int) key).itr();
      if(val >= 0) return sb.append('?').append(val).toString();
    }

    return sb.append(" ? (").append(exprs[0]).append(')').toString();
  }
}
