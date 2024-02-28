package org.basex.query.value.item;

import static org.basex.query.QueryError.*;

import java.util.function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Abstract super class for function items.
 * This class is inherited by {@link XQMap}, {@link Array}, and {@link FuncItem}.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public abstract class FItem extends Item implements XQFunction {
  /**
   * Constructor.
   * @param type function type
   */
  protected FItem(final Type type) {
    super(type);
  }

  @Override
  public final boolean equal(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    throw FIATOMIZE_X.get(ii, this);
  }

  @Override
  public final boolean atomicEqual(final Item item, final InputInfo ii) {
    return false;
  }

  @Override
  public void refineType(final Expr expr) {
    final FuncType t = funcType().intersect(expr.seqType().type);
    if(t != null) type = t;
  }

  @Override
  public final FuncType funcType() {
    return (FuncType) type;
  }

  @Override
  public Item materialize(final Predicate<Data> test, final InputInfo ii, final QueryContext qc)
      throws QueryException {
    throw BASEX_STORE_X.get(ii, this);
  }

  @Override
  public boolean materialized(final Predicate<Data> test, final InputInfo ii)
      throws QueryException {
    throw BASEX_STORE_X.get(ii, this);
  }

  /**
   * Converts this function item to the given function type.
   * @param ft function type
   * @param qc query context
   * @param cc compilation context ({@code null} during runtime)
   * @param ii input info (can be {@code null})
   * @return coerced item
   * @throws QueryException query exception
   */
  public abstract FItem coerceTo(FuncType ft, QueryContext qc, CompileContext cc, InputInfo ii)
      throws QueryException;

  /**
   * Converts this function item to the given function type.
   * @param ft function type
   * @param qc query context
   * @param cc compilation context ({@code null} during runtime)
   * @param ii input info (can be {@code null})
   * @param sc static context (can be {@code null})
   * @param updating updating flag
   * @return coerced item
   * @throws QueryException query exception
   */
  final FItem coerceTo(final FuncType ft, final QueryContext qc, final CompileContext cc,
      final InputInfo ii, final StaticContext sc, final boolean updating) throws QueryException {

    final SeqType[] argTypes = ft.argTypes;
    final int arity = arity(), nargs = argTypes.length;
    if(nargs < arity) throw arityError(this, arity, nargs, false, ii);

    // optimize: continue with coercion if current type is only an instance of new type
    FuncType tp = funcType();
    if(cc != null ? tp.eq(ft) : tp.instanceOf(ft)) return this;

    // create new compilation context and variable scope
    final VarScope vs = new VarScope(sc);
    final Var[] vars = new Var[arity];
    final Expr[] args = new Expr[arity];
    for(int a = 0; a < arity; a++) {
      vars[a] = vs.addNew(paramName(a), argTypes[a], true, qc, ii);
      args[a] = new VarRef(ii, vars[a]).optimize(cc);
    }

    try {
      if(cc != null) cc.pushScope(vs);

      // create new function call (will immediately be inlined/simplified when optimized)
      Expr body = new DynFuncCall(ii, sc, updating, false, this, args);
      if(cc != null) body = body.optimize(cc);

      // add type check if return types differ
      final SeqType dt = ft.declType;
      if(!tp.declType.instanceOf(dt)) {
        body = new TypeCheck(ii, body, dt, true);
        if(cc != null) body = body.optimize(cc);
      }

      // adopt type of optimized body if it is more specific than passed on type
      final SeqType bt = body.seqType();
      tp = cc != null && !bt.eq(dt) && bt.instanceOf(dt) ? FuncType.get(bt, argTypes) : ft;
      body.markTailCalls(null);
      return new FuncItem(ii, body, vars, annotations(), tp, sc, vs.stackSize(), funcName());
    } finally {
      if(cc != null) cc.removeScope();
    }
  }

  @Override
  public final boolean equals(final Object obj) {
    try {
      return obj instanceof FItem && deepEqual((FItem) obj, null);
    } catch(final QueryException ex) {
      Util.debug(ex);
      return false;
    }
  }
}
