package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;

/**
 * Backup function.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class BackupFn extends DbAccessFn {
  @Override
  public boolean accept(final ASTVisitor visitor) {
    return dataLock(arg(0), true, visitor) && visitAll(visitor, args());
  }

  /**
   * Evaluates an expression to the name of a backup.
   * @param expr expression
   * @param qc query context
   * @return name of backup
   * @throws QueryException query exception
   */
  protected final String toBackup(final Expr expr, final QueryContext qc) throws QueryException {
    final String name = toZeroString(expr, qc);
    if(name.isEmpty() || Databases.validName(name)) return name;
    throw DB_NAME_X.get(info, name);
  }
}
