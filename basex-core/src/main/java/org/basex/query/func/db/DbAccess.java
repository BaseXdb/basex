package org.basex.query.func.db;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class DbAccess extends StandardFunc {
  /**
   * Returns the specified expression as normalized database path.
   * Throws an exception if the path is invalid.
   * @param i index of argument
   * @param qc query context
   * @return normalized path
   * @throws QueryException query exception
   */
  final String path(final int i, final QueryContext qc) throws QueryException {
    return path(toToken(exprs[i], qc));
  }

  /**
   * Converts the specified path to a normalized database path.
   * Throws an exception if the path is invalid.
   * @param path input path
   * @return normalized path
   * @throws QueryException query exception
   */
  final String path(final byte[] path) throws QueryException {
    final String norm = MetaData.normPath(string(path));
    if(norm == null) throw RESINV_X.get(info, path);
    return norm;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return dataLock(visitor, 0) && super.accept(visitor);
  }
}
