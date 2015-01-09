package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnDateTime extends DateTime {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item it = exprs[0].atomItem(qc, info);
    final Item zon = exprs.length == 2 ? exprs[1].atomItem(qc, info) : null;
    if(it == null || zon == null) return null;

    final Dat d = it.type.isUntyped() ? new Dat(it.string(info), info) :
      (Dat) checkType(it, AtomType.DAT);
    final Tim t = zon.type.isUntyped() ? new Tim(zon.string(info), info) :
      (Tim) checkType(zon, AtomType.TIM);
    return new Dtm(d, t, info);
  }
}
