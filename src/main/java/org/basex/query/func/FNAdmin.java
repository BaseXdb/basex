package org.basex.query.func;

import org.basex.core.*;
import org.basex.data.*;
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
    final User[] users;
    if(expr.length != 0) {
      final Data data = data(0, ctx);
      users = data.meta.users.users(ctx.context.users);
    } else {
      users = ctx.context.users.users(null);
    }

    return new Iter() {
      int up = -1;

      @Override
      public Item next() throws QueryException {
        if(++up >= users.length) return null;
        final FElem elem = new FElem(Q_USER).add(Token.token(users[up].name));
        elem.add(Q_PERMISSION, Token.token(users[up].perm.toString()));
        return elem;
      }
    };
  }
}
