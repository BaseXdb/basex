package org.basex.query.func.user;

import static org.basex.query.QueryError.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class UserExists extends UserFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String name = toName(0, qc);
    if(!Databases.validName(name)) throw BXUS_NAME_X.get(info, name);

    final Users users = qc.context.users;
    return Bln.get(users.get(name) != null);
  }
}
