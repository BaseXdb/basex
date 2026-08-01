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
  /** Shape of the map ({@code null} if statically unknown). */
  public ShapeType shape;
  /** Field ({@code null} if statically unknown, or if key is unknown). */
  public ShapeField field;
  /** Field index ({@code 0} if statically unknown, or if key is unknown). */
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
      // record(*) has an unknown field set: no static information on single fields
      if(mt instanceof final ShapeType sh && !sh.any()) mti.shape = sh;
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
    if(shape != null) {
      if(expr instanceof final Item item) {
        final Type kt = expr.seqType().type;
        if(kt.isStringOrUntyped()) {
          final TokenObjectMap<ShapeField> fields = shape.fields();
          final byte[] key = item.string(null);
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
