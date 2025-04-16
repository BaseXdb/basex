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
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public class MapType extends FType {
  /** Key type of the map. */
  private Type keyType;
  /** Value types (can be {@code null}, indicating that no type was specified). */
  private SeqType valueType;
  /** Flag indicating that keyType and valueType are final. */
  private boolean isFinal;

  /**
   * Constructor.
   * @param keyType key type
   * @param valueType value type
   */
  MapType(final Type keyType, final SeqType valueType) {
    this(keyType, valueType, true);
  }

  /**
   * Constructor.
   * @param keyType key type
   * @param valueType value type
   * @param isFinal whether keyType and valueType are final
   */
  MapType(final Type keyType, final SeqType valueType, final boolean isFinal) {
    this.keyType = keyType;
    this.valueType = valueType;
    this.isFinal = isFinal;
  }

  /**
   * Creates a new map type.
   * @param keyType key type
   * @param valueType value type
   * @return map type
   */
  public static MapType get(final Type keyType, final SeqType valueType) {
    final MapType mt = valueType.mapType(keyType);
    if(!mt.isFinal) throw Util.notExpected();
    return mt;
  }

  /**
   * Supply final value of key type and value type.
   * @param kt key type
   * @param vt value type
   */
  public void finalizeTypes(final Type kt, final SeqType vt) {
    if(isFinal) throw Util.notExpected();
    this.keyType = kt;
    this.valueType = vt;
    this.isFinal = true;
  }

  /**
   * Getter for the key type.
   * @return key type
   */
  public Type keyType() {
    return keyType;
  }

  /**
   * Getter for the value type.
   * @return value type
   */
  public SeqType valueType() {
    return valueType;
  }

  @Override
  public final XQMap cast(final Item item, final QueryContext qc, final InputInfo info)
      throws QueryException {
    if(item instanceof XQMap) {
      final XQMap map = (XQMap) item;
      if(map.instanceOf(this, false)) return map;
    }
    throw typeError(item, this, info);
  }

  @Override
  public final XQMap read(final DataInput in, final QueryContext qc)
      throws IOException, QueryException {
    int size = in.readNum();
    final MapBuilder mb = new MapBuilder(size);
    while(--size >= 0) mb.put((Item) Store.read(in, qc), Store.read(in, qc));
    return mb.map();
  }

  @Override
  public boolean eq(final Type type) {
    if(this == type) return true;
    if(type.getClass() != MapType.class) return false;
    final MapType mt = (MapType) type;
    return keyType.eq(mt.keyType) && valueType.eq(mt.valueType);
  }

  @Override
  public boolean instanceOf(final Type type) {
    if(this == type || type.oneOf(SeqType.MAP, SeqType.FUNCTION, AtomType.ITEM)) return true;
    if(this == SeqType.MAP && type == SeqType.RECORD) return true;
    if(type instanceof ChoiceItemType) return ((ChoiceItemType) type).hasInstance(this);
    if(type instanceof RecordType) return false;
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
    if(type == SeqType.RECORD) return SeqType.MAP;
    if(instanceOf(type)) return type;
    if(type.instanceOf(this)) return this;

    if(type instanceof MapType) {
      final MapType mt = (MapType) type;
      return union(mt.keyType, mt.valueType);
    }
    return type instanceof ArrayType ? SeqType.FUNCTION :
           type instanceof FuncType ? type.union(this) : AtomType.ITEM;
  }

  /**
   * Creates a union of two map types.
   * @param kt key type
   * @param vt value type
   * @return map type
   */
  public MapType union(final Type kt, final SeqType vt) {
    return keyType.eq(kt) && valueType.eq(vt) ? this : get(keyType.union(kt), valueType.union(vt));
  }

  @Override
  public Type intersect(final Type type) {
    if(type instanceof ChoiceItemType) return type.intersect(this);
    if(type == SeqType.RECORD) return SeqType.RECORD;
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
  public final FuncType funcType() {
    return FuncType.get(valueType.union(Occ.ZERO), keyType.seqType());
  }

  @Override
  public final AtomType atomic() {
    return null;
  }

  @Override
  public String toString() {
    final Object[] param = this == SeqType.MAP ? WILDCARD : new Object[] { keyType, valueType};
    return new QueryString().token(QueryText.MAP).params(param).toString();
  }
}
