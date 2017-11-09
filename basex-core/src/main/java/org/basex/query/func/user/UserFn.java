package org.basex.query.func.user;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.server.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
abstract class UserFn extends StandardFunc {
  /**
   * Checks if the specified expression contains valid database patterns.
   * @param i expression index
   * @param qc query context
   * @return patterns
   * @throws QueryException query exception
   */
  protected final StringList toPatterns(final int i, final QueryContext qc) throws QueryException {
    final StringList patterns = new StringList();
    if(exprs.length > i) {
      final Iter iter = qc.iter(exprs[i]);
      for(Item it; (it = iter.next()) != null;) {
        qc.checkStop();
        final String pattern = Token.string(toToken(it));
        if(!pattern.isEmpty() && !Databases.validName(pattern, true))
          throw USER_PATTERN_X.get(info, pattern);
        patterns.add(pattern);
      }
    } else {
      patterns.add("");
    }
    return patterns;
  }

  /**
   * Checks if the specified expression is a valid user name.
   * @param i expression index
   * @param qc query context
   * @return name
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
   * @return user
   * @throws QueryException query exception
   */
  protected final User toUser(final int i, final QueryContext qc) throws QueryException {
    final String name = toName(i, qc);
    final User user = qc.context.users.get(name);
    if(user != qc.context.user()) checkAdmin(qc);
    if(user == null) throw USER_UNKNOWN_X.get(info, name);
    return user;
  }

  /**
   * Checks if the specified expression contains valid permissions.
   * @param i expression index
   * @param qc query context
   * @return permissions
   * @throws QueryException query exception
   */
  protected final ArrayList<Perm> toPerms(final int i, final QueryContext qc)
      throws QueryException {

    final ArrayList<Perm> perms = new ArrayList<>();
    if(exprs.length > i) {
      final Iter iter = qc.iter(exprs[i]);
      for(Item it; (it = iter.next()) != null;) {
        qc.checkStop();
        final String perm = Token.string(toToken(it));
        final Perm p = Perm.get(perm);
        if(p == null) throw USER_PERMISSION_X.get(info, perm);
        perms.add(p);
      }
    } else {
      perms.add(Perm.NONE);
    }
    return perms;
  }

  /**
   * Checks if the specified expression is a string.
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
   * Checks if the specified user is currently not logged in.
   * @param i expression index
   * @param qc query context
   * @return user
   * @throws QueryException query exception
   */
  protected final User toSafeUser(final int i, final QueryContext qc) throws QueryException {
    return checkSafe(toUser(i, qc), qc);
  }

  /**
   * Checks if the specified user is currently not logged in.
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
    return (!sig.has(Flag.UPD) || visitor.lock(Locking.USER)) && super.accept(visitor);
  }
}
