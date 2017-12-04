package org.basex.query.expr;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.query.value.map.Map;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Lookup expression.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class Lookup extends Arr {
  /**
   * Constructor.
   * @param info input info
   * @param expr key specifier and (for postfix lookups) context expression
   */
  public Lookup(final InputInfo info, final Expr... expr) {
    super(info, SeqType.ITEM_ZM, expr);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    final Expr keys = exprs[0];
    final long ks = keys.seqType().mayBeArray() ? -1 : keys.size();
    if(exprs.length == 1) {
      if(seqType(cc.qc.focus.value, keys)) {
        if(ks == 0) return cc.replaceWith(this, keys);
      }
      return this;
    }

    // postfix expression
    final Expr ctx = exprs[1];
    final long es = ctx.size();
    if(seqType(ctx, keys)) {
      if((ctx instanceof Map || ctx instanceof Array) && keys instanceof Value)
        return cc.preEval(this);

      if(es == 0) return cc.replaceWith(this, ctx);
      if(ks == 0) return cc.replaceWith(this, keys);

      if(keys != Str.WC) {
        Expr ex = this;
        if(es == 1) {
          // context expression yields one item
          if(ks == 1) {
            // one key: rewrite to function call
            ex = new DynFuncCall(info, cc.sc(), ctx, keys).optimize(cc);
          } else {
            // otherwise, rewrite to for each loop
            ex = cc.function(Function.FOR_EACH, info, exprs);
          }
        } else if(keys instanceof Value) {
          // keys are constant, so we do not duplicate work in the inner loop
          final LinkedList<Clause> clauses = new LinkedList<>();
          final Var c = cc.vs().addNew(new QNm("c"), null, false, cc.qc, info);
          clauses.add(new For(c, null, null, ctx, false));
          final Var k = cc.vs().addNew(new QNm("k"), null, false, cc.qc, info);
          clauses.add(new For(k, null, null, keys, false));
          final VarRef rc = new VarRef(info, c), rk = new VarRef(info, k);
          final DynFuncCall ret = new DynFuncCall(info, cc.sc(), rc, rk);
          ex = new GFLWOR(info, clauses, ret).optimize(cc);
        }
        return cc.replaceWith(this, ex);
      }
    }
    return this;
  }

  /**
   * Assigns a sequence type.
   * @param ctx context expression
   * @param keys keys
   * @return {@code true} if static type of the expression is map or array
   */
  private boolean seqType(final Expr ctx, final Expr keys) {
    if(ctx == null) return false;

    final Type tp = ctx.seqType().type;
    final boolean map = tp instanceof MapType, array = tp instanceof ArrayType;
    if(!map && !array) return false;

    // derive type from input expression
    final SeqType st = ((FuncType) tp).declType;
    Occ occ = st.occ;
    // map lookup may result in empty sequence (array lookups will always yield at least one item)
    if(map) occ = st.occ.union(Occ.ZERO);
    // key is wildcard, or expression yields no single item
    if(keys == Str.WC || ctx.size() != 1 || !keys.seqType().oneNoArray())
      occ = st.occ.union(Occ.ONE_MORE);

    exprType.assign(st.type, occ);
    return true;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    final Expr keys = exprs[0];
    final Iter iter = exprs.length == 1 ? ctxValue(qc).iter() : exprs[1].iter(qc);

    // iterate through all map/array inputs
    for(Item it; (it = qc.next(iter)) != null;) {
      if(!(it instanceof Map || it instanceof Array)) throw LOOKUP_X.get(info, it);
      final FItem fit = (FItem) it;

      if(keys == Str.WC) {
        // wildcard: add all values
        if(fit instanceof Map) {
          ((Map) fit).values(vb);
        } else {
          for(final Value val : ((Array) it).members()) vb.add(val);
        }
      } else {
        final Iter ir = keys.atomIter(qc, info);
        for(Item key; (key = qc.next(ir)) != null;) {
          vb.add(fit.invokeValue(qc, info, key));
        }
      }
    }
    return vb.value();
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.CTX.in(flags) && exprs.length == 1 || super.has(flags);
  }

  @Override
  public Lookup copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Lookup(info, copyAll(cc, vm, exprs)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Lookup && super.equals(obj);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    if(exprs.length > 1) sb.append(exprs[1]);

    final Expr keys = exprs[0];
    if(keys == Str.WC) return sb.append("?*").toString();

    if(keys instanceof Str) {
      final Str str = (Str) keys;
      if(XMLToken.isNCName(str.string())) return sb.append('?').append(str.toJava()).toString();
    } else if(keys instanceof Int) {
      final long val = ((Int) keys).itr();
      if(val >= 0) return sb.append('?').append(val).toString();
    }
    return sb.append("?(").append(keys).append(')').toString();
  }
}
