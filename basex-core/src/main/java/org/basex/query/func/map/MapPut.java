package org.basex.query.func.map;

import static org.basex.query.func.Function.*;

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
 * @author Christian Gruen
 */
public final class MapPut extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQMap map = toMap(arg(0), qc);
    final Item key = toAtomItem(arg(1), qc);
    final Value value = arg(2).value(qc);

    return map.put(key, value);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr map = arg(0), key = arg(1), value = arg(2);
    if(map == XQMap.empty()) return cc.function(_MAP_ENTRY, info, key, value);

    Type tp = null;
    final MapCompilation mc = MapCompilation.get(map).key(key);
    if(mc.field != null) {
      // use optimized getter for records
      if(!mc.record.hasOptional()) return new RecordSet(info, map, mc.index, value).optimize(cc);

      final SeqType vt = value.seqType(), ft = mc.field.seqType();
      if(vt.instanceOf(ft)) {
        // structure does not change (new value has same type): propagate record type
        tp = mc.record;
      } else if(mc.record.fields().size() < RecordType.MAX_GENERATED_SIZE) {
        // otherwise, derive new record type
        tp = mc.record.copy(null, mc.key, vt.union(ft), cc);
      }
    } else if(mc.validKey) {
      if(mc.record.isExtensible()) {
        // structure does not change: propagate record type
        tp = mc.record;
      } else if(mc.key != null && mc.record.fields().size() < RecordType.MAX_GENERATED_SIZE) {
        // otherwise, derive new record type
        tp = mc.record.copy(null, mc.key, value.seqType(), cc);
      }
    }

    if(tp == null && mc.mapType != null) {
      final Type akt = key.seqType().type.atomic();
      if(akt != null) tp = mc.mapType.union(akt, value.seqType());
    }
    if(tp != null) exprType.assign(tp);
    return this;
  }
}
