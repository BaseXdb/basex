package org.basex.query.value.item;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract super class for function items.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public abstract class FItem extends Item implements XQFunction {
  /**
   * Constructor.
   * @param t type
   */
  protected FItem(final FuncType t) {
    super(t);
  }

  @Override
  public Item invItem(final QueryContext ctx, final InputInfo ii, final Value... args)
      throws QueryException {
    return invValue(ctx, ii, args).item(ctx, ii);
  }

  /**
   * Coerces this function item to the given function type.
   * @param ft function type
   * @param ctx query context
   * @param ii input info
   * @return coerced item
   * @throws QueryException query exception
   */
  public abstract FItem coerceTo(final FuncType ft, final QueryContext ctx,
      final InputInfo ii) throws QueryException;

  @Override
  public final byte[] string(final InputInfo ii) throws QueryException {
    throw FIVALUE.thrw(ii, description());
  }

  @Override
  public final boolean eq(final InputInfo ii, final Item it) throws QueryException {
    throw FIEQ.thrw(ii, description());
  }

  @Override
  public Object toJava() throws QueryException {
    throw Util.notexpected();
  }

  @Override
  public abstract void plan(final FElem root);
}
