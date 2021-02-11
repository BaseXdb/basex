package org.basex.query.func.user;

import static org.basex.query.QueryError.*;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class UserUpdateInfo extends UserFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANode node = toElem(exprs[0], qc);
    if(!T_INFO.matches(node)) throw ELM_X_X.get(info, Q_INFO.prefixId(), node);
    final User user = exprs.length > 1 ? toUser(1, qc) : null;

    qc.updates().add(new UpdateInfo(node.materialize(qc, true), user, qc, info), qc);
    return Empty.VALUE;
  }

  /** Update primitive. */
  private static final class UpdateInfo extends UserUpdate {
    /** Node to be updated. */
    private final ANode node;

    /**
     * Constructor.
     * @param user user ({@code null} if operation is global)
     * @param node info element
     * @param qc query context
     * @param info input info
     */
    private UpdateInfo(final ANode node, final User user, final QueryContext qc,
        final InputInfo info) {
      super(UpdateType.USERINFO, user, qc, info);
      this.node = node;
    }

    @Override
    public void merge(final Update update) throws QueryException {
      if(user != null) super.merge(update);
      else throw USER_INFO_X.get(info, operation());
    }

    @Override
    public void apply() {
      if(user != null) user.info(node);
      else users.info(node);
    }

    @Override
    public String operation() {
      return "updated";
    }
  }
}
