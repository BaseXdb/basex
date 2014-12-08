package org.basex.query.func.user;

import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class UserList extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    checkAdmin(qc);

    final boolean global = exprs.length == 0;
    final User[] users = users(global, qc);
    final TokenList tl = new TokenList(users.length);
    for(final User user : users) tl.add(user.name());
    return StrSeq.get(tl);
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return visitor.lock(DBLocking.ADMIN) &&
        (exprs.length == 0 || dataLock(visitor, 0)) && super.accept(visitor);
  }

  /**
   * Returns global or local users.
   * @param global global/local flag
   * @param qc query context
   * @return users
   * @throws QueryException query exception
   */
  public final User[] users(final boolean global, final QueryContext qc) throws QueryException {
    return global ? qc.context.users.users(null) :
      checkData(qc).meta.users.users(qc.context.users);
  }
}
