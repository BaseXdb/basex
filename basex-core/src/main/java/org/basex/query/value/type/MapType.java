package org.basex.query.value.type;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Type for maps.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class MapType extends FuncType {
  /** Name. */
  public static final byte[] MAP = Token.token(QueryText.MAP);

  /**
   * Constructor.
   * @param keyType key type
   * @param declType declared return type
   */
  MapType(final AtomType keyType, final SeqType declType) {
    super(declType, keyType.seqType());
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
    if(type.oneOf(SeqType.MAP, SeqType.FUNCTION, AtomType.ITEM)) return true;
    if(!(type instanceof FuncType) || type instanceof ArrayType) return false;

    final FuncType ft = (FuncType) type;
    return declType.instanceOf(ft.declType) && (
      type instanceof MapType ? keyType().instanceOf(((MapType) type).keyType()) :
      ft.argTypes.length == 1 && ft.argTypes[0].instanceOf(SeqType.ANY_ATOMIC_TYPE_O)
    );
  }

  @Override
  public Type union(final Type type) {
    if(instanceOf(type)) return type;
    if(type.instanceOf(this)) return this;

    if(type instanceof MapType) {
      final MapType mt = (MapType) type;
      return get((AtomType) keyType().union(mt.keyType()), declType.union(mt.declType));
    }
    return type instanceof ArrayType ? SeqType.FUNCTION :
           type instanceof FuncType  ? type.union(this) : AtomType.ITEM;
  }

  @Override
  public Type intersect(final Type type) {
    if(instanceOf(type)) return this;
    if(type.instanceOf(this)) return type;

    if(!(type instanceof FuncType) || type instanceof ArrayType) return null;

    final FuncType ft = (FuncType) type;
    final SeqType dt = declType.intersect(ft.declType);
    if(dt == null) return null;

    if(type instanceof MapType) {
      final MapType mt = (MapType) type;
      final AtomType kt = (AtomType) keyType().intersect(mt.keyType());
      return kt != null ? get(kt, dt) : null;
    }

    return ft.argTypes.length == 1 && ft.argTypes[0].instanceOf(SeqType.ANY_ATOMIC_TYPE_O) ?
      new FuncType(dt, ft.argTypes[0].union(SeqType.ANY_ATOMIC_TYPE_O)) : null;
  }

  /**
   * Returns the key type.
   * @return key type
   */
  public AtomType keyType() {
    return (AtomType) argTypes[0].type;
  }

  /**
   * Creates a new map type.
   * @param keyType key type
   * @param declType declared return type
   * @return map type
   */
  public static MapType get(final AtomType keyType, final SeqType declType) {
    return keyType == AtomType.ANY_ATOMIC_TYPE && declType.eq(SeqType.ITEM_ZM) ? SeqType.MAP :
      new MapType(keyType, declType);
  }

  @Override
  public String toString() {
    final Object[] param = this == SeqType.MAP ? WILDCARD : new Object[] { keyType(), declType };
    return new QueryString().token(MAP).params(param).toString();
  }
}
