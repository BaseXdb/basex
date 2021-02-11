package org.basex.query.func.admin;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.server.Log.LogType;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class AdminWriteLog extends AdminFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkAdmin(qc);

    final String msg = string(toToken(exprs[0], qc));
    final String type = exprs.length > 1 ? string(toToken(exprs[1], qc)) : LogType.INFO.toString();
    if(type.matches(".*\\s.*$")) throw ADMIN_TYPE_X.get(info, type);

    qc.context.log.write(type, msg, null, qc.context);
    return Empty.VALUE;
  }
}
