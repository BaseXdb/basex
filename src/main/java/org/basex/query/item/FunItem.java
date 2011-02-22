package org.basex.query.item;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Function item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
public class FunItem extends Item {

  /**
   * Constructor.
   * @param args function arguments
   * @param body function body
   * @param t function type
   */
  protected FunItem(final Var[] args, final Expr body, final FunType t) {
    super(t);
  }

  @Override
  public byte[] atom() {
    // TODO throw correct error
    return null;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return super.item(ctx, ii);
  }

  @Override
  @SuppressWarnings("unused")
  public boolean eq(final InputInfo ii, final Item it) throws QueryException {
    return false;
  }

}
