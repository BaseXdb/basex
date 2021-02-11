package org.basex.query.func.user;

import static org.basex.core.users.UserText.*;

import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class UserInfo extends UserFn {
  @Override
  public ANode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANode node = exprs.length > 0 ? toUser(0, qc).info() : qc.context.users.info();
    return node == null ? new FElem(INFO) : node;
  }
}
