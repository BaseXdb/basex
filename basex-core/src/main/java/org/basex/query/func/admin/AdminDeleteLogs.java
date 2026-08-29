package org.basex.query.func.admin;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.log.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class AdminDeleteLogs extends AdminFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String date = toString(arg(0), qc);

    final LogFile file = qc.context.log.file(date);
    if(file == null) throw ADMIN_DELETE_X.get(info, date);
    if(file.current()) throw ADMIN_TODAY.get(info, date);
    if(!file.delete()) throw ADMIN_DELETE_X.get(info, date);
    return Empty.VALUE;
  }
}
