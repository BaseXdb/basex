package org.basex.query.func;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Admin functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNAdmin extends StandardFunc {
  /** QName: user. */
  static final QNm Q_USER = new QNm("user");
  /** QName: permission. */
  static final QNm Q_PERMISSION = new QNm("permission");

  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNAdmin(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    checkAdmin(ctx);
    switch(sig) {
      case _ADMIN_USERS: return users(ctx);
      default:           return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    checkAdmin(ctx);
    switch(sig) {
      default: return super.item(ctx, ii);
    }
  }

  /**
   * Lists all registered users.
   * @param ctx query context
   * @return users
   * @throws QueryException query exception
   */
  private Iter users(final QueryContext ctx) throws QueryException {
    final User[] users = expr.length == 0 ? ctx.context.users.users(null) :
      data(0, ctx).meta.users.users(ctx.context.users);

    return new Iter() {
      int p = -1;
      @Override
      public Item next() throws QueryException {
        return ++p >= users.length ? null :
          new FElem(Q_USER).add(Token.token(users[p].name)).
          add(Q_PERMISSION, Token.token(users[p].perm.toString()));
      }
    };
  }
}
