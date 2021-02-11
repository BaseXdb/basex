package org.basex.query.func.user;

import static org.basex.query.QueryError.*;

import java.util.*;

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
public final class UserGrant extends UserFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkAdmin(qc);
    final User user = toInactiveUser(0, qc);
    final ArrayList<Perm> perms = toPerms(1, qc);
    final StringList patterns = toPatterns(2, qc);

    if(user.name().equals(UserText.ADMIN)) throw USER_ADMIN.get(info);
    final int ps = perms.size();
    for(int p = 0; p < ps; p++) {
      if(!patterns.get(p).isEmpty() && (perms.get(p) == Perm.CREATE || perms.get(p) == Perm.ADMIN))
        throw USER_LOCAL.get(info);
    }

    qc.updates().add(new Grant(user, perms, patterns, qc, info), qc);
    return Empty.VALUE;
  }

  /** Update primitive. */
  private static final class Grant extends UserPermUpdate {
    /**
     * Constructor.
     * @param user user
     * @param perms permissions
     * @param patterns patterns
     * @param qc query context
     * @param info input info
     * @throws QueryException query exception
     */
    private Grant(final User user, final ArrayList<Perm> perms, final StringList patterns,
        final QueryContext qc, final InputInfo info) throws QueryException {
      super(UpdateType.USERGRANT, user, perms, patterns, qc, info);
    }

    @Override
    public void apply() {
      grant();
    }

    @Override
    public String operation() {
      return "altered";
    }
  }
}
