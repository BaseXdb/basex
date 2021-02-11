package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnDateTime extends DateTime {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = exprs[0].atomItem(qc, info);
    final Item zone = exprs.length == 2 ? exprs[1].atomItem(qc, info) : null;
    if(item == Empty.VALUE || zone == Empty.VALUE) return Empty.VALUE;

    final Dat date = item.type.isUntyped() ? new Dat(item.string(info), info) :
      (Dat) checkType(item, AtomType.DATE);
    final Tim time = zone.type.isUntyped() ? new Tim(zone.string(info), info) :
      (Tim) checkType(zone, AtomType.TIME);
    return new Dtm(date, time, info);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr expr1 = exprs[0], expr2 = exprs.length == 2 ? exprs[1] : Str.EMPTY;
    final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();
    if(st1.zero()) return expr1;
    if(st2.zero()) return expr2;
    if(st1.oneOrMore() && !st1.mayBeArray() && st2.oneOrMore() && !st2.mayBeArray())
      exprType.assign(Occ.EXACTLY_ONE);
    return this;
  }
}
