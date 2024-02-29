package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class MapPair extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item key = toAtomItem(arg(0), qc);
    final Value value = arg(1).value(qc);

    return XQMap.singleton(Str.KEY, key).put(Str.VALUE, value);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final SeqType st = arg(0).seqType().union(arg(1).seqType());
    exprType.assign(MapType.get(AtomType.STRING, st));
    return this;
  }

  @Override
  protected void simplifyArgs(final CompileContext cc) throws QueryException {
    arg(0, arg -> arg.simplifyFor(Simplify.DATA, cc));
  }
}
