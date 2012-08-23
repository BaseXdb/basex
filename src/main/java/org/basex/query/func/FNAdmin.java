package org.basex.query.func;

import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.server.*;
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
  /** QName: user. */
  static final QNm Q_DATABASE = new QNm("database");
  /** QName: user. */
  static final QNm Q_SESSION = new QNm("session");
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
      case _ADMIN_USERS:    return users(ctx);
      case _ADMIN_SESSIONS: return sessions(ctx);
      default:              return super.iter(ctx);
    }
  }

  /**
   * Lists all registered users.
   * @param ctx query context
   * @return users
   * @throws QueryException query exception
   */
  private Iter users(final QueryContext ctx) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    for(final User u : expr.length == 0 ? ctx.context.users.users(null) :
      data(0, ctx).meta.users.users(ctx.context.users)) {
      vb.add(new FElem(Q_USER).add(u.name).add(Q_PERMISSION,
          u.perm.toString().toLowerCase(Locale.ENGLISH)));
    }
    return vb;
  }

  /**
   * Lists all open sessions.
   * @param ctx query context
   * @return users
   */
  private Iter sessions(final QueryContext ctx) {
    final ValueBuilder vb = new ValueBuilder();
    synchronized(ctx.context.sessions) {
      for(final ClientListener sp : ctx.context.sessions) {
        final String user = sp.context().user.name;
        final String addr = sp.address();
        final Data data = sp.context().data();
        final FElem elem = new FElem(Q_SESSION).add(addr).add(Q_USER, user);
        if(data != null) elem.add(Q_DATABASE, data.meta.name);
        vb.add(elem);
      }
    }
    return vb;
  }

  /*
   * Creates a new user. Needs to be implemented as updating function.
   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
  private Item createUser(final QueryContext ctx) throws QueryException {
    final String user = Token.string(checkStr(expr[0], ctx));
    final Item pw = checkItem(expr[1], ctx);
    final byte[] pass;
    if(pw instanceof AStr) {
      pass = pw.string(info);
    } else if(pw instanceof Bin) {
      pass = new Hex((Bin) pw, info).string(info);
    } else {
      throw STRBINTYPE.thrw(info, pw.type);
    }

    try {
      CreateUser.create(user, Token.string(pass), ctx.context);
    } catch(final BaseXException ex) {
      BXAD_USER.thrw(info, ex);
    }
    return null;
  }
   */
}
