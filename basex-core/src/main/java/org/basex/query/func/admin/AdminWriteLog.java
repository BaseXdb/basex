package org.basex.query.func.admin;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.server.*;
import org.basex.server.Log.LogType;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class AdminWriteLog extends AdminFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkAdmin(qc);

    final String msg = Token.string(toToken(exprs[0], qc));
    final ClientListener cl = qc.context.listener;
    final String addr = cl == null ? Log.SERVER : cl.address();
    final User user = cl == null ? qc.context.user : cl.context().user;
    qc.context.log.write(addr, user, LogType.INFO, msg, null);
    return null;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(DBLocking.ADMIN) && super.accept(visitor);
  }
}
