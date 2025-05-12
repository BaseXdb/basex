package org.basex.query.func.map;

import org.basex.query.*;
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
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class MapEntry extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item key = toAtomItem(arg(0), qc);
    final Value value = arg(1).value(qc);

    return XQMap.get(key, value);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr key = arg(0), value = arg(1);

    final Type type;
    if(key instanceof final Str str && key.seqType().eq(SeqType.STRING_O)) {
      type = cc.qc.shared.record(str, value.seqType());
    } else {
      final AtomType kt = key.seqType().type.atomic();
      type = MapType.get(kt != null ? kt : AtomType.ANY_ATOMIC_TYPE, value.seqType());
    }
    exprType.assign(type);
    return this;
  }

  @Override
  public long structSize() {
    return 1;
  }
}
