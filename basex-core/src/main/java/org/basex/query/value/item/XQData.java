package org.basex.query.value.item;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Function item with a known data structure (map, array).
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public abstract class XQData extends FItem {
  /**
   * Constructor.
   * @param type function type
   */
  protected XQData(final Type type) {
    super(type);
  }

  @Override
  public final AnnList annotations() {
    return AnnList.EMPTY;
  }

  @Override
  public final byte[] string(final InputInfo ii) throws QueryException {
    throw FIATOMIZE_X.get(ii, this);
  }

  @Override
  public final int arity() {
    return 1;
  }

  @Override
  public final QNm funcName() {
    return null;
  }

  @Override
  public final Expr inline(final Expr[] exprs, final CompileContext cc) {
    return null;
  }

  @Override
  public final boolean vacuousBody() {
    return false;
  }

  @Override
  public final Value invokeInternal(final QueryContext qc, final InputInfo ii, final Value[] args)
      throws QueryException {
    final Item key = args[0].atomItem(qc, ii);
    if(key.isEmpty()) throw EMPTYFOUND.get(ii);
    return get(key, ii);
  }

  @Override
  public final int stackFrameSize() {
    return 0;
  }

  @Override
  public final FItem coerceTo(final FuncType ft, final QueryContext qc, final CompileContext cc,
      final InputInfo ii) throws QueryException {
    if(instanceOf(ft)) return this;

    // create a coerced function:
    //    function($key as ft.argTypes[0]) as ft.declType {XQData.this ? $key coerce to ft.declType}

    final StaticContext sc = new StaticContext(qc);
    final Var[] params = { new VarScope(sc).addNew(paramName(0), ft.argTypes[0], true, qc, ii)};
    final VarRef param = new VarRef(ii, params[0]);
    final Lookup lookup = new Lookup(ii, this, param);
    final TypeCheck check = new TypeCheck(ii, lookup, ft.declType, true);
    final FItem fItem = new FuncItem(ii, check, params, annotations(), ft, sc, params.length, null);
    if(ft.argTypes.length != 1) throw arityError(fItem, 1, ft.argTypes.length, true, ii);
    return fItem;
  }

  /**
   * Gets a value from this item.
   * @param key key to look for (must not be {@code null})
   * @param info input info (can be {@code null})
   * @return bound value if found, the empty sequence {@code ()} otherwise
   * @throws QueryException query exception
   */
  public abstract Value get(Item key, InputInfo info) throws QueryException;

  /**
   * Returns a string representation of the item.
   * @param indent indent output
   * @param tb token builder
   * @param level current level
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public abstract void string(boolean indent, TokenBuilder tb, int level, InputInfo info)
      throws QueryException;
}
