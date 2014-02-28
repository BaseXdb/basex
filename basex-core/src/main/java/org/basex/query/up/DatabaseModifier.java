package org.basex.query.up;

import static org.basex.query.util.Err.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;

/**
 * The database modifier holds all database updates during a snapshot.
 * Database permissions are checked to ensure that a user possesses enough
 * privileges to alter database contents.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
final class DatabaseModifier extends ContextModifier {
  @Override
  void add(final Operation o, final QueryContext ctx) throws QueryException {
    add(o);
    // check permissions
    if(o instanceof DBBackup || o instanceof DBCreate || o instanceof DBRestore ||
        o instanceof DBDrop) {
      if(!ctx.context.perm(Perm.CREATE, null)) throw BASX_PERM.get(o.getInfo(), Perm.CREATE);
    } else if(!ctx.context.perm(Perm.WRITE, o.getData().meta)) {
      throw BASX_PERM.get(o.getInfo(), Perm.WRITE);
    }
  }
}
