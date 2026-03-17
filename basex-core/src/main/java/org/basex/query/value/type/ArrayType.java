package org.basex.query.value.type;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.Types.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.in.DataInput;
import org.basex.query.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Type for arrays.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayType extends FType {
  /** Type of the array values. */
  private final SeqType valueType;

  /**
   * Constructor.
   * @param valueType value type
   */
  ArrayType(final SeqType valueType) {
    this.valueType = valueType;
  }

  /**
   * Creates an array type.
   * @param valueType value type
   * @return array type
   */
  public static ArrayType get(final SeqType valueType) {
    return valueType.arrayType();
  }

  /**
   * Getter for the value type.
   * @return value type
   */
  public SeqType valueType() {
    return valueType;
  }

  @Override
  public XQArray cast(final Item item, final QueryContext qc, final InputInfo info)
      throws QueryException {
    if(item instanceof final XQArray array && array.instanceOf(this, false)) return array;
    throw typeError(item, this, info);
  }

  @Override
  public XQArray read(final DataInput in, final QueryContext qc)
      throws IOException, QueryException {
    int size = in.readNum();
    final ArrayBuilder ab = new ArrayBuilder(qc, size);
    while(--size >= 0) ab.add(Stores.read(in, qc));
    return ab.array(this);
  }

  @Override
  public boolean eq(final Type type) {
    return this == type || type instanceof final ArrayType at && valueType.eq(at.valueType);
  }

  @Override
  public boolean instanceOf(final Type type) {
    if(this == type || type.oneOf(ARRAY, FUNCTION, BasicType.ITEM)) return true;
    if(type instanceof final ArrayType at) {
      return this != ARRAY && valueType.instanceOf(at.valueType);
    }
    if(type instanceof final FuncType ft) {
      return valueType.instanceOf(ft.declType) && ft.argTypes.length == 1 &&
          ft.argTypes[0].instanceOf(INTEGER_O);
    }
    return type instanceof final ChoiceItemType cit && cit.hasInstance(this);
  }

  @Override
  public Type union(final Type type) {
    return type instanceof ChoiceItemType ? type.union(this) :
      instanceOf(type) ? type :
      type.instanceOf(this) ? this :
      type instanceof final ArrayType at ? union(at.valueType) :
      type instanceof MapType ? FUNCTION :
      type instanceof FuncType ? type.union(this) : BasicType.ITEM;
  }

  /**
   * Creates a union of two array types.
   * @param vt value type
   * @return array type
   */
  public ArrayType union(final SeqType vt) {
    return valueType.eq(vt) ? this : get(valueType.union(vt));
  }

  @Override
  public ArrayType intersect(final Type type) {
    if(type instanceof ChoiceItemType) return (ArrayType) type.intersect(this);
    if(instanceOf(type)) return this;
    if(type.instanceOf(this)) return (ArrayType) type;

    if(type instanceof final ArrayType at) {
      final SeqType mt = valueType.intersect(at.valueType);
      if(mt != null) return get(mt);
    }
    return null;
  }

  @Override
  public FuncType funcType() {
    return FuncType.get(valueType, INTEGER_O);
  }

  @Override
  public BasicType atomic() {
    return valueType.type.atomic();
  }

  @Override
  public ID id() {
    return ID.ARRAY;
  }

  @Override
  public String name() {
    return QueryText.ARRAY;
  }

  @Override
  public String toString() {
    return toString(", ", this == ARRAY ? WILDCARD : new Object[] { valueType });
  }
}
