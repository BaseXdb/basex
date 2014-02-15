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
 * @author BaseX Team 2005-14, BSD License
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
    super(new Ann(), new SeqType[]{ arg.seqType() }, rt);
    keyType = arg;
  }

  @Override
  public byte[] string() {
    return MAP;
  }

  @Override
  public Map cast(final Item it, final QueryContext ctx, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    if(it instanceof Map) {
      final Map m = (Map) it;
      if(m.hasType(this)) return m;
    }
    throw Err.castError(ii, this, it);
  }

  @Override
  public boolean eq(final Type t) {
    if(this == t) return true;
    if(t.getClass() != MapType.class) return false;
    final MapType mt = (MapType) t;
    return keyType.eq(mt.keyType) && ret.eq(mt.ret);
  }

  @Override
  public boolean instanceOf(final Type t) {
    if(!(t instanceof MapType)) return super.instanceOf(t);
    final MapType mt = (MapType) t;
    return ret.instanceOf(mt.ret) && mt.keyType.instanceOf(keyType);
  }

  @Override
  public Type union(final Type t) {
    if(instanceOf(t)) return t;
    if(t instanceof MapType) {
      final MapType mt = (MapType) t;
      if(mt.instanceOf(this)) return this;
      final AtomType a = (AtomType) keyType.intersect(mt.keyType);
      return a != null ? get(a, ret.union(mt.ret)) : ANY_FUN;
    }
    return t instanceof FuncType ? t.union(this) : AtomType.ITEM;
  }

  @Override
  public MapType intersect(final Type t) {
    // case for item() and compatible FuncType, e.g. function(xs:anyAtomicType) as item()*
    // also excludes FuncType.ANY_FUN
    if(instanceOf(t)) return this;
    if(t instanceof MapType) {
      final MapType mt = (MapType) t;
      if(mt.instanceOf(this)) return mt;
      final SeqType rt = ret.intersect(mt.ret);
      return rt == null ? null : get((AtomType) keyType.union(mt.keyType), rt);
    }
    if(t instanceof FuncType) {
      final FuncType ft = (FuncType) t;
      if(ft.args.length == 1 && ft.args[0].instanceOf(SeqType.AAT)) {
        final SeqType rt = ret.intersect(ft.ret);
        return rt == null ? null : get((AtomType) keyType.union(ft.args[0].type), rt);
      }
    }
    return null;
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
