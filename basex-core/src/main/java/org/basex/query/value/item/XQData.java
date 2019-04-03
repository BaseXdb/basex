package org.basex.query.value.item;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function item with a known data structure (map, array).
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public abstract class XQData extends FItem {
  /**
   * Constructor.
   * @param type type
   */
  protected XQData(final FuncType type) {
    super(type, new AnnList());
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
  public final int stackFrameSize() {
    return 0;
  }

  @Override
  public final Expr inline(final Expr[] exprs, final CompileContext cc) {
    return null;
  }

  @Override
  public final boolean isVacuousBody() {
    return false;
  }

  @Override
  public final Value invValue(final QueryContext qc, final InputInfo ii, final Value... args)
      throws QueryException {
    final Item key = args[0].atomItem(qc, ii);
    if(key == Empty.VALUE) throw EMPTYFOUND.get(ii);
    return get(key, ii);
  }

  @Override
  public final Item invItem(final QueryContext qc, final InputInfo ii, final Value... args)
      throws QueryException {
    return invValue(qc, ii, args).item(qc, ii);
  }

  @Override
  public final boolean instanceOf(final Type tp) {
    return tp == AtomType.ITEM || tp instanceof FuncType && instanceOf((FuncType) tp, false);
  }

  @Override
  public final Value invokeValue(final QueryContext qc, final InputInfo ii, final Value... args)
      throws QueryException {
    return FuncCall.invoke(this, args, false, qc, ii);
  }

  @Override
  public final Item invokeItem(final QueryContext qc, final InputInfo ii, final Value... args)
      throws QueryException {
    return (Item) FuncCall.invoke(this, args, true, qc, ii);
  }

  @Override
  public final FItem coerceTo(final FuncType ft, final QueryContext qc, final InputInfo ii,
      final boolean optimize) throws QueryException {

    if(instanceOf(ft, true)) return this;
    throw typeError(this, ft, ii);
  }

  /**
   * Gets a value from this item.
   * @param key key to look for (must not be {@code null})
   * @param ii input info
   * @return bound value if found, the empty sequence {@code ()} otherwise
   * @throws QueryException query exception
   */
  public abstract Value get(Item key, InputInfo ii) throws QueryException;

  /**
   * Checks if this is an instance of the specified type.
   * @param ft type
   * @param coerce coerce value
   * @return result of check
   */
  protected abstract boolean instanceOf(FuncType ft, boolean coerce);

  /**
   * Returns a string representation of the item.
   * @param indent indent output
   * @param tb token builder
   * @param level current level
   * @param ii input info
   * @throws QueryException query exception
   */
  public abstract void string(boolean indent, TokenBuilder tb, int level, InputInfo ii)
      throws QueryException;

  @Override
  public final boolean equals(final Object obj) {
    // [CG] could be enhanced
    return this == obj;
  }
}
