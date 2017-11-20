package org.basex.query.func.admin;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.server.*;
import org.basex.server.Log.LogType;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class AdminWriteLog extends AdminFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkAdmin(qc);

    final String msg = string(toToken(exprs[0], qc));
    final String type = exprs.length > 1 ? string(toToken(exprs[1], qc)) : LogType.INFO.toString();
    if(!type.matches("^[A-Z]+$")) throw ADMIN_TYPE_X.get(info, type);

    final ClientInfo ci = qc.context.client;
    final String addr = ci == null ? Log.SERVER : ci.address();
    final String user = ci == null ? qc.context.user().name() : ci.user();
    qc.context.log.write(addr, user, type, msg, null);
    return null;
  }
}
