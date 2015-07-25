package org.basex.query.func.admin;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class AdminDeleteLogs extends AdminFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkAdmin(qc);

    final String date = Token.string(toToken(exprs[0], qc));
    final String cdate = Log.name(new Date());
    if(date.equals(cdate)) throw BXAD_TODAY.get(info, date + IO.LOGSUFFIX);

    final IOFile file = new IOFile(qc.context.log.dir(), date + IO.LOGSUFFIX);
    if(!file.exists()) throw WHICHRES_X.get(info, file);
    if(!file.delete()) throw BXAD_DELETE_X.get(info, date + IO.LOGSUFFIX);
    return null;
  }
}
