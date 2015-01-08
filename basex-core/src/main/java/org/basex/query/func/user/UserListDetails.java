package org.basex.query.func.user;

import static org.basex.core.users.UserText.*;

import java.util.*;
import java.util.Map.Entry;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class UserListDetails extends UserList {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    checkAdmin(qc);
    final User u = exprs.length > 0 ? toUser(0, qc) : null;

    final ValueBuilder vb = new ValueBuilder();
    for(final User us : qc.context.users.users(null)) {
      if(u != null && u != us) continue;

      final String perm = us.perm(null).toString().toLowerCase(Locale.ENGLISH);
      final FElem user = new FElem(USER).add(NAME, us.name()).add(PERMISSION, perm);
      for(final Entry<Algorithm, EnumMap<Code, String>> codes : us.alg().entrySet()) {
        final FElem password = new FElem(PASSWORD).add(ALGORITHM, codes.getKey().toString());
        for(final Entry<Code, String> code : codes.getValue().entrySet()) {
          password.add(new FElem(code.getKey().toString()).add(code.getValue()));
        }
        user.add(password);
      }
      for(final Entry<String, Perm> local : us.locals().entrySet()) {
        user.add(new FElem(DATABASE).add(PATTERN, local.getKey()).
            add(PERMISSION, local.getValue().toString()));
      }
      vb.add(user);
    }
    return vb;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value();
  }
}
