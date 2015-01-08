package org.basex.query.func.user;

import java.util.*;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class UserList extends UserFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    checkAdmin(qc);

    final ArrayList<User> users = qc.context.users.users(null);
    final TokenList tl = new TokenList(users.size());
    for(final User user : users) tl.add(user.name());
    return StrSeq.get(tl);
  }
}
