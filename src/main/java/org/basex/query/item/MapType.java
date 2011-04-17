package org.basex.query.item;

import static org.basex.query.QueryTokens.*;

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
public final class MapType extends FunType {
  /** Key type of the map. */
  public final AtomType keyType;
  /** The general map type. */
  public static final MapType ANY_MAP = new MapType(AtomType.AAT,
      SeqType.ITEM_ZM);

  /**
   * Constructor.
   * @param arg argument type
   * @param rt return type
   */
  private MapType(final AtomType arg, final SeqType rt) {
    super(new SeqType[]{ arg.seq() }, rt);
    keyType = arg;
  }

  @Override
  public byte[] nam() {
    return MAP;
  }

  @Override
  public FItem e(final Item it, final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    if(!it.type.instance(this)) throw Err.cast(ii, this, it);

    return (Map) it;
  }

  @Override
  public boolean instance(final Type t) {
    return t instanceof MapType && super.instance(t);
  }

  /**
   * Creates a new map type.
   * @param key key type
   * @param val value type
   * @return map type
   */
  public static MapType get(final AtomType key, final SeqType val) {
    return new MapType(key, val);
  }
}
