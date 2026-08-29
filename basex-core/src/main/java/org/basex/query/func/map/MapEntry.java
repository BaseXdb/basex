package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class MapEntry extends MapFn {
  @Override
  public XQMap value(final QueryContext qc) throws QueryException {
    final Value value = arg(1).value(qc);

    return seqType().type instanceof final ShapeType sh ? XQMap.get(sh, value) :
      XQMap.get(toAtomItem(arg(0), qc), value);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr key = arg(0), value = arg(1);

    final Type type;
    if(key instanceof final Str str && key.seqType().eq(Types.STRING_O)) {
      final TokenObjectMap<ShapeField> fields = new TokenObjectMap<>(1);
      fields.put(str.string(), new ShapeField(value.seqType()));
      type = cc.qc.shared.shape(new ShapeType(fields));
    } else {
      final BasicType kt = key.seqType().type.atomic();
      type = MapType.get(kt != null ? kt : BasicType.ANY_ATOMIC_TYPE, value.seqType());
    }
    exprType.assign(type);
    return this;
  }

  @Override
  public long structSize() {
    return 1;
  }
}
