package org.basex.query.func.user;

import java.util.*;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class UserList extends UserFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final TokenList tl = new TokenList();
    for(final User user : users(qc)) tl.add(user.name());
    return StrSeq.get(tl);
  }

  /**
   * Returns all users that may be accessed by the current user and code.
   * @param qc query context
   * @return users
   */
  final ArrayList<User> users(final QueryContext qc) {
    return qc.context.users.users(null, user -> accessible(user, qc));
  }
}
