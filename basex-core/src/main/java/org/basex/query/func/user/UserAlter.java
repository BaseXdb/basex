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
public final class UserAlter extends UserFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkAdmin(qc);
    final User user = toSafeUser(0, qc);
    final String name = user.name(), newname = toSafeName(1, qc);
    if(Strings.eq(UserText.ADMIN, name, newname)) throw USER_ADMIN.get(info);
    if(Strings.eq(name, newname)) throw USER_EQUAL_X.get(info, name);

    qc.resources.updates().add(new Alter(user, newname, qc, ii), qc);
    return null;
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
