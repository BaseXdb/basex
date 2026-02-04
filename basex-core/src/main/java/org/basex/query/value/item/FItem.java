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
 * @author BaseX Team, BSD License
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
  public final boolean atomicEqual(final Item item) {
    throw Util.notExpected();
  }

  @Override
  public final int compare(final Item item, final Collation coll, final boolean transitive,
      final InputInfo ii) throws QueryException {
    throw Util.notExpected();
  }

  @Override
  public final FuncType funcType() {
    return type.funcType();
  }

  /**
   * Identity of this function.
   * @return identity string
   */
  public abstract String funcIdentity();

  @Override
  public Item materialize(final Predicate<Data> test, final InputInfo ii, final QueryContext qc)
      throws QueryException {
    throw BASEX_FUNCTION_X.get(info(ii), this);
  }

  @Override
  public boolean materialized(final Predicate<Data> test, final InputInfo ii)
      throws QueryException {
    throw BASEX_FUNCTION_X.get(info(ii), this);
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
  public FItem coerceTo(final FuncType ft, final QueryContext qc, final CompileContext cc,
      final InputInfo ii) throws QueryException {

    final InputInfo info = info(ii);
    final SeqType[] argTypes = ft.argTypes;
    final int arity = arity(), nargs = argTypes.length;
    if(nargs < arity) throw arityError(this, arity, nargs, false, info);

    // optimize: continue with coercion if current type is only an instance of new type
    if(type.eq(ft)) return this;

    // create new compilation context and variable scope
    final VarScope vs = new VarScope();
    final Var[] vars = new Var[nargs];
    final Expr[] args = new Expr[arity];
    for(int a = 0; a < arity; a++) {
      vars[a] = vs.addNew(paramName(a), argTypes[a], qc, info);
      args[a] = new VarRef(info, vars[a]).optimize(cc);
    }
    for(int a = arity; a < nargs; a++) {
      vars[a] = vs.addNew(QNm.EMPTY, argTypes[a], qc, info);
    }

    try {
      if(cc != null) cc.pushScope(vs);

      // create new function call (will immediately be inlined/simplified when optimized)
      Expr body = new DynFuncCall(info, updating(), false, this, args);
      if(cc != null) body = body.optimize(cc);

      // add type check if return types differ
      final SeqType dt = ft.declType;
      FuncType tp = funcType();
      if(!tp.declType.instanceOf(dt)) {
        body = new TypeCheck(info, body, dt);
        if(cc != null) body = body.optimize(cc);
      }

      // adopt type of optimized body if it is more specific than passed on type
      final SeqType bt = body.seqType();
      tp = cc != null && !bt.eq(dt) && bt.instanceOf(dt) ? FuncType.get(bt, argTypes) : ft;
      body.markTailCalls(null);
      return new FuncItem(info, body, vars, annotations(), tp, vs.stackSize(), funcName());
    } finally {
      if(cc != null) cc.removeScope();
    }
  }

  /**
   * Indicates if the function item is updating.
   * @return result of check
   */
  abstract boolean updating();

  @Override
  public final boolean equals(final Object obj) {
    try {
      return obj instanceof final FItem fitem && deepEqual(fitem, null);
    } catch(final QueryException ex) {
      Util.debug(ex);
      return false;
    }
  }
}
