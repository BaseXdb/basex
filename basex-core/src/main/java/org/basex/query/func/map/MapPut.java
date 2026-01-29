package org.basex.query.func.map;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
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
public final class MapPut extends MapFn {
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
    final MapTypeInfo mti = MapTypeInfo.get(map).key(key);
    if(mti.field != null) {
      // use optimized getter for records
      if(!mti.record.hasOptional()) return new RecordSet(info, map, mti.index, value).optimize(cc);

      final SeqType vt = value.seqType(), ft = mti.field.seqType();
      if(vt.instanceOf(ft)) {
        // structure does not change (new value has same type): propagate record type
        tp = mti.record;
      } else if(mti.record.fields().size() < RecordType.MAX_GENERATED_SIZE) {
        // otherwise, derive new record type
        tp = mti.record.copy(null, mti.key, vt.union(ft), cc);
      }
    } else if(mti.validKey) {
      if(mti.record.isExtensible()) {
        // structure does not change: propagate record type
        tp = mti.record;
      } else if(mti.key != null && mti.record.fields().size() < RecordType.MAX_GENERATED_SIZE) {
        // otherwise, derive new record type
        tp = mti.record.copy(null, mti.key, value.seqType(), cc);
      }
    }

    if(tp == null && mti.mapType != null) {
      final Type akt = key.seqType().type.atomic();
      if(akt != null) tp = mti.mapType.union(akt, value.seqType());
    }
    if(tp != null) exprType.assign(tp);
    return this;
  }
}
