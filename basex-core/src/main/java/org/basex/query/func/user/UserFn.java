package org.basex.query.func.user;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.server.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class UserFn extends StandardFunc {
  /** QName. */
  static final QNm Q_INFO = new QNm("info");

  /**
   * Evaluates an expression to database patterns.
   * @param expr expression (can be {@code Empty#UNDEFINED})
   * @param qc query context
   * @return patterns
   * @throws QueryException query exception
   */
  protected final StringList toPatterns(final Expr expr, final QueryContext qc)
      throws QueryException {
    final StringList patterns = new StringList();
    for(final Item item : expr.atomValue(qc, info)) {
      final String pattern = toString(item);
      if(!pattern.isEmpty() && !Databases.validPattern(pattern))
        throw USER_PATTERN_X.get(info, pattern);
      patterns.add(pattern);
    }
    if(patterns.isEmpty()) patterns.add("");
    return patterns;
  }

  /**
   * Evaluates an expression to a username.
   * @param expr expression
   * @param empty accept empty names
   * @param qc query context
   * @return username
   * @throws QueryException query exception
   */
  protected final String toName(final Expr expr, final boolean empty, final QueryContext qc)
      throws QueryException {
    return toName(expr, empty, USER_NAME_X, qc);
  }

  /**
   * Checks if the specified expression references an existing user.
   * @param expr expression
   * @param empty accept empty names
   * @param qc query context
   * @return user, or {@code null} if empty names are allowed
   * @throws QueryException query exception
   */
  protected final User toUser(final Expr expr, final boolean empty, final QueryContext qc)
      throws QueryException {
    final String name = toName(expr, empty, qc);
    if(name.isEmpty()) return null;

    if(!qc.user.name().equals(name)) checkPerm(qc, Perm.ADMIN);
    final User user = qc.context.users.get(name);
    if(user == null) throw USER_UNKNOWN_X.get(info, name);
    return user;
  }

  /**
   * Checks if the specified expression contains valid permissions.
   * @param expr expression (can be {@code Empty#UNDEFINED})
   * @param qc query context
   * @return permissions
   * @throws QueryException query exception
   */
  protected final ArrayList<Perm> toPermissions(final Expr expr, final QueryContext qc)
      throws QueryException {

    final ArrayList<Perm> perms = new ArrayList<>();
    for(final Item item : expr.atomValue(qc, info)) {
      final String perm = toString(item);
      final Perm p = Enums.get(Perm.class, perm);
      if(p == null) throw USER_PERMISSION_X.get(info, perm);
      perms.add(p);
    }
    if(perms.isEmpty()) perms.add(Perm.NONE);
    return perms;
  }

  /**
   * Ensures that no user with the specified name is logged in.
   * @param expr expression
   * @param qc query context
   * @return name
   * @throws QueryException query exception
   */
  protected final String toInactiveName(final Expr expr, final QueryContext qc)
      throws QueryException {
    final String name = toName(expr, false, qc);
    toInactiveUser(qc.context.users.get(name), qc);
    return name;
  }

  /**
   * Ensures that the specified user is not logged in.
   * @param expr expression
   * @param qc query context
   * @return user
   * @throws QueryException query exception
   */
  protected final User toInactiveUser(final Expr expr, final QueryContext qc)
      throws QueryException {
    return toInactiveUser(toUser(expr, false, qc), qc);
  }

  /**
   * Ensures that the specified user is not logged in.
   * @param user user (can be {@code null})
   * @param qc query context
   * @return specified user
   * @throws QueryException query exception
   */
  private User toInactiveUser(final User user, final QueryContext qc) throws QueryException {
    if(user != null) {
      final String name = user.name();
      for(final ClientListener cl : qc.context.sessions) {
        if(cl.context().user().name().equals(name)) throw USER_LOGGEDIN_X.get(info, name);
      }
    }
    return user;
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return (!hasUPD() || visitor.lock(Locking.USER)) && super.accept(visitor);
  }
}
