package org.basex.query.func.user;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UserInfo extends UserFn {
  @Override
  public XNode value(final QueryContext qc) throws QueryException {
    final User user = toUser(arg(0), true, qc);

    final XNode node = user != null ? user.info() : qc.context.users.info();
    return node != null ? node : FElem.build(Q_INFO).finish();
  }
}
