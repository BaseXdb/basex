package org.basex.query.func.user;

import static org.basex.core.users.UserText.*;

import java.util.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class UserListDetails extends UserList {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Context ctx = qc.context;
    final User u = exprs.length > 0 ? toUser(0, qc) : null;
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final User us : u != null ? Collections.singletonList(u) : ctx.users.users(null, ctx)) {
      final String perm = us.perm((String) null).toString();
      final FElem user = new FElem(USER).add(NAME, us.name()).add(PERMISSION, perm);
      us.alg().forEach((key, value) -> {
        final FElem password = new FElem(PASSWORD).add(ALGORITHM, key.toString());
        value.forEach((k, v) -> password.add(new FElem(k.toString()).add(v)));
        user.add(password);
      });
      us.locals().forEach((key, value) -> user.add(
          new FElem(DATABASE).add(PATTERN, key).add(PERMISSION, value.toString())));
      vb.add(user);
    }
    return vb.value();
  }
}
