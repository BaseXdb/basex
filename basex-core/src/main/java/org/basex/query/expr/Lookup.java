package org.basex.query.expr;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.ann.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
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
   * @param expr key specifier and (for postfix lookups) context expression
   */
  public Lookup(final InputInfo info, final Expr... expr) {
    super(info, SeqType.ITEM_ZM, expr);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    exprs[1] = exprs[1].simplifyFor(Simplify.DATA, cc);

    final Expr inputs = exprs[0];
    final long is = inputs.size();
    if(is == 0) return cc.replaceWith(this, inputs);

    // skip optimizations if input may yield other items than maps or arrays
    final Type tp = inputs.seqType().type;
    final boolean map = tp instanceof MapType, array = tp instanceof ArrayType;
    if(!(map || array)) return this;

    Expr expr = opt(cc);

    // replace if different, unless there is a chance of a %method that needs to be processed
    final SeqType st = map ? ((MapType) tp).valueType() : ((ArrayType) tp).valueType();
    if(expr != this && (array || !st.mayBeFunction())) {
      return cc.replaceWith(this, expr);
    }

    // derive type from input expression
    final Expr keys = exprs[1];
    final SeqType kt = keys.seqType();
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
    final long ks = keys.seqType().mayBeArray() || keys.has(Flag.NDT) ? -1 : keys.size();
    if(ks == 0) return keys;

    final long is = input.size();
    final QueryBiFunction<Expr, Expr, Expr> rewrite = (in, arg) ->
      keys == WILDCARD ? cc.function(input.seqType().type instanceof MapType ?
      Function._MAP_ITEMS : Function._ARRAY_ITEMS, info, in) :
      new DynFuncCall(info, in, arg).optimize(cc);

    // single keys
    if(ks == 1) {
      // single input:
      //   INPUT?(KEY)  ->  INPUT(KEY)
      //   ARRAY?*      ->  array:items(MAP)
      //   MAP?*        ->  map:items(MAP)
      if(is == 1) return rewrite.apply(input, keys);
      // multiple inputs:
      //   INPUTS?(KEY)  ->  INPUTS ! .(KEY)
      final Expr ex = cc.get(input, true, () -> rewrite.apply(ContextValue.get(cc, info), keys));
      return SimpleMap.get(cc, info, input, ex);
    }

    // multiple deterministic keys, inputs are values or variable references
    if(ks != -1 && (input instanceof Value || input instanceof VarRef)) {
      if(is == 1) {
        // single input:
        //  INPUT?(KEYS)  ->  KEYS ! INPUT(.)
        final Expr ex = cc.get(keys, true, () -> rewrite.apply(input, ContextValue.get(cc, info)));
        return SimpleMap.get(cc, info, keys, ex);
      }
      // multiple inputs:
      //  INPUTS?(KEYS)  ->  for $item in INPUTS return KEYS ! $item(.)
      final Var var = cc.vs().addNew(new QNm("item"), null, cc.qc, info);
      final For fr = new For(var, input).optimize(cc);
      final Expr ex = cc.get(keys, true, () ->
        rewrite.apply(new VarRef(info, var).optimize(cc), ContextValue.get(cc, info)));
      return new GFLWOR(info, fr, SimpleMap.get(cc, info, keys, ex)).optimize(cc);
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
    if(!(item instanceof XQStruct)) throw LOOKUP_X.get(info, item);
    final XQStruct struct = (XQStruct) item;
    final Expr keys = exprs[1];

    // wildcard: add all values
    if(keys == WILDCARD) return bindFocusIfNeeded(struct, struct.items(qc));

    final ValueBuilder vb = new ValueBuilder(qc);
    final Iter ir = keys.atomIter(qc, info);
    for(Item key; (key = ir.next()) != null;) {
      Value value = struct.invoke(qc, info, key);
      vb.add(bindFocusIfNeeded(struct, value));
    }
    return vb.value(this);
  }

  /**
   * Bind the focus of a %method function item, in case it is a singleton map value, to the map upon
   * lookup.
   * @param struct structure
   * @param value lookup result
   * @return value, or new function item with focus bound to map
   */
  private Value bindFocusIfNeeded(final XQStruct struct, final Value value) {
    if(!(struct instanceof XQMap) || !(value instanceof FuncItem)) return value;
    final FuncItem fi = (FuncItem) value;
    if(!fi.annotations().contains(Annotation.METHOD)) return value;
    final AnnList anns = AnnList.EMPTY;
    for(Ann a : fi.annotations()) if(a.definition != Annotation.METHOD) anns.attach(a);
    final QueryFocus qf = new QueryFocus();
    qf.value = struct;
    return new FuncItem(fi, anns, qf);
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
