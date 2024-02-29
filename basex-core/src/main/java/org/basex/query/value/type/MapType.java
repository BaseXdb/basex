package org.basex.query.value.type;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.in.DataInput;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Type for maps.
 *
 * @author BaseX Team 2005-24, BSD License
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

  /**
   * Creates a new map type.
   * @param keyType key type
   * @param declType declared return type
   * @return map type
   */
  public static MapType get(final AtomType keyType, final SeqType declType) {
    return declType.mapType(keyType);
  }

  @Override
  public XQMap cast(final Item item, final QueryContext qc, final InputInfo info)
      throws QueryException {
    if(item instanceof XQMap) {
      final XQMap m = (XQMap) item;
      if(m.instanceOf(this)) return m;
    }
    throw typeError(item, this, info);
  }

  @Override
  public XQMap read(final DataInput in, final QueryContext qc) throws IOException, QueryException {
    final MapBuilder mb = new MapBuilder();
    for(int s = in.readNum() - 1; s >= 0; s--) {
      mb.put((Item) Store.read(in, qc), Store.read(in, qc));
    }
    return mb.map();
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
    if(this == type || type.oneOf(SeqType.MAP, SeqType.FUNCTION, AtomType.ITEM)) return true;
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
      return get(keyType().union(mt.keyType()), declType.union(mt.declType));
    }
    return type instanceof ArrayType ? SeqType.FUNCTION :
           type instanceof FuncType  ? type.union(this) : AtomType.ITEM;
  }

  @Override
  public MapType intersect(final Type type) {
    if(instanceOf(type)) return this;
    if(type.instanceOf(this)) return (MapType) type;

    if(!(type instanceof FuncType) || type instanceof ArrayType) return null;

    final FuncType ft = (FuncType) type;
    final SeqType dt = declType.intersect(ft.declType);
    if(dt == null) return null;

    if(type instanceof MapType) {
      final Type kt = keyType().intersect(((MapType) type).keyType());
      if(kt instanceof AtomType) return get((AtomType) kt, dt);
    }

    return null;
  }

  /**
   * Returns the key type.
   * @return key type
   */
  public AtomType keyType() {
    return (AtomType) argTypes[0].type;
  }

  @Override
  public ID id() {
    return ID.MAP;
  }

  @Override
  public String toString() {
    final Object[] param = this == SeqType.MAP ? WILDCARD : new Object[] { keyType(), declType };
    return new QueryString().token(MAP).params(param).toString();
  }
}
