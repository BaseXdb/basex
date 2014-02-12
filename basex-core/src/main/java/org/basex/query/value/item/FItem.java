package org.basex.query.value.item;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract super class for function items.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public abstract class FItem extends Item implements XQFunction {
  /** Annotations. */
  final Ann ann;

  /**
   * Constructor.
   * @param t type
   * @param annotations this function item's annotations
   */
  protected FItem(final FuncType t, final Ann annotations) {
    super(t);
    ann = annotations;
  }

  @Override
  public final Ann annotations() {
    return ann;
  }

  @Override
  public final Value invokeValue(final QueryContext ctx, final InputInfo ii, final Value... args)
      throws QueryException {
    return FuncCall.value(this, args, ctx, ii);
  }

  @Override
  public final Item invokeItem(final QueryContext ctx, final InputInfo ii, final Value... args)
      throws QueryException {
    return FuncCall.item(this, args, ctx, ii);
  }

  /**
   * Coerces this function item to the given function type.
   * @param ft function type
   * @param ctx query context
   * @param ii input info
   * @param opt if the result should be optimized
   * @return coerced item
   * @throws QueryException query exception
   */
  public abstract FItem coerceTo(final FuncType ft, final QueryContext ctx, final InputInfo ii,
      boolean opt) throws QueryException;

  @Override
  public final byte[] string(final InputInfo ii) throws QueryException {
    throw FIVALUE.get(ii, type);
  }

  @Override
  public final boolean eq(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    throw FIEQ.get(ii, type);
  }

  @Override
  public abstract void plan(final FElem root);
}
