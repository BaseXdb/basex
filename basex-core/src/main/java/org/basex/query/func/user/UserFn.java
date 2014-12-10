package org.basex.query.func.user;

import static org.basex.query.QueryError.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class UserFn extends StandardFunc {
  /**
   * Checks if the specified expression is a valid database name.
   * @param i expression index
   * @param qc query context
   * @return name of database
   * @throws QueryException query exception
   */
  protected final String toDB(final int i, final QueryContext qc) throws QueryException {
    final String name = Token.string(toToken(exprs[i], qc));
    if(!Databases.validName(name, true)) throw BXUS_DB_X.get(info, name);
    return name;
  }

  /**
   * Checks if the specified expression is a valid user name.
   * @param i expression index
   * @param qc query context
   * @return token
   * @throws QueryException query exception
   */
  protected final String toName(final int i, final QueryContext qc) throws QueryException {
    final String name = Token.string(toToken(exprs[i], qc));
    if(!Databases.validName(name)) throw BXUS_NAME_X.get(info, name);
    return name;
  }

  /**
   * Checks if the specified expression references an existing user.
   * @param i expression index
   * @param qc query context
   * @return token
   * @throws QueryException query exception
   */
  protected final User toUser(final int i, final QueryContext qc) throws QueryException {
    final String name = toName(i, qc);
    final User user = qc.context.users.get(name);
    if(user == null) throw BXUS_WHICH_X.get(info, name);
    return user;
  }

  /**
   * Checks if the specified expression provides an existing permission.
   * @param i expression index
   * @param qc query context
   * @return token
   * @throws QueryException query exception
   */
  protected final Perm toPerm(final int i, final QueryContext qc) throws QueryException {
    final String perm = Token.string(toToken(exprs[i], qc));
    final Perm p = Perm.get(perm);
    if(p == null) throw BXUS_PERM_X.get(info, perm);
    return p;
  }

  /**
   * Checks if the specified user is currently logged in.
   * @param name name of user
   * @param qc query context
   * @return name
   * @throws QueryException query exception
   */
  protected final String checkSessions(final String name, final QueryContext qc)
      throws QueryException {
    final User user = qc.context.users.get(name);
    if(user != null) checkSessions(user, qc);
    return name;
  }

  /**
   * Checks if the specified user is currently logged in.
   * @param user user
   * @param qc query context
   * @return user
   * @throws QueryException query exception
   */
  protected final User checkSessions(final User user, final QueryContext qc) throws QueryException {
    final String name = user.name();
    for(final ClientListener s : qc.context.sessions) {
      if(s.context().user().name().equals(name)) throw BXUS_SESSION_X.get(info, name);
    }
    return user;
  }
}
