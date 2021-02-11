package org.basex.query.func.user;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class UserPassword extends UserFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    qc.updates().add(new Password(toUser(0, qc), toString(1, qc), qc, info), qc);
    return Empty.VALUE;
  }

  /** Update primitive. */
  private static final class Password extends UserUpdate {
    /** Password. */
    private final String pw;

    /**
     * Constructor.
     * @param user user
     * @param pw password
     * @param qc query context
     * @param info input info
     */
    private Password(final User user, final String pw, final QueryContext qc,
        final InputInfo info) {
      super(UpdateType.USERPASSWORD, user, qc, info);
      this.pw = pw;
    }

    @Override
    public void apply() {
      user.password(pw);
    }

    @Override
    public String operation() {
      return "altered";
    }
  }
}
