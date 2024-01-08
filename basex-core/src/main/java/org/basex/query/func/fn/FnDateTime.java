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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnDateTime extends DateTime {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item date = arg(0).atomItem(qc, info);
    final Item time = arg(1).atomItem(qc, info);
    if(date.isEmpty() || time.isEmpty()) return Empty.VALUE;

    final Dat dat = date.type.isUntyped() ? new Dat(date.string(info), info) :
      (Dat) checkType(date, AtomType.DATE);
    final Tim tim = time.type.isUntyped() ? new Tim(time.string(info), info) :
      (Tim) checkType(time, AtomType.TIME);
    return new Dtm(dat, tim, info);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr date = arg(0), time = defined(1) ? arg(1) : Str.EMPTY;
    final SeqType stDate = date.seqType(), stTime = time.seqType();
    if(stDate.zero()) return date;
    if(stTime.zero()) return time;
    if(stDate.oneOrMore() && !stDate.mayBeArray() && stTime.oneOrMore() && !stTime.mayBeArray())
      exprType.assign(Occ.EXACTLY_ONE);
    return this;
  }
}
