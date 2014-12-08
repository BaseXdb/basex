package org.basex.query.func.user;

import static org.basex.query.QueryError.*;

import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.query.util.*;
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
    final User user = toUser(exprs[0], qc);
    final String perm = Token.string(toToken(exprs[1], qc));

    if(user.name().equals(UserText.ADMIN)) throw BXUS_ADMIN.get(info);

    final Perm p = Perm.get(perm);
    if(p == null) throw BXUS_PERM_X.get(info, perm);

    qc.resources.updates().add(new Grant(user, p, qc, ii), qc);
    return null;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(DBLocking.ADMIN) && super.accept(visitor);
  }

  /** Update primitive. */
  private static final class Grant extends UserUpdate {
    /** Permission. */
    private final Perm perm;

    /**
     * Constructor.
     * @param user user
     * @param perm permission
     * @param qc query context
     * @param info input info
     */
    private Grant(final User user, final Perm perm, final QueryContext qc, final InputInfo info) {
      super(UpdateType.USERGRANT, user, qc, info);
      this.perm = perm;
    }

    @Override
    public void apply() {
      users.perm(user, perm);
    }

    @Override
    public String operation() { return "altered"; }
  }
}
