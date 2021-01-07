package org.basex.query.expr;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Lookup expression.
 *
 * @author BaseX Team 2005-20, BSD License
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
    exprs[0] = exprs[0].simplifyFor(Simplify.STRING, cc);

    final Expr keys = exprs[0];
    final long ks = keys.seqType().mayBeArray() ? -1 : keys.size();
    if(unary()) {
      return ks == 0 && exprType(cc.qc.focus.value, keys) ? cc.replaceWith(this, keys) : this;
    }

    // postfix expression
    final Expr ctx = exprs[1];
    if(exprType(ctx, keys)) {
      if((ctx instanceof XQMap || ctx instanceof XQArray) && keys instanceof Value)
        return cc.preEval(this);

      final long es = ctx.size();
      if(es == 0) return cc.replaceWith(this, ctx);
      if(ks == 0) return cc.replaceWith(this, keys);

      if(keys != Str.WILDCARD) {
        Expr expr = this;
        if(es == 1) {
          // context expression yields one item
          if(ks == 1) {
            // one key: rewrite to function call
            expr = new DynFuncCall(info, cc.sc(), ctx, keys).optimize(cc);
          } else if(ks != -1) {
            // otherwise, rewrite to for each loop
            expr = cc.function(Function.FOR_EACH, info, exprs);
          }
        } else if(keys instanceof Value) {
          // keys are constant, so we do not duplicate work in the inner loop
          final LinkedList<Clause> clauses = new LinkedList<>();
          final Var c = cc.vs().addNew(new QNm("c"), null, false, cc.qc, info);
          clauses.add(new For(c, ctx));
          final Var k = cc.vs().addNew(new QNm("k"), null, false, cc.qc, info);
          clauses.add(new For(k, keys));
          final VarRef rc = new VarRef(info, c), rk = new VarRef(info, k);
          final DynFuncCall rtrn = new DynFuncCall(info, cc.sc(), rc, rk);
          expr = new GFLWOR(info, clauses, rtrn).optimize(cc);
        }
        return cc.replaceWith(this, expr);
      }
    }

    // return result or expression
    return allAreValues(true) ? cc.preEval(this) : this;
  }

  /**
   * Assigns a sequence type.
   * @param ctx context expression
   * @param keys keys
   * @return {@code true} if expression type was assigned
   */
  private boolean exprType(final Expr ctx, final Expr keys) {
    if(ctx == null) return false;

    final FuncType ft = ctx.funcType();
    final boolean map = ft instanceof MapType, array = ft instanceof ArrayType;
    if(!map && !array) return false;

    // derive type from input expression
    final SeqType st = ft.declType;
    Occ occ = st.occ;
    if(keys == Str.WILDCARD || ctx.size() != 1 || !keys.seqType().one() ||
        keys.seqType().mayBeArray()) {
      // key is wildcard, or expression yields no single item
      occ = occ.union(Occ.ZERO_OR_MORE);
    } else if(map) {
      // map lookup may result in empty sequence
      occ = occ.union(Occ.ZERO);
    }

    exprType.assign(st.type, occ);
    return true;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Expr keys = exprs[0];
    final Iter iter = (unary() ? ctxValue(qc) : exprs[1]).iter(qc);

    // iterate through all map/array inputs
    final ValueBuilder vb = new ValueBuilder(qc);
    for(Item item; (item = qc.next(iter)) != null;) {
      if(!(item instanceof XQMap || item instanceof XQArray)) throw LOOKUP_X.get(info, item);
      final FItem fit = (FItem) item;

      if(keys == Str.WILDCARD) {
        // wildcard: add all values
        if(fit instanceof XQMap) {
          ((XQMap) fit).values(vb);
        } else {
          for(final Value value : ((XQArray) item).members()) vb.add(value);
        }
      } else {
        final Iter ir = keys.atomIter(qc, info);
        for(Item key; (key = qc.next(ir)) != null;) vb.add(fit.invoke(qc, info, key));
      }
    }
    return vb.value(this);
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.CTX.in(flags) && unary() || super.has(flags);
  }

  @Override
  public VarUsage count(final Var var) {
    // context reference check: check if this is a unary lookup
    return (var == null && unary() ? VarUsage.ONCE : VarUsage.NEVER).plus(super.count(var));
  }

  @Override
  public Expr inline(final InlineContext ic) throws QueryException {
    return inline(ic, () -> unary() ? new Lookup(info, exprs[0], ic.copy()) : null);
  }

  @Override
  public Lookup copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Lookup(info, copyAll(cc, vm, exprs)));
  }

  /**
   * Checks if this is a unary lookup.
   * @return result of check
   */
  private boolean unary() {
    return exprs.length == 1;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Lookup && super.equals(obj);
  }

  @Override
  public void plan(final QueryString qs) {
    if(exprs.length > 1) qs.token(exprs[1]);
    qs.token('?');

    final Expr keys = exprs[0];
    Object key = null;
    if(keys == Str.WILDCARD) {
      key = "*";
    } else if(keys instanceof Str) {
      final Str str = (Str) keys;
      if(XMLToken.isNCName(str.string())) key = str.toJava();
    } else if(keys instanceof Int) {
      final long l = ((Int) keys).itr();
      if(l >= 0) key = l;
    }
    if(key != null) qs.token(key);
    else qs.paren(keys);
  }
}
