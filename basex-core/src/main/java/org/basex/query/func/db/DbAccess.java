package org.basex.query.func.db;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.index.*;
import org.basex.query.expr.path.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
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
    final QNm qName = new QNm(toToken(exprs[a], qc), sc);
    if(!qName.hasPrefix()) qName.uri(sc.ns.uri(EMPTY));

    final NameTest nt = new NameTest(NodeType.ATT, qName, NamePart.FULL, sc.elemNS);
    // return empty sequence if test will yield no results
    if(!nt.optimize(qc.focus.value)) return Empty.ITER;

    // wrap iterator with name test
    final Iter iter = ia.iter(qc);
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        Item item;
        while((item = iter.next()) != null && !nt.matches(item)) qc.checkStop();
        return item;
      }
    };
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return dataLock(visitor, 0) && super.accept(visitor);
  }
}
