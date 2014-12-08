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

    final ValueBuilder vb = new ValueBuilder();
    final boolean global = exprs.length == 0;
    for(final User us : users(global, qc)) {
      final String perm = us.perm().toString().toLowerCase(Locale.ENGLISH);
      final FElem user = new FElem(USER).add(NAME, us.name()).add(PERMISSION, perm);
      if(global) {
        for(final Entry<Algorithm, EnumMap<Code, String>> codes : us.alg().entrySet()) {
          final FElem password = new FElem(PASSWORD).add(ALGORITHM, codes.getKey().toString());
          for(final Entry<Code, String> code : codes.getValue().entrySet()) {
            password.add(new FElem(code.getKey().toString()).add(code.getValue()));
          }
          user.add(password);
        }
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
