package org.basex.query.value.item;

import static org.basex.query.QueryError.*;

import java.util.function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
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
      final QueryContext qc, final InputInfo ii) throws QueryException {
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
  public Item materialize(final Predicate<Data> test, final boolean funcs, final InputInfo ii,
      final QueryContext qc) throws QueryException {
    if(!funcs) throw BASEX_FUNCTION_X.get(info(ii), this);
    TransferVisitor.check(this, info(ii));
    return this;
  }

  @Override
  public boolean materialized(final Predicate<Data> test, final boolean funcs, final InputInfo ii)
      throws QueryException {
    return funcs && TransferVisitor.dependency(this) == null;
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

    final SeqType[] argTypes = ft.argTypes;
    if(argTypes == null) return this;

    final InputInfo info = info(ii);
    final int arity = arity(), nargs = argTypes.length;
    if(nargs < arity) throw arityError(this, arity, nargs, false, info);

    // optimize: skip coercion if current type equals new type
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

    final Expr body = funcBody(vs, args, ft.declType, cc, info);

    // advertise the target type; keep the body type as refined return type for result typing
    final FuncType tp = cc != null ? ft.withRefinedType(body.seqType()) : ft;
    return new FuncItem(info, body, vars, annotations(), tp, vs.stackSize(), funcName());
  }

  /**
   * Creates the body of a function item that invokes this function with the specified arguments.
   * @param vs variable scope with the parameters of the new function item
   * @param args arguments
   * @param declType declared return type (can be {@code null})
   * @param cc compilation context ({@code null} during runtime)
   * @param ii input info (can be {@code null})
   * @return function body
   * @throws QueryException query exception
   */
  public final Expr funcBody(final VarScope vs, final Expr[] args, final SeqType declType,
      final CompileContext cc, final InputInfo ii) throws QueryException {

    try {
      if(cc != null) cc.pushScope(vs);

      // create new function call (will immediately be inlined/simplified when optimized)
      Expr body = new DynFuncCall(ii, updating(), false, this, args);
      if(cc != null) body = body.optimize(cc);

      // add type check if return types differ
      if(declType != null && !body.seqType().instanceOf(declType)) {
        body = new TypeCheck(ii, body, declType);
        if(cc != null) body = body.optimize(cc);
      }
      body.markTailCalls(null);
      return body;
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
