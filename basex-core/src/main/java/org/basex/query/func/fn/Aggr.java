package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.AtomType.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;

/**
 * Aggregation function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
abstract class Aggr extends StandardFunc {
  /**
   * Sums up the specified item(s).
   * @param iter iterator
   * @param it first item
   * @param avg calculate average
   * @return summed up item
   * @throws QueryException query exception
   */
  Item sum(final Iter iter, final Item it, final boolean avg) throws QueryException {
    Item rs = it.type.isUntyped() ? Dbl.get(it.string(info), info) : it;
    final boolean num = rs instanceof ANum, dtd = rs.type == DTD, ymd = rs.type == YMD;
    if(!num && (!(rs instanceof Dur) || rs.type == DUR)) throw SUM_X_X.get(info, rs.type, rs);

    int c = 1;
    for(Item i; (i = iter.next()) != null;) {
      if(i.type.isNumberOrUntyped()) {
        if(!num) throw SUMDUR_X_X.get(info, i.type, i);
      } else {
        if(num) throw SUMNUM_X_X.get(info, i.type, i);
        if(dtd && i.type != DTD || ymd && i.type != YMD) throw SUMDUR_X_X.get(info, i.type, i);
      }
      rs = Calc.PLUS.ev(info, rs, i);
      ++c;
    }
    return avg ? Calc.DIV.ev(info, rs, Int.get(c)) : rs;
  }
}
