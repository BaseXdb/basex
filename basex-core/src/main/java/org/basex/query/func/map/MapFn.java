package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Map functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class MapFn extends StandardFunc {
  /** Extended key (not defined in a record). */
  static final byte[] EXTENDED = {};

  /**
   * Returns type information for the specified map and key.
   * @param map map expression
   * @param key key expression
   * @return record information
   * @throws QueryException query exception
   */
  final MapInfo mapInfo(final Expr map, final Expr key) throws QueryException {
    final MapInfo mi = new MapInfo();
    final Type type = map.seqType().type;
    if(type instanceof MapType) {
      mi.mapType = (MapType) type;
      if(type instanceof RecordType) {
        mi.record = (RecordType) type;
        final Type kt = key.seqType().type;
        if(key instanceof Item && kt.isStringOrUntyped()) {
          final byte[] k = ((Item) key).string(null);
          mi.field = mi.record.field(k);
          mi.key = mi.field != null ? k : EXTENDED;
        } else if(kt.instanceOf(AtomType.ANY_ATOMIC_TYPE) && !kt.isStringOrUntyped()) {
          mi.key = EXTENDED;
        }
      }
    }
    return mi;
  }

  /** Map information. */
  static final class MapInfo {
    /** Map type ({@code null} if statically unknown). */
    MapType mapType;
    /** Record type ({@code null} if statically unknown). */
    RecordType record;
    /** Key ({@code null} if statically unknown). */
    byte[] key;
    /** Record field ({@code null} if statically unknown, or if key is unknown). */
    RecordField field;
  }
}
