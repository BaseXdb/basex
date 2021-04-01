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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Lookup extends Arr {
  /** Wildcard string. */
  public static final Str WILDCARD = Str.get(new byte[] { '*' });

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
    exprs[1] = exprs[1].simplifyFor(Simplify.STRING, cc);

    final Expr inputs = exprs[0];
    final long is = inputs.size();
    if(is == 0) return cc.replaceWith(this, inputs);

    // skip optimizations if input may yield other items than maps or arrays
    final FuncType ft = inputs.funcType();
    final boolean map = ft instanceof MapType, array = ft instanceof ArrayType;
    if(!(map || array)) return this;

    final Expr expr = opt(cc);
    if(expr != this) return cc.replaceWith(this, expr);

    // derive type from input expression
    final Expr keys = exprs[1];
    final SeqType st = ft.declType, kt = keys.seqType();
    Occ occ = st.occ;
    if(inputs.size() != 1 || keys == WILDCARD || !kt.one() || kt.mayBeArray()) {
      // key is wildcard, or expressions yield no single item
      occ = occ.union(Occ.ZERO_OR_MORE);
    } else if(map) {
      // map lookup may result in empty sequence
      occ = occ.union(Occ.ZERO);
    }
    exprType.assign(st.type, occ);

    return this;
  }

  /**
   * Rewrites the lookup to another expression.
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  public Expr opt(final CompileContext cc) throws QueryException {
    final Expr inputs = exprs[0], keys = exprs[1];
    final long ks = keys.seqType().mayBeArray() || keys.has(Flag.NDT) ? -1 : keys.size();
    if(ks == 0) return keys;

    final long is = inputs.size();
    final QueryBiFunction<Expr, Expr, Expr> rewrite = (input, arg) ->
      keys == WILDCARD ? cc.function(inputs.funcType() instanceof MapType ?
      Function._UTIL_MAP_VALUES : Function._UTIL_ARRAY_VALUES, info, input) :
      new DynFuncCall(info, cc.sc(), input, arg).optimize(cc);

    // single keys
    if(ks == 1) {
      // single input:
      //   INPUT?(KEY)  ->  INPUT(KEY)
      //   ARRAY?*      ->  util:array-values(MAP)
      //   MAP?*        ->  util:map-values(MAP)
      if(is == 1) return rewrite.apply(inputs, keys);
      // multiple inputs:
      //   INPUTS?(KEY)  ->  INPUTS ! .(KEY)
      final Expr ex = cc.get(inputs, () -> rewrite.apply(ContextValue.get(cc, info), keys));
      return SimpleMap.get(cc, info, inputs, ex);
    }

    // multiple deterministic keys, inputs are values or variable references
    if(ks != -1 && (inputs instanceof Value || inputs instanceof VarRef)) {
      if(is == 1) {
        // single input:
        //  INPUT?(KEYS)  ->  KEYS ! INPUT(.)
        final Expr ex = cc.get(keys, () -> rewrite.apply(inputs, ContextValue.get(cc, info)));
        return SimpleMap.get(cc, info, keys, ex);
      }
      // multiple inputs:
      //  INPUTS?(KEYS)  ->  for $_ in INPUTS return KEYS ! $_(.)
      final LinkedList<Clause> clauses = new LinkedList<>();
      final Var var = cc.vs().addNew(new QNm("_"), null, false, cc.qc, info);
      clauses.add(new For(var, inputs).optimize(cc));
      final Expr ex = cc.get(keys, () ->
        rewrite.apply(new VarRef(info, var).optimize(cc), ContextValue.get(cc, info)));
      return new GFLWOR(info, clauses, SimpleMap.get(cc, info, keys, ex)).optimize(cc);
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      final Iter iter = exprs[0].iter(qc);
      Iter ir;

      @Override
      public Item next() throws QueryException {
        while(true) {
          if(ir != null) {
            final Item item = qc.next(ir);
            if(item != null) return item;
          }
          final Item item = qc.next(iter);
          if(item == null) return null;
          ir = add(item, new ValueBuilder(qc), qc).value(Lookup.this).iter();
        }
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    final Iter iter = exprs[0].iter(qc);
    for(Item item; (item = qc.next(iter)) != null;) add(item, vb, qc);
    return vb.value(this);
  }

  /**
   * Adds values to the specified value builder.
   * @param item input item
   * @param vb value builder
   * @param qc query context
   * @return supplied value builder
   * @throws QueryException query exception
   */
  private ValueBuilder add(final Item item, final ValueBuilder vb, final QueryContext qc)
      throws QueryException {

    if(!(item instanceof XQMap || item instanceof XQArray)) throw LOOKUP_X.get(info, item);

    final Expr keys = exprs[1];
    if(keys == WILDCARD) {
      // wildcard: add all values
      if(item instanceof XQMap) {
        ((XQMap) item).values(vb);
      } else {
        for(final Value member : ((XQArray) item).members()) vb.add(member);
      }
    } else {
      final FItem fitem = (FItem) item;
      final Iter ir = keys.atomIter(qc, info);
      for(Item key; (key = qc.next(ir)) != null;) vb.add(fitem.invoke(qc, info, key));
    }
    return vb;
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
  public void plan(final QueryString qs) {
    qs.token(exprs[0]).token('?');

    final Expr keys = exprs[1];
    Object key = null;
    if(keys == WILDCARD) {
      key = WILDCARD.string();
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
