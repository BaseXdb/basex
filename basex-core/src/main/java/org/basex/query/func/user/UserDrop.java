package org.basex.query.func.user;

import static org.basex.query.QueryError.*;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class UserDrop extends UserFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkAdmin(qc);
    final User user = toSafeUser(0, qc);
    final String db = exprs.length > 1 ? toDB(1, qc) : "";
    if(user.name().equals(UserText.ADMIN)) throw USER_ADMIN.get(info);
    qc.resources.updates().add(new Drop(user, db, qc, ii), qc);
    return null;
  }

  /** Update primitive. */
  private static final class Drop extends UserUpdate {
    /**
     * Constructor.
     * @param user user
     * @param pattern database (optional)
     * @param qc query context
     * @param info input info
     */
    private Drop(final User user, final String pattern, final QueryContext qc,
        final InputInfo info) {
      super(UpdateType.USERDROP, user, pattern, qc, info);
    }

    @Override
    public void apply() {
      boolean global = false;
      for(final String db : patterns) global |= db == null;
      if(global) {
        users.drop(user, "");
      } else {
        for(final String db : patterns) users.drop(user, db);
      }
    }

    @Override
    public String operation() { return "dropped"; }
  }
}
