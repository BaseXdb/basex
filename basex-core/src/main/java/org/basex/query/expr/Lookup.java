package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Lookup expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Lookup extends ALookup {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param modifier modifier
   * @param expr context expression and key specifier
   */
  public Lookup(final InputInfo info, final Modifier modifier, final Expr... expr) {
    super(info, modifier, expr);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    exprs[1] = exprs[1].simplifyFor(Simplify.DATA, cc);

    final Expr inputs = exprs[0];
    final long is = inputs.size();
    if(is == 0) return cc.replaceWith(this, inputs);

    // skip optimizations if input may yield items other than maps or arrays
    final Type tp = inputs.seqType().type;
    final boolean map = tp instanceof MapType, array = tp instanceof ArrayType;
    if(!(map || array)) return this;

    final Expr expr = opt(cc);
    if(expr != this) return cc.replaceWith(this, expr);

    // derive type from input expression
    final Expr keys = exprs[1];
    final SeqType kt = keys.seqType();
    final SeqType st = map ? ((MapType) tp).valueType() : ((ArrayType) tp).valueType();
    Occ occ = st.occ;
    if(inputs.size() != 1 || keys == WILDCARD || !kt.one() || kt.mayBeArray()) {
      // key is wildcard, or expressions yield no single item
      occ = occ.union(Occ.ZERO_OR_MORE);
    } else if(map) {
      // map lookup may result in empty sequence
      occ = occ.union(Occ.ZERO);
    }
    // invalidate function type (%method annotation would need to be removed from type)
    exprType.assign(st.mayBeFunction() ? AtomType.ITEM : st.type, occ);
    return this;
  }

  /**
   * Rewrites the lookup to another expression.
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  private Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = exprs[0], keys = exprs[1];
    final long is = input.size();
    final long ks = keys.seqType().mayBeArray() || keys.has(Flag.NDT) ? -1 : keys.size();
    if(ks == 0) return keys;

    final Type it = input.seqType().type;
    final boolean map = it instanceof MapType, array = it instanceof ArrayType;
    if((map || array) && hasDefaultModifier()) {
      /* REWRITE LOOKUP:
       *  MAP?*      ->  map:items(MAP)
       *  ARRAY?*    ->  array:items(MAP)
       *  MAP?KEY    ->  map:get(INPUT, KEY, (), true())
       *  ARRAY?KEY  ->  array:get(INPUT, KEY) */
      final QueryBiFunction<Expr, Expr, Expr> rewrite = (in, arg) -> keys == WILDCARD ?
        map ? cc.function(Function._MAP_ITEMS, info, in) :
          cc.function(Function._ARRAY_ITEMS, info, in) :
        map ? cc.function(Function._MAP_GET, info, in, arg, Empty.UNDEFINED, Bln.TRUE) :
          cc.function(Function._ARRAY_GET, info, in, arg, Empty.VALUE);

      // single key
      if(ks == 1) {
        // single input:  INPUT?KEY  ->  REWRITE(INPUT, KEY)
        if(is == 1) return rewrite.apply(input, keys);
        // multiple inputs:  INPUTS?KEY  ->  INPUTS ! REWRITE(., KEY)
        return SimpleMap.get(cc, info, input,
            cc.get(input, true, () -> rewrite.apply(ContextValue.get(cc, info), keys)));
      }

      // multiple deterministic keys, inputs are values or variable references
      if(ks != -1 && (input instanceof Value || input instanceof VarRef)) {
        if(is == 1) {
          // single input:  INPUT?KEYS  ->  KEYS ! REWRITE(INPUT, .)
          return SimpleMap.get(cc, info, keys,
              cc.get(keys, true, () -> rewrite.apply(input, ContextValue.get(cc, info))));
        }
        // multiple inputs:  INPUTS?KEYS  ->  for $item in INPUTS return KEYS ! REWRITE($item, .)
        final Var var = cc.vs().addNew(new QNm("item"), null, cc.qc, info);
        final For fr = new For(var, input).optimize(cc);
        final Expr ex = cc.get(keys, true, () ->
          rewrite.apply(new VarRef(info, var).optimize(cc), ContextValue.get(cc, info)));
        return new GFLWOR(info, fr, SimpleMap.get(cc, info, keys, ex)).optimize(cc);
      }
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
          ir = valueFor(item, false, qc).iter();
        }
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    final Iter iter = exprs[0].iter(qc);
    for(Item item; (item = qc.next(iter)) != null;) {
      vb.add(valueFor(item, false, qc));
    }
    return vb.value(this);
  }

  @Override
  public Lookup copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new Lookup(info, modifier, copyAll(cc, vm, exprs)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Lookup && super.equals(obj);
  }
}
