package org.basex.query.value.type;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Type for maps.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class MapType extends FuncType {
  /** Key type of the map. */
  public final AtomType keyType;

  /**
   * Constructor.
   * @param arg argument type
   * @param rt return type
   */
  MapType(final AtomType arg, final SeqType rt) {
    super(new SeqType[]{ arg.seqType() }, rt);
    keyType = arg;
  }

  @Override
  public byte[] string() {
    return MAP;
  }

  @Override
  public boolean isMap() {
    return true;
  }

  @Override
  public FItem cast(final Item it, final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    if(!it.type.isMap() || !((Map) it).hasType(this)) Err.cast(ii, this, it);
    return (Map) it;
  }

  /**
   * Creates a new map type.
   * @param key key type
   * @param val value type
   * @return map type
   */
  public static MapType get(final AtomType key, final SeqType val) {
    return key == AtomType.AAT && val.eq(SeqType.ITEM_ZM) ?
        SeqType.ANY_MAP : new MapType(key, val);
  }

  @Override
  public String toString() {
    return keyType == AtomType.AAT && ret.eq(SeqType.ITEM_ZM) ? "map(*)"
        : "map(" + keyType + ", " + ret + ')';
  }
}
