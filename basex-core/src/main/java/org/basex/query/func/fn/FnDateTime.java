package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnDateTime extends DateTime {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = exprs[0].atomItem(qc, info);
    final Item zone = exprs.length == 2 ? exprs[1].atomItem(qc, info) : null;
    if(item == null || zone == null) return null;

    final Dat d = item.type.isUntyped() ? new Dat(item.string(info), info) :
      (Dat) checkType(item, AtomType.DAT);
    final Tim t = zone.type.isUntyped() ? new Tim(zone.string(info), info) :
      (Tim) checkType(zone, AtomType.TIM);
    return new Dtm(d, t, info);
  }
}
