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
 * @author BaseX Team, BSD License
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
    final User user = qc.context.user();
    if(update instanceof NameUpdate) {
      if(!user.has(Perm.CREATE, ((NameUpdate) update).name()))
        throw BASEX_PERMISSION_X.get(update.info(), Perm.CREATE);
    } else if(update instanceof DataUpdate) {
      if(!user.has(Perm.WRITE, ((DataUpdate) update).data().meta.name))
        throw BASEX_PERMISSION_X.get(update.info(), Perm.WRITE);
    }
    super.add(update, qc);
  }
}
