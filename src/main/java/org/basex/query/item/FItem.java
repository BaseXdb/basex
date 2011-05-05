package org.basex.query.item;

import static org.basex.query.util.Err.*;

import java.io.IOException;

import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Abstract super class for function items.
 *
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
  public Iter invIter(final QueryContext ctx, final InputInfo ii,
      final Value... args) throws QueryException {
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
  public Item invItem(final QueryContext ctx, final InputInfo ii,
      final Value... args) throws QueryException {
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
  public abstract FItem coerceTo(final FunType ft, final QueryContext ctx,
      final InputInfo ii) throws QueryException;

  @Override
  public byte[] atom(final InputInfo ii) throws QueryException {
    throw NOTYP.thrw(ii, desc());
  }

  @Override
  public boolean eq(final InputInfo ii, final Item it) throws QueryException {
    throw FNEQ.thrw(ii, desc());
  }

  @Override
  public Object toJava() {
    throw Util.notexpected();
  }

  @Override
  public abstract void plan(Serializer ser) throws IOException;
}
