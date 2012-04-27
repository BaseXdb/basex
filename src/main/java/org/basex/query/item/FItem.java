package org.basex.query.item;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.util.*;

/**
 * Abstract super class for function items.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public abstract class FItem extends Item {
  /**
   * Constructor.
   * @param t type
   */
  protected FItem(final Type t) {
    super(t);
  }

  /**
   * Number of arguments this function item takes.
   * @return function arity
   */
  public abstract int arity();

  /**
   * Name of this function, {@code null} means anonymous function.
   * @return name or {@code null}
   */
  public abstract QNm fName();

  /**
   * Invokes this function item with the given arguments.
   * @param ctx query context
   * @param ii input info
   * @param args arguments
   * @return resulting iterator
   * @throws QueryException query exception
   */
  public abstract Value invValue(final QueryContext ctx, final InputInfo ii,
      final Value... args) throws QueryException;

  /**
   * Invokes this function item with the given arguments.
   * @param ctx query context
   * @param ii input info
   * @param args arguments
   * @return resulting iterator
   * @throws QueryException query exception
   */
  public Iter invIter(final QueryContext ctx, final InputInfo ii, final Value... args)
      throws QueryException {
    return invValue(ctx, ii, args).iter();
  }

  /**
   * Invokes this function item with the given arguments.
   * @param ctx query context
   * @param ii input info
   * @param args arguments
   * @return resulting item
   * @throws QueryException query exception
   */
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
