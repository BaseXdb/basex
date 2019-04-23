package org.basex.query.value.type;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Type for maps.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Leo Woerteler
 */
public final class MapType extends FuncType {
  /**
   * Constructor.
   * @param type argument type
   * @param declType declared return type
   */
  MapType(final AtomType type, final SeqType declType) {
    super(declType, type.seqType());
  }

  @Override
  public byte[] string() {
    return Token.token(MAP);
  }

  @Override
  public XQMap cast(final Item item, final QueryContext qc, final StaticContext sc,
      final InputInfo ii) throws QueryException {

    if(item instanceof XQMap) {
      final XQMap m = (XQMap) item;
      if(m.instanceOf(this)) return m;
    }
    throw typeError(item, this, ii);
  }

  @Override
  public boolean eq(final Type type) {
    if(this == type) return true;
    if(!(type instanceof MapType)) return false;
    final MapType mt = (MapType) type;
    return keyType().eq(mt.keyType()) && declType.eq(mt.declType);
  }

  @Override
  public boolean instanceOf(final Type type) {
    return instanceOf(type, true);
  }

  @Override
  public Type union(final Type type) {
    if(instanceOf(type, false)) return type;

    if(type instanceof MapType) {
      final MapType mt = (MapType) type;
      if(mt.instanceOf(this, false)) return this;
      final AtomType kt = (AtomType) keyType().union(mt.keyType());
      final SeqType dt = declType.union(mt.declType);
      return get(kt, dt);
    }
    return type instanceof ArrayType ? SeqType.ANY_FUNC :
           type instanceof FuncType  ? type.union(this) : AtomType.ITEM;
  }

  @Override
  public Type intersect(final Type type) {
    if(instanceOf(type, false)) return this;

    if(type instanceof MapType) {
      final MapType mt = (MapType) type;
      if(mt.instanceOf(this, false)) return mt;
      final AtomType kt = (AtomType) keyType().intersect(mt.keyType());
      final SeqType dt = declType.intersect(mt.declType);
      if(kt != null && dt != null) return get(kt, dt);
    } else if(type instanceof FuncType) {
      final FuncType ft = (FuncType) type;
      if(ft.argTypes.length == 1 && ft.argTypes[0].instanceOf(SeqType.ANY_MAP.argTypes[0])) {
        final AtomType kt = (AtomType) keyType().intersect(ft.argTypes[0].type);
        final SeqType dt = declType.intersect(ft.declType);
        if(kt != null && dt != null) return get(kt, dt);
      }
    }
    return null;
  }

  /**
   * Instance test.
   * @param type type to be compared
   * @param generic check against generic map type
   * @return result of check
   */
  private boolean instanceOf(final Type type, final boolean generic) {
    return type == AtomType.ITEM || type == SeqType.ANY_FUNC || type == SeqType.ANY_MAP ||
        type instanceof FuncType && !(type instanceof ArrayType) &&
        instanceOf((FuncType) type, generic ? SeqType.ANY_MAP : this);
  }

  /**
   * Creates a new map type.
   * @param keyType key type
   * @param declType declared return type
   * @return map type
   */
  public static MapType get(final AtomType keyType, final SeqType declType) {
    return keyType == AtomType.AAT && declType.eq(SeqType.ITEM_ZM) ? SeqType.ANY_MAP :
      new MapType(keyType, declType);
  }

  /**
   * Returns the key type.
   * @return key type
   */
  public AtomType keyType() {
    return (AtomType) argTypes[0].type;
  }

  @Override
  public String toString() {
    final AtomType keyType = keyType();
    return keyType == AtomType.AAT && declType.eq(SeqType.ITEM_ZM) ? "map(*)"
        : "map(" + keyType + ", " + declType + ')';
  }
}
