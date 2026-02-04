package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;

/**
 * Compile-time information on maps.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MapTypeInfo {
  /** Map type ({@code null} if statically unknown). */
  public MapType mapType;
  /** Record type ({@code null} if statically unknown). */
  public RecordType record;
  /** Key ({@code null} if statically unknown). */
  public byte[] key;
  /** Record field ({@code null} if statically unknown, or if key is unknown). */
  public RecordField field;
  /** Record field index ({@code 0} if statically unknown, or if key is unknown). */
  public Integer index;
  /** Key type mismatch. */
  public boolean keyMismatch;
  /** Key is known to be valid. */
  public boolean validKey;

  /**
   * Returns compile-time information for the specified map.
   * @param map map expression
   * @return map information
   */
  public static MapTypeInfo get(final Expr map) {
    final MapTypeInfo mti = new MapTypeInfo();
    final SeqType st = map.seqType();
    if(st.one() && st.type instanceof final MapType mt) {
      mti.mapType = mt;
      if(mt instanceof final RecordType rt) mti.record = rt;
    }
    return mti;
  }

  /**
   * Attaches map information for the specified key.
   * @param expr key expression
   * @return map information
   * @throws QueryException query exception
   */
  public MapTypeInfo key(final Expr expr) throws QueryException {
    if(record != null) {
      if(expr instanceof final Item item) {
        final Type kt = expr.seqType().type;
        if(kt.isStringOrUntyped()) {
          final TokenObjectMap<RecordField> fields = record.fields();
          key = item.string(null);
          index = fields.index(key);
          field = fields.get(key);
        }
        if(kt.instanceOf(BasicType.ANY_ATOMIC_TYPE)) validKey = true;
      }
    }
    if(mapType != null) {
      final Type et = expr.seqType().type.atomic(), kt = mapType.keyType();
      keyMismatch = et != null && (kt.isStringOrUntyped() && et.isNumber() ||
        kt.isNumber() && et.isStringOrUntyped());
    }
    return this;
  }
}
