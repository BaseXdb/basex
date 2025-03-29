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
public final class MapCompilation {
  /** Extended key (not defined in a record). */
  public static final byte[] EXTENDED = {};

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

  /**
   * Returns type information for the specified map and key.
   * @param map map expression
   * @return record information
   */
  public static MapCompilation get(final Expr map) {
    final MapCompilation mi = new MapCompilation();
    final Type type = map.seqType().type;
    if(type instanceof MapType) {
      mi.mapType = (MapType) type;
      if(type instanceof RecordType) mi.record = (RecordType) type;
    }
    return mi;
  }

  /**
   * Attaches map information for the specified key.
   * @param expr key expression
   * @return map information
   * @throws QueryException query exception
   */
  public MapCompilation key(final Expr expr) throws QueryException {
    if(record != null) {
      final Type kt = expr.seqType().type;
      if(kt.isStringOrUntyped()) {
        if(expr instanceof Item) {
          final TokenObjectMap<RecordField> fields = record.fields();
          final byte[] k = ((Item) expr).string(null);
          index = fields.index(k);
          field = fields.get(k);
          key = field != null ? k : EXTENDED;
        }
      } else if(kt.instanceOf(AtomType.ANY_ATOMIC_TYPE)) {
        key = EXTENDED;
      }
    }
    return this;
  }

  /**
   * Attaches map information for the specified index.
   * @param expr index expression
   * @return map information
   */
  public MapCompilation index(final Expr expr) {
    if(record != null && expr instanceof Int) {
      index = (int) ((Int) expr).itr();
      final TokenObjectMap<RecordField> fields = record.fields();
      if(index > 0 && index <= fields.size()) {
        field = fields.value(index);
      }
    }
    return this;
  }
}