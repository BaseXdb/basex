package org.basex.query.func.user;

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
public final class UserPassword extends UserFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkAdmin(qc);
    qc.resources.updates().add(new Password(toUser(0, qc), toString(1, qc), ii, qc), qc);
    return null;
  }

  /** Update primitive. */
  private static final class Password extends UserUpdate {
    /** Password. */
    private final String pw;

    /**
     * Constructor.
     * @param user user
     * @param pw password
     * @param info input info
     * @param qc query context
     */
    private Password(final User user, final String pw, final InputInfo info,
        final QueryContext qc) {
      super(UpdateType.USERPASSWORD, user, null, qc, info);
      this.pw = pw;
    }

    @Override
    public void apply() {
      users.password(user, pw);
    }

    @Override
    public String operation() { return "altered"; }
  }
}
