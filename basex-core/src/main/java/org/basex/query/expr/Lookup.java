package org.basex.query.expr;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
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
public final class Lookup extends Arr {
  /** Wildcard string. */
  public static final Str WILDCARD = Str.get('*');

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr context expression and key specifier
   */
  public Lookup(final InputInfo info, final Expr... expr) {
    super(info, Types.ITEM_ZM, expr);
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
    exprType.assign(st.type, occ);
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
    if(map || array) {
      /* REWRITE LOOKUP:
       *  MAP?*     → map:items(MAP)
       *  ARRAY?*   → array:items(MAP)
       *  MAP?KEY   → map:get(INPUT, KEY)
       *  ARRAY?KEY → array:get(INPUT, KEY) */
      final QueryBiFunction<Expr, Expr, Expr> rewrite = (in, arg) -> keys == WILDCARD ?
        cc.function(map ? Function._MAP_ITEMS : Function._ARRAY_ITEMS, info, in) :
        cc.function(map ? Function._MAP_GET : Function._ARRAY_GET, info, in, arg);

      // single key
      if(ks == 1) {
        // single input:  INPUT?KEY → REWRITE(INPUT, KEY)
        if(is == 1) return rewrite.apply(input, keys);
        // multiple inputs:  INPUTS?KEY → INPUTS ! REWRITE(., KEY)
        return SimpleMap.get(cc, info, input,
            cc.get(input, true, () -> rewrite.apply(ContextValue.get(cc, info), keys)));
      }

      // multiple deterministic keys, inputs are values or variable references
      if(ks != -1 && (input instanceof Value || input instanceof VarRef)) {
        // single input:  INPUT?KEYS → KEYS ! REWRITE(INPUT, .)
        if(is == 1) return SimpleMap.get(cc, info, keys,
            cc.get(keys, true, () -> rewrite.apply(input, ContextValue.get(cc, info))));
        // multiple inputs:  INPUT?KEYS → for $item in INPUT return KEYS ! REWRITE($item, .)
        final FLWORBuilder flwor = new FLWORBuilder(1, cc, info);
        final Expr next = cc.get(keys, true, () ->
          rewrite.apply(flwor.ref(flwor.item), ContextValue.get(cc, info)));
        final Expr rtrn = SimpleMap.get(cc, info, keys, next);
        return flwor.finish(input, null, rtrn);
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
          ir = valueFor(item, qc).iter();
        }
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    final Iter iter = exprs[0].iter(qc);
    for(Item item; (item = qc.next(iter)) != null;) {
      vb.add(valueFor(item, qc));
    }
    return vb.value(this);
  }

  /**
   * Returns the looked up values for the specified input.
   * @param item input item
   * @param qc query context
   * @return supplied value builder
   * @throws QueryException query exception
   */
  private Value valueFor(final Item item, final QueryContext qc) throws QueryException {
    if(!(item instanceof final XQStruct struct)) throw LOOKUP_X.get(info, item);
    final Expr keys = exprs[1];

    // wildcard: add all values
    if(keys == WILDCARD) return struct.items(qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    final Iter ir = keys.atomIter(qc, info);
    for(Item key; (key = ir.next()) != null;) {
      vb.add(struct.invoke(qc, info, key));
    }
    return vb.value(this);
  }

  @Override
  public Lookup copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new Lookup(info, copyAll(cc, vm, exprs)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Lookup && super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(exprs[0]).token('?');

    final Expr keys = exprs[1];
    Object key = null;
    if(keys == WILDCARD) {
      key = WILDCARD.string();
    } else if(keys instanceof final Str str) {
      if(XMLToken.isNCName(str.string())) key = str.toJava();
    } else if(keys instanceof final Itr itr) {
      final long l = itr.itr();
      if(l >= 0) key = l;
    }
    if(key != null) qs.token(key);
    else qs.paren(keys);
  }
}
