package org.basex.query.func.user;

import static org.basex.query.QueryError.*;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.expr.path.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class UserUpdateInfo extends UserFn {
  /** Root node test. */
  private static final QNm Q_INFO = new QNm(UserText.INFO);
  /** Root node test. */
  private static final NodeTest T_INFO = new NodeTest(Q_INFO);

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANode node = toElem(exprs[0], qc);
    if(!T_INFO.eq(node)) throw ELM_X_X.get(info, Q_INFO.prefixId(), node);

    qc.updates().add(new UpdateInfo(node.deepCopy(qc.context.options, qc), qc, info), qc);
    return null;
  }

  /** Update primitive. */
  private static final class UpdateInfo extends UserPermUpdate {
    /** Node to be updated. */
    private final ANode node;

    /**
     * Constructor.
     * @param node info element
     * @param qc query context
     * @param info input info
     * @throws QueryException query exception
     */
    private UpdateInfo(final ANode node, final QueryContext qc, final InputInfo info)
        throws QueryException {
      super(UpdateType.USERINFO, qc.context.user(), null, new StringList(), qc, info);
      this.node = node;
    }

    @Override
    public void merge(final Update update) throws QueryException {
      throw USER_INFO_X.get(info, operation());
    }

    @Override
    public void apply() {
      users.info(node);
    }

    @Override
    public String operation() { return "updated"; }
  }
}
