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
public final class UserAlter extends UserFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkAdmin(qc);
    final User user = checkSessions(toUser(0, qc), qc);
    final String newname = checkSessions(toName(1, qc), qc);
    if(Strings.eq(UserText.ADMIN, user.name(), newname)) throw BXUS_ADMIN.get(info);

    qc.resources.updates().add(new Alter(user, newname, qc, ii), qc);
    return null;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(DBLocking.ADMIN) && super.accept(visitor);
  }

  /** Update primitive. */
  private static final class Alter extends UserUpdate {
    /** New name. */
    private final String newname;

    /**
     * Constructor.
     * @param user user
     * @param newname new name
     * @param qc query context
     * @param info input info
     */
    private Alter(final User user, final String newname, final QueryContext qc,
        final InputInfo info) {
      super(UpdateType.USERALTER, user, null, qc, info);
      this.newname = newname;
    }

    @Override
    public void apply() {
      final User old = users.get(newname);
      if(old != null) users.drop(old, null);
      users.alter(user, newname);
    }

    @Override
    public String operation() { return "altered"; }
  }
}
