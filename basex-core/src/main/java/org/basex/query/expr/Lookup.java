package org.basex.query.expr;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
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
public final class Lookup extends Arr {
  /** Indicates if the keys can be evaluated once for all input items. */
  private boolean cacheKeys;
  /** Indicates if the input yields at most one item. */
  private boolean single;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr context expression and key specifier
   */
  public Lookup(final InputInfo info, final Expr... expr) {
    super(info, Types.ITEM_ZM, expr);
  }

  /**
   * Checks if a map of the specified type may be a strict record that lacks the requested key.
   * @param mt map type
   * @param key key expression
   * @param ii input info (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  public static boolean mayLackField(final MapType mt, final Expr key, final InputInfo ii)
      throws QueryException {
    return mt instanceof final ShapeType sh ?
      sh.strict() && !(key instanceof final AStr str && sh.fields().contains(str.string(ii))) :
      mt.keyType().intersect(BasicType.STRING) != null;
  }

  @Override
  public boolean navigational() {
    final Expr inputs = exprs[0];
    return (inputs instanceof Path || inputs instanceof Lookup) && inputs.navigational();
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    exprs[1] = exprs[1].simplifyFor(Simplify.DATA, cc);

    final Expr inputs = exprs[0], keys = exprs[1];
    cacheKeys = !keys.has(Flag.NDT);
    single = inputs.seqType().zeroOrOne();
    final long is = inputs.size();
    if(is == 0) return cc.replaceWith(this, inputs);

    // skip optimizations if input may yield items other than maps or arrays
    final Type tp = inputs.seqType().type;
    final boolean map = tp instanceof MapType, array = tp instanceof ArrayType;
    if(!(map || array)) return this;

    // pre-evaluate a single-key lookup
    if(is == 1 && inputs instanceof Value && keys instanceof Item) return cc.preEval(this);

    final Expr expr = opt(cc);
    if(expr != this) return cc.replaceWith(this, expr);

    // derive type from input expression
    final SeqType kt = keys.seqType();
    final SeqType st = map ? ((MapType) tp).valueType() : ((ArrayType) tp).valueType();
    Occ occ = st.occ;
    if(is != 1 || keys == Str.WILDCARD || !kt.one() || kt.mayBeWrapped()) {
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
    final Expr inputs = exprs[0], keys = exprs[1];
    final long is = inputs.size();
    final long ks = keys.seqType().mayBeWrapped() || keys.has(Flag.NDT) ? -1 : keys.size();
    if(ks == 0) return keys;

    final Type it = inputs.seqType().type;
    final boolean map = it instanceof MapType, array = it instanceof ArrayType;
    if(map || array) {
      // keep the lookup if a runtime value could be a strict record that lacks a requested key
      if(keys != Str.WILDCARD && it instanceof final MapType mt && mayLackField(mt, keys, info)) {
        return this;
      }

      /* REWRITE LOOKUP:
       *  MAP?*     → map:items(MAP)
       *  ARRAY?*   → array:items(MAP)
       *  MAP?KEY   → map:get(INPUT, KEY)
       *  ARRAY?KEY → array:get(INPUT, KEY) */
      final QueryBiFunction<Expr, Expr, Expr> rewrite = (in, arg) -> keys == Str.WILDCARD ?
        cc.function(map ? Function._MAP_ITEMS : Function._ARRAY_ITEMS, info, in) :
        cc.function(map ? Function._MAP_GET : Function._ARRAY_GET, info, in, arg);

      // single key
      if(ks == 1) {
        // single input:  INPUT?KEY → REWRITE(INPUT, KEY)
        if(is == 1) return rewrite.apply(inputs, keys);
        // multiple inputs:  INPUTS?KEY → INPUTS ! REWRITE(., KEY)
        return SimpleMap.get(cc, info, inputs,
            cc.get(inputs, true, () -> rewrite.apply(ContextValue.get(cc, info), keys)));
      }

      // multiple deterministic keys, input can be duplicated and is focus-independent
      if(ks != -1 && inputs.duplicable() && !inputs.has(Flag.CTX)) {
        // single input:  INPUT?KEYS → KEYS ! REWRITE(INPUT, .)
        if(is == 1) return SimpleMap.get(cc, info, keys,
            cc.get(keys, true, () -> rewrite.apply(inputs, ContextValue.get(cc, info))));
        // multiple inputs:  INPUT?KEYS → for $item in INPUT return KEYS ! REWRITE($item, .)
        final FLWORBuilder flwor = new FLWORBuilder(1, cc, info);
        final Expr next = cc.get(keys, true, () ->
          rewrite.apply(flwor.ref(flwor.item), ContextValue.get(cc, info)));
        final Expr rtrn = SimpleMap.get(cc, info, keys, next);
        return flwor.finish(inputs, null, rtrn);
      }
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    if(single) return value(qc).iter();

    return new Iter() {
      final Iter inputs = exprs[0].iter(qc);
      Iter results = Empty.ITER;
      Value keys;

      @Override
      public Item next() throws QueryException {
        while(true) {
          final Item result = qc.next(results);
          if(result != null) return result;
          final Item input = qc.next(inputs);
          if(input == null) return null;
          if(keys == null || !cacheKeys) keys = exprs[1].atomValue(qc, info);
          results = lookup(input, keys, qc).iter();
        }
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    if(single) {
      final Item input = exprs[0].item(qc, info);
      return input.isEmpty() ? Empty.VALUE : lookup(input, exprs[1].atomValue(qc, info), qc);
    }
    final Iter inputs = exprs[0].iter(qc);
    final ValueBuilder vb = new ValueBuilder(qc);
    Value keys = null;
    for(Item input; (input = qc.next(inputs)) != null;) {
      if(keys == null || !cacheKeys) keys = exprs[1].atomValue(qc, info);
      vb.add(lookup(input, keys, qc));
    }
    return vb.value(this);
  }

  /**
   * Looks up the specified keys in an input item.
   * @param input input item
   * @param keys keys
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Value lookup(final Item input, final Value keys, final QueryContext qc)
      throws QueryException {
    final long ks = keys.size();
    if(ks == 0) return Empty.VALUE;
    final Item item = input instanceof final JNode jnode ? jnode.value.item(qc, info) : input;
    if(item.isEmpty()) return Empty.VALUE;
    if(!(item instanceof final XQStruct struct)) throw LOOKUP_X.get(info, item);
    if(exprs[1] == Str.WILDCARD) return struct.items(qc);
    if(ks == 1) return struct.invoke(qc, info, keys.itemAt(0));

    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Item key : keys) vb.add(struct.invoke(qc, info, key));
    return vb.value(this);
  }

  @Override
  public Lookup copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    final Lookup lookup = copyType(new Lookup(info, copyAll(cc, vm, exprs)));
    lookup.single = single;
    return lookup;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final Lookup lookup &&
      (exprs[1] == Str.WILDCARD) == (lookup.exprs[1] == Str.WILDCARD) && super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(exprs[0]).token('?');

    final Expr keys = exprs[1];
    byte[] key = null;
    if(keys == Str.WILDCARD) {
      key = Str.WILDCARD.string();
    } else if(keys instanceof final Str str) {
      if(XMLToken.isNCName(str.string())) key = str.string();
    } else if(keys instanceof final Itr itr) {
      final long l = itr.itr();
      if(l >= 0) key = Token.token(l);
    }
    if(key != null) qs.value(key);
    else qs.paren(keys);
  }
}
