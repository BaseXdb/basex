package org.basex.query.func.db;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
abstract class DbAccess extends DbFn {
  /**
   * Returns the specified expression as normalized database path.
   * Throws an exception if the path is invalid.
   * @param i index of argument
   * @param qc query context
   * @return normalized path
   * @throws QueryException query exception
   */
  final String path(final int i, final QueryContext qc) throws QueryException {
    final String path = string(toToken(exprs[i], qc));
    final String norm = MetaData.normPath(path);
    if(norm == null) throw RESINV_X.get(info, path);
    return norm;
  }

  /**
   * Performs the attribute function.
   * @param ia index access
   * @param qc query context
   * @param a index of attribute argument
   * @return iterator
   * @throws QueryException query exception
   */
  final Iter attribute(final IndexAccess ia, final QueryContext qc, final int a)
      throws QueryException {

    // no attribute specified
    if(exprs.length <= a) return ia.iter(qc);

    // parse and compile the name test
    final QNm nm = new QNm(toToken(exprs[a], qc), sc);
    if(!nm.hasPrefix()) nm.uri(sc.ns.uri(Token.EMPTY));

    final NameTest nt = new NameTest(nm, NameTest.Kind.URI_NAME, true, sc.elemNS);
    // return empty sequence if test will yield no results
    if(!nt.optimize(qc)) return Empty.ITER;

    // wrap iterator with name test
    return new NodeIter() {
      final NodeIter ir = ia.iter(qc);
      @Override
      public ANode next() throws QueryException {
        ANode n;
        while((n = ir.next()) != null && !nt.eq(n));
        return n;
      }
    };
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return dataLock(visitor, 0) && super.accept(visitor);
  }
}
