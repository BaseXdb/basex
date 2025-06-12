package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.up.primitives.name.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class DbCopy extends DbAccessFn {
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
    final String name = toName(arg(0), false, qc), newname = toName(arg(1), false, qc);
    if(name.equals(newname)) throw DB_CONFLICT4_X.get(info, name, newname);

    // source database does not exist
    checkPerm(qc, Perm.CREATE, name);
    if(!qc.context.soptions.dbExists(name)) throw DB_GET1_X.get(info, name);

    qc.updates().add(keep ? new DBCopy(name, newname, qc, info) :
      new DBAlter(name, newname, qc, info), qc);
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return dataLock(arg(1), false, visitor) && super.accept(visitor);
  }
}
