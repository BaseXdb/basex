package org.basex.query.func.admin;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class AdminDeleteLogs extends AdminFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String date = toString(arg(0), qc);

    final LogFile file = qc.context.log.file(date);
    if(file == null) throw WHICHRES_X.get(info, date);
    if(file.current()) throw ADMIN_TODAY.get(info, date);
    if(!file.delete()) throw ADMIN_DELETE_X.get(info, date);
    return Empty.VALUE;
  }
}
