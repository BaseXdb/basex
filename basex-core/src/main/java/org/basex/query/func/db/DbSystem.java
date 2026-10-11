package org.basex.query.func.db;

import org.basex.core.cmd.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbSystem extends StandardFunc {
  @Override
  public FNode value(final QueryContext qc) {
    final boolean admin = qc.has(Perm.ADMIN, null, info);
    return DbInfo.node(DbAccessFn.Q_SYSTEM, Info.info(qc.context, admin), qc);
  }
}
