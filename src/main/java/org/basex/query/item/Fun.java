package org.basex.query.item;

import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.iter.ValueIter;
import org.basex.util.InputInfo;

/**
 * Function item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Leonard Woerteler
 */
public abstract class Fun extends Item {

  /**
   * Constructor.
   */
  protected Fun() {
    super(FunType.instance(null, null));
  }

  @Override
  public final byte[] atom() {
    throw new RuntimeException("Functions don't habe atomic values.");
  }

  @Override
  public final ValueIter iter() {
    throw new RuntimeException("Functions can't be evaluated");
  }

  /**
   * Applies this function item to the given arguments.
   * @param args arguments
   * @return result value
   * @throws QueryException query exception
   */
  public abstract Value apply(final Expr[] args) throws QueryException;

  @Override
  public boolean eq(final InputInfo ii, final Item it) {
    return it == this;
  }
}
