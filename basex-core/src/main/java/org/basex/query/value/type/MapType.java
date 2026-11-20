package org.basex.query.value.type;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.Types.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.in.DataInput;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * XDM map types.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public class MapType extends FType {
  /** Key type. */
  private final Type keyType;
  /** Value type. */
  private final SeqType valueType;

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
   * Returns the map type with the specified key and value type.
   * @param keyType key type
   * @param valueType value type
   * @return map type
   */
  public static MapType get(final Type keyType, final SeqType valueType) {
    return valueType.mapType(keyType);
  }

  /**
   * Returns the map type with the key and value type of the specified map or record type.
   * @param mapType map or record type
   * @return map type
   */
  public static MapType get(final MapType mapType) {
    return get(mapType.keyType, mapType.valueType);
  }

  /**
   * Returns the key type.
   * @return key type
   */
  public final Type keyType() {
    return keyType;
  }

  /**
   * Returns the value type.
   * @return value type
   */
  public final SeqType valueType() {
    return valueType;
  }

  @Override
  public Coercion coercion() {
    return valueType.type.coercion();
  }

  @Override
  public boolean attachable() {
    return keyType.attachable() && valueType.type.attachable();
  }

  @Override
  public boolean global() {
    return keyType.global() && valueType.type.global();
  }

  @Override
  public final XQMap cast(final Item item, final QueryContext qc, final InputInfo info)
      throws QueryException {
    if(item instanceof final XQMap map && map.instanceOf(this, false)) return map;
    throw typeError(item, this, info);
  }

  @Override
  public final XQMap read(final DataInput in, final QueryContext qc)
      throws IOException, QueryException {
    int size = in.readNum();
    final MapBuilder mb = new MapBuilder(size);
    while(--size >= 0) mb.put((Item) Stores.read(in, qc), Stores.read(in, qc));
    return mb.map();
  }

  @Override
  public boolean eq(final Type type) {
    if(this == type) return true;
    if(type.getClass() != MapType.class) return false;
    final MapType mt = (MapType) type;
    return TypeRef.eq(keyType, mt.keyType) && valueType.eq(mt.valueType);
  }

  @Override
  public boolean instanceOf(final Type type) {
    if(this == type || type.oneOf(MAP, FUNCTION, BasicType.ITEM)) return true;
    if(type instanceof ShapeType) return false;
    if(type instanceof final MapType mt) {
      return this != MAP && valueType.instanceOf(mt.valueType) &&
        keyType.instanceOf(mt.keyType.deref());
    }
    if(type instanceof final FuncType ft) {
      return funcType().declType.instanceOf(ft.declType) && ft.unary(ANY_ATOMIC_TYPE_O);
    }
    return type instanceof final ChoiceItemType cit && cit.hasInstance(this);
  }

  @Override
  public Type union(final Type type) {
    if(type instanceof ChoiceItemType || type instanceof ShapeType) return type.union(this);
    if(type.keptExact(this, true)) return this;
    if(keptExact(type, true)) return type;
    if(type instanceof final MapType mt) return union(mt.keyType, mt.valueType);
    return type instanceof ArrayType ? FUNCTION :
           type instanceof FuncType ? type.union(this) : BasicType.ITEM;
  }

  /**
   * Returns the union of this type and the map type with the specified key and value type.
   * @param kt key type of the other map type
   * @param vt value type of the other map type
   * @return map type
   */
  public MapType union(final Type kt, final SeqType vt) {
    return TypeRef.eq(keyType, kt) && valueType.eq(vt) ? this :
      get(keyType.union(kt.deref()), valueType.union(vt));
  }

  @Override
  public Type intersect(final Type type) {
    if(type instanceof ChoiceItemType || type instanceof ShapeType) return type.intersect(this);
    if(instanceOf(type)) return this;
    if(type.instanceOf(this)) return type;

    if(type instanceof final MapType mt) {
      // disjoint key or value types: the empty map is still an instance of both types
      final Type kt = keyType.intersect(mt.keyType.deref());
      final SeqType vt = valueType.intersect(mt.valueType);
      return kt != null && vt != null ? get(kt, vt) : get(BasicType.ERROR, ERROR_O);
    }
    return type instanceof final FuncType ft && callable(ft) ? this : null;
  }

  /**
   * Checks if maps can be instances of the specified function type.
   * @param ft function type
   * @return result of check
   */
  final boolean callable(final FuncType ft) {
    // maps take a single atomic argument, and return the empty sequence for unknown keys
    return ft.unary(ANY_ATOMIC_TYPE_O) && !ft.declType.oneOrMore();
  }

  @Override
  public final FuncType funcType() {
    return FuncType.get(valueType.union(Occ.ZERO), keyType.seqType());
  }

  @Override
  public final BasicType atomic() {
    return null;
  }

  @Override
  public ID id() {
    return ID.MAP;
  }

  @Override
  public Object name() {
    return QueryText.MAP;
  }

  @Override
  public String toString() {
    return toString(", ", this == MAP ? WILDCARD : new Object[] { keyType, valueType });
  }
}
