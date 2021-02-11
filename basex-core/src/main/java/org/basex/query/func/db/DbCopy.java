package org.basex.query.func.db;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.up.primitives.name.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class DbCopy extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    copy(qc, true);
    return Empty.VALUE;
  }

  /**
   * Performs the copy function.
   * @param qc query context
   * @param keep keep copied database
   * @throws QueryException query exception
   */
  final void copy(final QueryContext qc, final boolean keep) throws QueryException {
    final String name = string(toToken(exprs[0], qc));
    final String newname = string(toToken(exprs[1], qc));

    if(!Databases.validName(name)) throw DB_NAME_X.get(info, name);
    if(!Databases.validName(newname)) throw DB_NAME_X.get(info, newname);

    // source database does not exist
    if(!qc.context.soptions.dbExists(name)) throw DB_OPEN1_X.get(info, name);
    if(name.equals(newname)) throw DB_CONFLICT4_X.get(info, name, newname);

    qc.updates().add(keep ? new DBCopy(name, newname, qc, info) :
      new DBAlter(name, newname, qc, info), qc);
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return dataLock(visitor, 0) && dataLock(visitor, 1) && super.accept(visitor);
  }
}
