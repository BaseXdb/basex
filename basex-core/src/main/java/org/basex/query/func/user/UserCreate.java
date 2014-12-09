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
public final class UserCreate extends UserFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkAdmin(qc);
    final String name = checkSessions(toName(0, qc), qc);
    final String pw = Token.string(toToken(exprs[1], qc));
    final Perm perm = exprs.length > 2 ? toPerm(2, qc) : null;
    if(name.equals(UserText.ADMIN)) throw BXUS_ADMIN.get(info);

    qc.resources.updates().add(new Create(name, pw, perm, qc, ii), qc);
    return null;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(DBLocking.ADMIN) && super.accept(visitor);
  }

  /** Update primitive. */
  private static final class Create extends UserUpdate {
    /** Name. */
    private final String name;
    /** Password. */
    private final String pw;
    /** Permission. */
    private final Perm perm;

    /**
     * Constructor.
     * @param name user name
     * @param pw password
     * @param perm permission
     * @param qc query context
     * @param info input info
     */
    private Create(final String name, final String pw, final Perm perm, final QueryContext qc,
        final InputInfo info) {
      super(UpdateType.USERCREATE, null, null, qc, info);
      this.name = name;
      this.pw = pw;
      this.perm = perm;
    }

    @Override
    public void apply() {
      if(user != null) users.drop(user, null);
      users.create(name, pw, perm);
    }

    @Override
    public String name() {
      return name;
    }

    @Override
    public String operation() { return "created"; }
  }
}
