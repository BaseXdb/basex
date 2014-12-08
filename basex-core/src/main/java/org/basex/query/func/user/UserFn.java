package org.basex.query.func.user;

import static org.basex.query.QueryError.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class UserFn extends StandardFunc {
  /**
   * Checks if the specified expression is a valid user name.
   * @param ex expression to be evaluated
   * @param qc query context
   * @return token
   * @throws QueryException query exception
   */
  protected final String toName(final Expr ex, final QueryContext qc) throws QueryException {
    final String name = Token.string(toToken(ex, qc));
    if(!Databases.validName(name)) throw BXUS_NAME_X.get(info, name);
    return name;
  }

  /**
   * Checks if the specified expression references an existing user.
   * @param ex expression to be evaluated
   * @param qc query context
   * @return token
   * @throws QueryException query exception
   */
  protected final User toUser(final Expr ex, final QueryContext qc) throws QueryException {
    final String name = toName(ex, qc);
    final User user = qc.context.users.get(name);
    if(user == null) throw BXUS_WHICH_X.get(info, name);
    return user;
  }
}
