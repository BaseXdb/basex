package org.basex.query.func.user;

import static org.basex.query.QueryError.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
abstract class UserFn extends StandardFunc {
  /**
   * Checks if the specified expression is a valid database name.
   * @param i expression index
   * @param qc query context
   * @return name of database
   * @throws QueryException query exception
   */
  protected final String toDB(final int i, final QueryContext qc) throws QueryException {
    final String name = toString(i, qc);
    if(!Databases.validName(name, true)) throw USER_PATTERN_X.get(info, name);
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
    final String name = toString(i, qc);
    if(!Databases.validName(name)) throw USER_NAME_X.get(info, name);
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
    if(user == null) throw USER_UNKNOWN_X.get(info, name);
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
    if(p == null) throw USER_PERMISSION_X.get(info, perm);
    return p;
  }

  /**
   * Checks if the specified expression is a valid password.
   * @param i expression index
   * @param qc query context
   * @return name of database
   * @throws QueryException query exception
   */
  protected final String toString(final int i, final QueryContext qc) throws QueryException {
    return Token.string(toToken(exprs[i], qc));
  }

  /**
   * Checks if the specified user is currently logged in.
   * @param i expression index
   * @param qc query context
   * @return name
   * @throws QueryException query exception
   */
  protected final String toSafeName(final int i, final QueryContext qc) throws QueryException {
    final String name = toName(i, qc);
    checkSafe(qc.context.users.get(name), qc);
    return name;
  }

  /**
   * Checks if the specified user is currently logged in.
   * @param i expression index
   * @param qc query context
   * @return user
   * @throws QueryException query exception
   */
  protected final User toSafeUser(final int i, final QueryContext qc) throws QueryException {
    return checkSafe(toUser(i, qc), qc);
  }

  /**
   * Checks if the specified user is currently logged in.
   * @param user user (can be {@code null})
   * @param qc query context
   * @return specified user
   * @throws QueryException query exception
   */
  private User checkSafe(final User user, final QueryContext qc) throws QueryException {
    if(user != null) {
      final String name = user.name();
      for(final ClientListener s : qc.context.sessions) {
        if(s.context().user().name().equals(name)) throw USER_LOGGEDIN_X.get(info, name);
      }
    }
    return user;
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return visitor.lock(DBLocking.ADMIN) && super.accept(visitor);
  }
}
