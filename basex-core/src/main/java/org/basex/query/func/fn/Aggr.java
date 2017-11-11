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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
abstract class Aggr extends StandardFunc {
  /**
   * Sums up the specified item(s).
   * @param iter iterator
   * @param item first item
   * @param avg calculate average
   * @param qc query context
   * @return summed up item
   * @throws QueryException query exception
   */
  Item sum(final Iter iter, final Item item, final boolean avg, final QueryContext qc)
      throws QueryException {

    Item res = item.type.isUntyped() ? Dbl.get(item.dbl(info)) : item;
    final boolean num = res instanceof ANum, dtd = res.type == DTD, ymd = res.type == YMD;
    if(!num && !dtd && !ymd) throw SUM_X_X.get(info, res.type, res);

    int c = 1;
    for(Item it; (it = iter.next()) != null;) {
      qc.checkStop();
      if(it.type.isNumberOrUntyped()) {
        if(!num) throw SUMDUR_X_X.get(info, it.type, it);
      } else {
        if(num) throw SUMNUM_X_X.get(info, it.type, it);
        if(dtd && it.type != DTD || ymd && it.type != YMD) throw SUMDUR_X_X.get(info, it.type, it);
      }
      res = Calc.PLUS.ev(res, it, info);
      ++c;
    }
    return avg ? Calc.DIV.ev(res, Int.get(c), info) : res;
  }
}
