package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnAbs extends Num {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item it = exprs[0].atomItem(qc, info);
    if(it == null) return null;
    toDbl(it);
    return abs(it, info);
  }

  /**
   * Returns an absolute number.
   * @param it input item
   * @param ii input info
   * @return absolute item
   * @throws QueryException query exception
   */
  public static Item abs(final Item it, final InputInfo ii) throws QueryException {
    final double d = it.dbl(ii);
    final boolean s = d > 0d || 1 / d > 0;

    final Type ip = it.type;
    if(ip instanceof AtomType) {
      switch((AtomType) ip) {
        case DBL: return s ? it : Dbl.get(Math.abs(it.dbl(ii)));
        case FLT: return s ? it : Flt.get(Math.abs((float) it.dbl(ii)));
        case DEC: return s ? it : Dec.get(it.dec(ii).abs());
        case ITR: return s ? it : Int.get(Math.abs(it.itr(ii)));
        default:  break;
      }
    }
    return ip.instanceOf(AtomType.ITR) ? Int.get(Math.abs(it.itr(ii))) : Dec.get(it.dec(ii).abs());
  }
}
