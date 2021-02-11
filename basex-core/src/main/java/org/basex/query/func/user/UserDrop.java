package org.basex.query.func.user;

import static org.basex.query.QueryError.*;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class UserDrop extends UserFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final User user = toInactiveUser(0, qc);
    final StringList patterns = toPatterns(1, qc);
    if(user.name().equals(UserText.ADMIN)) throw USER_ADMIN.get(info);
    qc.updates().add(new Drop(user, patterns, qc, info), qc);
    return Empty.VALUE;
  }

  /** Update primitive. */
  private static final class Drop extends UserPermUpdate {
    /**
     * Constructor.
     * @param user user
     * @param patterns database (optional)
     * @param qc query context
     * @param info input info
     * @throws QueryException query exception
     */
    private Drop(final User user, final StringList patterns, final QueryContext qc,
        final InputInfo info) throws QueryException {
      super(UpdateType.USERDROP, user, null, patterns, qc, info);
    }

    @Override
    public void apply() {
      boolean global = false;
      for(final String pattern : patterns) global |= pattern.isEmpty();
      if(global) {
        users.drop(user);
      } else {
        for(final String db : patterns) user.drop(db);
      }
    }

    @Override
    public String operation() {
      return "dropped";
    }
  }
}
