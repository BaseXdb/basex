package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class FnDateTime extends DateTime {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = exprs[0].atomItem(qc, info);
    final Item zone = exprs.length == 2 ? exprs[1].atomItem(qc, info) : null;
    if(item == Empty.VALUE || zone == Empty.VALUE) return Empty.VALUE;

    final Dat date = item.type.isUntyped() ? new Dat(item.string(info), info) :
      (Dat) checkType(item, AtomType.DAT);
    final Tim time = zone.type.isUntyped() ? new Tim(zone.string(info), info) :
      (Tim) checkType(zone, AtomType.TIM);
    return new Dtm(date, time, info);
  }
}
