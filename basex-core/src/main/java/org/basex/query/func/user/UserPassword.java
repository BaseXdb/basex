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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class UserPassword extends UserFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final User user = toUser(arg(0), qc);
    final String password = toString(arg(1), qc);
    qc.updates().add(new Password(user, password, qc, info), qc);
    return Empty.VALUE;
  }

  /** Update primitive. */
  private static final class Password extends UserUpdate {
    /** Password. */
    private final String password;

    /**
     * Constructor.
     * @param user user
     * @param password password
     * @param qc query context
     * @param info input info (can be {@code null})
     */
    private Password(final User user, final String password, final QueryContext qc,
        final InputInfo info) {
      super(UpdateType.USERPASSWORD, user, qc, info);
      this.password = password;
    }

    @Override
    public void apply() {
      user.password(password);
    }

    @Override
    public String operation() {
      return "altered";
    }
  }
}
