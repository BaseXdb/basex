package org.basex.query.func.user;

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
public final class UserDrop extends UserFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkAdmin(qc);
    qc.resources.updates().add(new Drop(toUser(exprs[0], qc), qc, ii), qc);
    return null;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(DBLocking.ADMIN) && super.accept(visitor);
  }

  /** Update primitive. */
  private static final class Drop extends UserUpdate {
    /**
     * Constructor.
     * @param user user
     * @param qc query context
     * @param info input info
     */
    private Drop(final User user, final QueryContext qc, final InputInfo info) {
      super(UpdateType.USERDROP, user, qc, info);
    }

    @Override
    public void apply() {
      users.drop(user);
    }

    @Override
    public String operation() { return "dropped"; }
  }
}
