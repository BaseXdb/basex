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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class UserGrant extends UserFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkAdmin(qc);
    final User user = checkSessions(toUser(0, qc), qc);
    final Perm perm = toPerm(1, qc);
    final String db = exprs.length > 2 ? toDB(2, qc) : null;
    if(user.name().equals(UserText.ADMIN)) throw BXUS_ADMIN.get(info);
    if(db != null && (perm == Perm.CREATE || perm == Perm.ADMIN))
      throw BXUS_LOCAL_X.get(info);

    qc.resources.updates().add(new Grant(user, perm, db, qc, ii), qc);
    return null;
  }

  /** Update primitive. */
  private static final class Grant extends UserUpdate {
    /** Permission. */
    private final Perm perm;

    /**
     * Constructor.
     * @param user user
     * @param perm permission
     * @param db database
     * @param qc query context
     * @param info input info
     */
    private Grant(final User user, final Perm perm, final String db, final QueryContext qc,
        final InputInfo info) {
      super(UpdateType.USERGRANT, user, db, qc, info);
      this.perm = perm;
    }

    @Override
    public void apply() {
      for(final String db : databases) users.perm(user, perm, db);
    }

    @Override
    public String operation() { return "altered"; }
  }
}
