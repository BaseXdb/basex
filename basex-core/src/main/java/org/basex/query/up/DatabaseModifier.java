package org.basex.query.up;

import static org.basex.query.QueryError.*;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.query.up.primitives.name.*;
import org.basex.util.*;

/**
 * The database modifier holds all database updates during a snapshot.
 * Database permissions are checked to ensure that a user has enough privileges.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
final class DatabaseModifier extends ContextModifier {
  @Override
  void add(final Update update, final QueryContext qc) throws QueryException {
    // check permissions
    if(update instanceof NameUpdate) {
      if(!qc.context.perm(Perm.CREATE, null)) throw BASX_PERM_X.get(update.info(), Perm.CREATE);
    } else if(update instanceof DataUpdate) {
      if(!qc.context.perm(Perm.WRITE, ((DataUpdate) update).data().meta))
        throw BASX_PERM_X.get(update.info(), Perm.WRITE);
    } else if(update instanceof UserUpdate) {
      if(!qc.context.perm(Perm.ADMIN, null)) throw BASX_PERM_X.get(update.info(), Perm.ADMIN);
    } else {
      throw Util.notExpected("Unknown update type: " + update);
    }
    super.add(update, qc);
  }
}
