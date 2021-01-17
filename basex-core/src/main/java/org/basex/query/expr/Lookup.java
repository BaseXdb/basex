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
    final Expr keys = exprs[1];
    //
    final long ks = keys.seqType().mayBeArray() || keys.has(Flag.NDT) ? -1 : keys.size();

    Expr expr = this;
    final long is = inputs.size();
    if(is == 0) {
      expr = inputs;
    } else if(exprType()) {
      // only rewrite if input yields maps or arrays
      if(ks == 0) {
        expr = keys;
      } else if(keys != WILDCARD) {
        if(ks == 1) {
          // single keys
          if(is == 1) {
            // single input: INPUT?(KEY)  ->  INPUT(KEY)
            expr = new DynFuncCall(info, cc.sc(), inputs, keys).optimize(cc);
          } else {
            // multiple inputs: INPUTS?(KEY)  ->  INPUTS ! .(KEY)
            final Expr dfc = cc.get(inputs, () -> {
              final Expr ctx = new ContextValue(info).optimize(cc);
              return new DynFuncCall(info, cc.sc(), ctx, keys).optimize(cc);
            });
            expr = SimpleMap.get(cc, info, inputs, dfc);
          }
        } else if(ks != -1 && (inputs instanceof Value || inputs instanceof VarRef)) {
          // multiple deterministic keys, inputs are values or variable references
          if(is == 1) {
            // single input:
            //  INPUT?(KEYS)  ->  KEYS ! INPUT(.)
            final Expr dfc = cc.get(keys, () -> {
              final Expr ctx = new ContextValue(info).optimize(cc);
              return new DynFuncCall(info, cc.sc(), inputs, ctx).optimize(cc);
            });
            expr = SimpleMap.get(cc, info, keys, dfc);
          } else {
            // multiple inputs:
            //  INPUTS?(KEYS)  ->  for $_ in INPUTS return KEYS ! $_(.)
            final LinkedList<Clause> clauses = new LinkedList<>();
            final Var var = cc.vs().addNew(new QNm("_"), null, false, cc.qc, info);
            clauses.add(new For(var, inputs).optimize(cc));
            final VarRef ref = new VarRef(info, var).optimize(cc);
            final Expr dfc = cc.get(keys, () -> {
              final Expr ctx = new ContextValue(info).optimize(cc);
              return new DynFuncCall(info, cc.sc(), ref, ctx).optimize(cc);
            });
            expr = new GFLWOR(info, clauses, SimpleMap.get(cc, info, keys, dfc)).optimize(cc);
          }
        }
      } else {
        // wildcard, single input: pre-evaluate
        if(inputs instanceof Item) {
          return cc.preEval(expr);
        }
      }
    }
    return expr != this ? cc.replaceWith(this, expr) : this;
  }

  /**
   * Assigns a sequence type.
   * @return {@code true} if expression type was assigned
   */
  private boolean exprType() {
    final Expr inputs = exprs[0];
    final FuncType ft = inputs.funcType();
    final boolean map = ft instanceof MapType, array = ft instanceof ArrayType;
    if(!map && !array) return false;

    // derive type from input expression
    final Expr keys = exprs[1];
    final SeqType st = ft.declType;
    Occ occ = st.occ;
    if(inputs.size() != 1 || keys == WILDCARD || !keys.seqType().one() ||
        keys.seqType().mayBeArray()) {
      // key is wildcard, or expressions yield no single item
      occ = occ.union(Occ.ZERO_OR_MORE);
    } else if(map) {
      // map lookup may result in empty sequence
      occ = occ.union(Occ.ZERO);
    }

    exprType.assign(st.type, occ);
    return true;
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
        for(final Value value : ((XQArray) item).members()) vb.add(value);
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
