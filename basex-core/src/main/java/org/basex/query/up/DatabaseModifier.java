package org.basex.query.up;

import static org.basex.query.util.Err.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;

/**
 * The database modifier holds all database updates during a snapshot.
 * Database permissions are checked to ensure that a user has enough privileges.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
final class DatabaseModifier extends ContextModifier {
  @Override
  void add(final Update up, final QueryContext qc) throws QueryException {
    // check permissions
    if(up instanceof NameUpdate) {
      if(!qc.context.perm(Perm.CREATE, null)) throw BASX_PERM.get(up.info(), Perm.CREATE);
    } else if(!qc.context.perm(Perm.WRITE, ((DataUpdate) up).data().meta)) {
      throw BASX_PERM.get(up.info(), Perm.WRITE);
    }
    add(up);
  }
}
