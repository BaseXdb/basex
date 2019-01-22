package org.basex.query.func.user;

import static org.basex.core.users.UserText.*;

import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class UserInfo extends UserFn {
  @Override
  public ANode item(final QueryContext qc, final InputInfo ii) {
    final ANode node = qc.context.users.info();
    return node == null ? new FElem(INFO) : node;
  }
}
