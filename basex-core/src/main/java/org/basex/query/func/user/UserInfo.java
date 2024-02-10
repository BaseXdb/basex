package org.basex.query.func.user;

import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class UserInfo extends UserFn {
  @Override
  public ANode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANode node = defined(0) ? toUser(arg(0), qc).info() : qc.context.users.info();
    return node == null ? FElem.build(Q_INFO).finish() : node;
  }
}
