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
public final class UserGrant extends UserFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkAdmin(qc);
    final User user = toSafeUser(0, qc);
    final Perm perm = toPerm(1, qc);
    final String db = exprs.length > 2 ? toDB(2, qc) : "";
    if(user.name().equals(UserText.ADMIN)) throw USER_ADMIN.get(info);
    if(!db.isEmpty() && (perm == Perm.CREATE || perm == Perm.ADMIN))
      throw USER_LOCAL.get(info);

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
     * @param pattern pattern
     * @param qc query context
     * @param info input info
     */
    private Grant(final User user, final Perm perm, final String pattern, final QueryContext qc,
        final InputInfo info) {
      super(UpdateType.USERGRANT, user, pattern, qc, info);
      this.perm = perm;
    }

    @Override
    public void apply() {
      for(final String db : patterns) Users.perm(user, perm, db);
    }

    @Override
    public String operation() { return "altered"; }
  }
}
