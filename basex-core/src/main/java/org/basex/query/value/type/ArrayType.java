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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ArrayType extends FType {
  /** Name. */
  public static final byte[] ARRAY = Token.token(QueryText.ARRAY);

  /** Type of the array members. */
  public final SeqType memberType;

  /**
   * Constructor.
   * @param memberType member type
   */
  ArrayType(final SeqType memberType) {
    this.memberType = memberType;
  }

  /**
   * Creates an array type.
   * @param memberType member type
   * @return array type
   */
  public static ArrayType get(final SeqType memberType) {
    return memberType.arrayType();
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
        type instanceof ArrayType && memberType.eq(((ArrayType) type).memberType);
  }

  @Override
  public boolean instanceOf(final Type type) {
    if(this == type || type.oneOf(SeqType.ARRAY, SeqType.FUNCTION, AtomType.ITEM)) return true;

    if(type instanceof ArrayType) return memberType.instanceOf(((ArrayType) type).memberType);

    if(type instanceof FuncType) {
      final FuncType ft = (FuncType) type;
      return memberType.instanceOf(ft.declType) && ft.argTypes.length == 1 &&
          ft.argTypes[0].instanceOf(SeqType.INTEGER_O);
    }
    return false;
  }

  @Override
  public Type union(final Type type) {
    if(instanceOf(type)) return type;
    if(type.instanceOf(this)) return this;

    if(type instanceof ArrayType) return get(memberType.union(((ArrayType) type).memberType));
    return type instanceof MapType ? SeqType.FUNCTION :
           type instanceof FuncType ? type.union(this) : AtomType.ITEM;
  }

  @Override
  public ArrayType intersect(final Type type) {
    if(instanceOf(type)) return this;
    if(type.instanceOf(this)) return (ArrayType) type;

    if(type instanceof ArrayType) {
      final SeqType mt = memberType.intersect(((ArrayType) type).memberType);
      if(mt != null) return get(mt);
    }
    return null;
  }

  @Override
  public FuncType funcType() {
    return FuncType.get(memberType, SeqType.INTEGER_O);
  }

  @Override
  public AtomType atomic() {
    return memberType.type.atomic();
  }

  @Override
  public ID id() {
    return ID.ARRAY;
  }

  @Override
  public String toString() {
    final Object[] param = this == SeqType.ARRAY ? WILDCARD : new Object[] { memberType };
    return new QueryString().token(ARRAY).params(param).toString();
  }
}
