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
public final class MapType extends FType {
  /** Key type of the map. */
  public final Type keyType;
  /** Value types (can be {@code null}, indicating that no type was specified). */
  public final SeqType valueType;

  /**
   * Constructor.
   * @param keyType key type
   * @param valueType value type
   */
  MapType(final Type keyType, final SeqType valueType) {
    this.keyType = keyType;
    this.valueType = valueType;
  }

  /**
   * Creates a new map type.
   * @param keyType key type
   * @param valueType value type
   * @return map type
   */
  public static MapType get(final Type keyType, final SeqType valueType) {
    return valueType.mapType(keyType);
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
    return keyType.eq(mt.keyType) && valueType.eq(mt.valueType);
  }

  @Override
  public boolean instanceOf(final Type type) {
    if(this == type || type.oneOf(SeqType.MAP, SeqType.FUNCTION, AtomType.ITEM)) return true;
    if(type instanceof ChoiceItemType) return ((ChoiceItemType) type).hasInstance(this);
    if(type instanceof MapType) {
      final MapType mt = (MapType) type;
      return valueType.instanceOf(mt.valueType) && keyType.instanceOf(mt.keyType);
    }
    if(type instanceof FuncType) {
      final FuncType ft = type.funcType();
      return funcType().declType.instanceOf(ft.declType) && ft.argTypes.length == 1 &&
          ft.argTypes[0].instanceOf(SeqType.ANY_ATOMIC_TYPE_O);
    }
    return false;
  }

  @Override
  public Type union(final Type type) {
    if(type instanceof ChoiceItemType) return type.union(this);
    if(instanceOf(type)) return type;
    if(type.instanceOf(this)) return this;

    if(type instanceof MapType) {
      final MapType mt = (MapType) type;
      return get(keyType.union(mt.keyType), valueType.union(mt.valueType));
    }
    return type instanceof ArrayType ? SeqType.FUNCTION :
           type instanceof FuncType ? type.union(this) : AtomType.ITEM;
  }

  @Override
  public Type intersect(final Type type) {
    if(type instanceof ChoiceItemType) return type.intersect(this);
    if(instanceOf(type)) return this;
    if(type.instanceOf(this)) return type;

    if(type instanceof MapType) {
      final MapType mt = (MapType) type;
      final Type kt = keyType.intersect(mt.keyType);
      final SeqType vt = valueType.intersect(mt.valueType);
      if(kt != null && kt.atomic() != null && vt != null) return get(kt, vt);
    }
    return null;
  }

  @Override
  public ID id() {
    return ID.MAP;
  }

  @Override
  public String toString() {
    final Object[] param = this == SeqType.MAP ? WILDCARD : new Object[] { keyType, valueType};
    return new QueryString().token(QueryText.MAP).params(param).toString();
  }

  @Override
  public FuncType funcType() {
    return FuncType.get(valueType.union(Occ.ZERO), SeqType.ANY_ATOMIC_TYPE_O);
  }

  @Override
  public AtomType atomic() {
    return null;
  }
}
