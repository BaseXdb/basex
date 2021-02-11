package org.basex.query.func.db;

import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.expr.index.*;
import org.basex.query.expr.path.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class DbAttribute extends DbText {
  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    final ValueAccess va = valueAccess(data, qc);

    // no attribute specified: return iterator
    if(exprs.length <= 2) return va.iter(qc);

    // parse and compile the name test
    final QNm qName = new QNm(toToken(exprs[2], qc), sc);
    if(!qName.hasPrefix()) qName.uri(sc.ns.uri(EMPTY));

    // return empty sequence if test will yield no results
    final NameTest nt = new NameTest(qName, NamePart.FULL, NodeType.ATTRIBUTE, sc.elemNS);
    if(nt.noMatches(data)) return Empty.ITER;

    // wrap iterator with name test
    final Iter iter = va.iter(qc);
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
  public final Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  @Override
  IndexType type() {
    return IndexType.ATTRIBUTE;
  }
}
