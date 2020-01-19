package org.basex.query.func.db;

import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.index.*;
import org.basex.query.expr.path.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public class DbText extends DbAccess {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return valueAccess(qc).iter(qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return valueAccess(qc).value(qc);
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    return compileData(cc);
  }

  /**
   * Returns the index type (overwritten by implementing functions).
   * @return index type
   */
  IndexType type() {
    return IndexType.TEXT;
  }

  /**
   * Returns an index accessor.
   * @param qc query context
   * @return index accessor
   * @throws QueryException query exception
   */
  final ValueAccess valueAccess(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    final TokenSet set = new TokenSet();
    final Iter iter = exprs[1].iter(qc);
    for(Item item; (item = qc.next(iter)) != null;) set.put(toToken(item));
    return new ValueAccess(info, set, type(), null, new IndexStaticDb(info, data));
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
}
