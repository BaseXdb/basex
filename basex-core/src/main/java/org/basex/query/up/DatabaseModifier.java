package org.basex.query.up;

import static org.basex.query.QueryError.*;

import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.query.up.primitives.name.*;

/**
 * The database modifier holds all database updates during a snapshot.
 * Database permissions are checked to ensure that a user has enough privileges.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
final class DatabaseModifier extends ContextModifier {
  @Override
  public void addData(final Data data) {
    // ignore data
  }

  @Override
  synchronized void add(final Update update, final QueryContext qc) throws QueryException {
    // check permissions
    if(update instanceof NameUpdate) {
      if(!qc.context.perm(Perm.CREATE, ((NameUpdate) update).name()))
        throw BASEX_PERMISSION_X.get(update.info(), Perm.CREATE);
    } else if(update instanceof DataUpdate) {
      if(!qc.context.perm(Perm.WRITE, ((DataUpdate) update).data().meta.name))
        throw BASEX_PERMISSION_X.get(update.info(), Perm.WRITE);
    }
    super.add(update, qc);
  }
}
