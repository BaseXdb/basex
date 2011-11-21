package org.basex.query.item;

import static org.basex.query.QueryText.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.map.Map;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;

/**
 * Type for maps.
 *
 * @author BaseX Team 2005-11, BSD License
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
    super(new SeqType[]{ arg.seq() }, rt);
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
  public FItem e(final Item it, final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    if(!it.isMap() || !((Map) it).hasType(this)) throw Err.cast(ii, this, it);

    return (Map) it;
  }

  /**
   * Creates a new map type.
   * @param key key type
   * @param val value type
   * @return map type
   */
  public static MapType get(final AtomType key, final SeqType val) {
    if(key == AtomType.AAT && val.eq(SeqType.ITEM_ZM)) return SeqType.ANY_MAP;
    return new MapType(key, val);
  }

  @Override
  public String toString() {
    return keyType == AtomType.AAT && ret.eq(SeqType.ITEM_ZM) ? "map(*)"
        : "map(" + keyType + ", " + ret + ")";
  }
}
