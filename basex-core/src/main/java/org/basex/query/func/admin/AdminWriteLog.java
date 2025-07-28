package org.basex.query.func.admin;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.log.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class AdminWriteLog extends AdminFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String message = toString(arg(0), qc);
    final String type = toStringOrNull(arg(1), qc);
    if(type != null && type.matches(".*\\s.*$")) throw ADMIN_TYPE_X.get(info, type);

    qc.context.log.write(type != null ? type : LogType.INFO.toString(), message, null, qc.context);
    return Empty.VALUE;
  }
}
