package org.basex.query.value.type;

import static org.basex.query.QueryError.*;

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
  public final SeqType valueType;

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

  @Override
  public XQArray cast(final Item item, final QueryContext qc, final InputInfo info)
      throws QueryException {

    if(item instanceof XQArray) {
      final XQArray a = (XQArray) item;
      if(a.instanceOf(this)) return a;
    }
    throw typeError(item, this, info);
  }

  @Override
  public XQArray read(final DataInput in, final QueryContext qc)
      throws IOException, QueryException {
    final ArrayBuilder ab = new ArrayBuilder();
    for(int s = in.readNum() - 1; s >= 0; s--) ab.append(Store.read(in, qc));
    return ab.array();
  }

  @Override
  public boolean eq(final Type type) {
    return this == type ||
        type instanceof ArrayType && valueType.eq(((ArrayType) type).valueType);
  }

  @Override
  public boolean instanceOf(final Type type) {
    if(this == type || type.oneOf(SeqType.ARRAY, SeqType.FUNCTION, AtomType.ITEM)) return true;
    if(type instanceof ArrayType) return valueType.instanceOf(((ArrayType) type).valueType);
    if(type instanceof FuncType) {
      final FuncType ft = (FuncType) type;
      return valueType.instanceOf(ft.declType) && ft.argTypes.length == 1 &&
          ft.argTypes[0].instanceOf(SeqType.INTEGER_O);
    }
    if(type instanceof ChoiceItemType) return ((ChoiceItemType) type).hasInstance(this);
    return false;
  }

  @Override
  public Type union(final Type type) {
    if(type instanceof ChoiceItemType) return type.union(this);
    if(instanceOf(type)) return type;
    if(type.instanceOf(this)) return this;

    if(type instanceof ArrayType) return get(valueType.union(((ArrayType) type).valueType));
    return type instanceof MapType ? SeqType.FUNCTION :
           type instanceof FuncType ? type.union(this) : AtomType.ITEM;
  }

  @Override
  public ArrayType intersect(final Type type) {
    if(type instanceof ChoiceItemType) return (ArrayType) type.intersect(this);
    if(instanceOf(type)) return this;
    if(type.instanceOf(this)) return (ArrayType) type;

    if(type instanceof ArrayType) {
      final SeqType mt = valueType.intersect(((ArrayType) type).valueType);
      if(mt != null) return get(mt);
    }
    return null;
  }

  @Override
  public FuncType funcType() {
    return FuncType.get(valueType, SeqType.INTEGER_O);
  }

  @Override
  public AtomType atomic() {
    return valueType.type.atomic();
  }

  @Override
  public ID id() {
    return ID.ARRAY;
  }

  @Override
  public String toString() {
    final Object[] param = this == SeqType.ARRAY ? WILDCARD : new Object[] { valueType };
    return new QueryString().token(QueryText.ARRAY).params(param).toString();
  }
}
