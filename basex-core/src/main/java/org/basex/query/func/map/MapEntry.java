package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class MapEntry extends MapFn {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item key = toAtomItem(arg(0), qc);
    final Value value = arg(1).value(qc);

    final XQMap map = XQMap.get(key, value);
    if(seqType().type instanceof final RecordType rt) map.type = rt;
    return map;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr key = arg(0), value = arg(1);

    final Type type;
    if(key instanceof final Str str && key.seqType().eq(Types.STRING_O)) {
      final TokenObjectMap<RecordField> fields = new TokenObjectMap<>(1);
      fields.put(str.string(), new RecordField(value.seqType()));
      type = cc.qc.shared.record(new RecordType(fields));
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
